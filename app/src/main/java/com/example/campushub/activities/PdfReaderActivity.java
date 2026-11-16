package com.example.campushub.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campushub.R;
import java.io.IOException;
import java.io.File;

public class PdfReaderActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID = "book_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_PDF_PATH = "pdf_path";
    private ParcelFileDescriptor descriptor;
    private PdfRenderer renderer;
    private int pageIndex;
    private String bookId;
    private ImageView pageImage;
    private TextView pageNumber;
    private ProgressBar loading;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        pageImage = findViewById(R.id.imgPdfPage);
        pageNumber = findViewById(R.id.tvPageNumber);
        loading = findViewById(R.id.readerLoading);
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        ((TextView) findViewById(R.id.tvReaderTitle)).setText(getIntent().getStringExtra(EXTRA_BOOK_TITLE));
        findViewById(R.id.btnReaderBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnPreviousPage).setOnClickListener(v -> showPage(pageIndex - 1));
        findViewById(R.id.btnNextPage).setOnClickListener(v -> showPage(pageIndex + 1));
        try {
            String cachedPath = getIntent().getStringExtra(EXTRA_PDF_PATH);
            if (cachedPath != null) {
                descriptor = ParcelFileDescriptor.open(new File(cachedPath), ParcelFileDescriptor.MODE_READ_ONLY);
            } else if (getIntent().getData() != null) {
                descriptor = getContentResolver().openFileDescriptor(getIntent().getData(), "r");
            } else {
                throw new IOException("No PDF selected");
            }
            if (descriptor == null) throw new IOException("PDF could not be opened");
            renderer = new PdfRenderer(descriptor);
            if (renderer.getPageCount() <= 0) throw new IOException("PDF has no pages");
            int savedProgress = getSharedPreferences("campus_library", MODE_PRIVATE)
                    .getInt("progress_" + bookId, 0);
            pageIndex = Math.min(renderer.getPageCount() - 1,
                    Math.max(0, (int) Math.floor(savedProgress * renderer.getPageCount() / 100.0)));
            showPage(pageIndex);
        } catch (Exception e) {
            Toast.makeText(this, R.string.library_open_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showPage(int requestedIndex) {
        if (renderer == null) return;
        pageIndex = Math.max(0, Math.min(renderer.getPageCount() - 1, requestedIndex));
        loading.setVisibility(View.VISIBLE);
        pageImage.setVisibility(View.INVISIBLE);
        PdfRenderer.Page page = null;
        try {
            page = renderer.openPage(pageIndex);
            int maxDimension = 2200;
            float scale = Math.min((float) maxDimension / page.getWidth(), (float) maxDimension / page.getHeight());
            int width = Math.max(1, Math.round(page.getWidth() * scale));
            int height = Math.max(1, Math.round(page.getHeight() * scale));
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.WHITE);
            page.render(bitmap, null, new Matrix(), PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pageImage.setImageBitmap(bitmap);
            pageImage.setVisibility(View.VISIBLE);
            int count = renderer.getPageCount();
            pageNumber.setText(getString(R.string.reader_page, pageIndex + 1, count));
            findViewById(R.id.btnPreviousPage).setEnabled(pageIndex > 0);
            findViewById(R.id.btnNextPage).setEnabled(pageIndex < count - 1);
            int progress = Math.min(100, Math.round((pageIndex + 1) * 100f / count));
            getSharedPreferences("campus_library", MODE_PRIVATE).edit()
                    .putInt("progress_" + bookId, progress).apply();
            loading.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, R.string.library_open_error, Toast.LENGTH_SHORT).show();
        } finally {
            if (page != null) page.close();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { if (renderer != null) renderer.close(); } catch (Exception ignored) { }
        try { if (descriptor != null) descriptor.close(); } catch (Exception ignored) { }
    }
}
