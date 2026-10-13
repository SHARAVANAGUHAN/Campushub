package com.example.campushub.activities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campushub.R;

import java.util.List;

public class KindleBookAdapter extends RecyclerView.Adapter<KindleBookAdapter.BookViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public KindleBookAdapter(Context context, List<Book> bookList, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kindle_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.tvBookTitle.setText(book.getTitle());

        // 1. Load Book Cover Image (using Glide)
        Glide.with(context)
                .load(book.getCoverUrl())
                .placeholder(R.drawable.placeholder_book_cover)
                .into(holder.imgBookCover);

        // 2. Handle Lock / Unlock State
        if (book.isLocked()) {
            holder.cardBookCover.setAlpha(0.6f); // Dim cover opacity
            holder.imgStatusBadge.setImageResource(R.drawable.ic_lock);
            holder.progressReading.setVisibility(View.GONE);
            holder.tvProgressText.setText(context.getString(R.string.book_restricted));
            holder.tvProgressText.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            holder.cardBookCover.setAlpha(1.0f);
            holder.imgStatusBadge.setImageResource(R.drawable.ic_unlock);
            holder.progressReading.setVisibility(View.VISIBLE);
            holder.progressReading.setProgress(book.getProgressPercentage());

            if (book.getProgressPercentage() == 100) {
                holder.tvProgressText.setText(context.getString(R.string.book_finished));
            } else {
                holder.tvProgressText.setText(context.getString(R.string.book_read_percentage, book.getProgressPercentage()));
            }
            holder.tvProgressText.setTextColor(Color.parseColor("#64748B"));
        }

        // 3. Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (book.isLocked()) {
                Toast.makeText(context, context.getString(R.string.book_soft_copy_locked), Toast.LENGTH_SHORT).show();
            } else {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        CardView cardBookCover;
        ImageView imgBookCover, imgStatusBadge;
        ProgressBar progressReading;
        TextView tvProgressText,tvBookTitle ;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBookCover = itemView.findViewById(R.id.cardBookCover);
            imgBookCover = itemView.findViewById(R.id.imgBookCover);
            imgStatusBadge = itemView.findViewById(R.id.imgStatusBadge);
            progressReading = itemView.findViewById(R.id.progressReading);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
        }
    }
}
