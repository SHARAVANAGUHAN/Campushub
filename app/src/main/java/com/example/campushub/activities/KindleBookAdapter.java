package com.example.campushub.activities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campushub.R;
import java.util.ArrayList;
import java.util.List;

public class KindleBookAdapter extends RecyclerView.Adapter<KindleBookAdapter.BookViewHolder> {
    public interface OnBookClickListener { void onBookClick(Book book); }
    public interface OnBookLongClickListener { void onBookLongClick(Book book); }

    private final Context context;
    private final List<Book> books = new ArrayList<>();
    private final OnBookClickListener listener;
    private final OnBookLongClickListener longClickListener;

    public KindleBookAdapter(Context context, List<Book> initialBooks,
                             OnBookClickListener listener, OnBookLongClickListener longClickListener) {
        this.context = context;
        this.listener = listener;
        this.longClickListener = longClickListener;
        setBooks(initialBooks);
    }

    public void setBooks(List<Book> newBooks) {
        books.clear();
        if (newBooks != null) books.addAll(newBooks);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookViewHolder(LayoutInflater.from(context).inflate(R.layout.item_kindle_book, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvBookTitle.setText(book.getTitle());
        Glide.with(holder.imgBookCover).load(book.getCoverUrl())
                .placeholder(R.drawable.placeholder_book_cover)
                .error(R.drawable.placeholder_book_cover).centerCrop().into(holder.imgBookCover);
        holder.cardBookCover.setAlpha(book.isLocked() ? 0.62f : 1f);
        holder.imgStatusBadge.setImageResource(book.isLocked() ? R.drawable.ic_lock : R.drawable.ic_unlock);
        holder.imgStatusBadge.setContentDescription(context.getString(book.isLocked()
                ? R.string.book_access_locked : R.string.book_access_available));
        holder.progressReading.setVisibility(book.isLocked() ? View.GONE : View.VISIBLE);
        holder.progressReading.setProgress(book.getProgressPercentage());
        if (book.isLocked()) {
            holder.tvProgressText.setText(R.string.book_restricted);
            holder.tvProgressText.setTextColor(Color.parseColor("#94A3B8"));
        } else if (book.getProgressPercentage() == 100) {
            holder.tvProgressText.setText(R.string.book_finished);
            holder.tvProgressText.setTextColor(Color.parseColor("#0F766E"));
        } else {
            holder.tvProgressText.setText(context.getString(R.string.book_read_percentage, book.getProgressPercentage()));
            holder.tvProgressText.setTextColor(Color.parseColor("#64748B"));
        }
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onBookClick(book); });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener == null) return false;
            longClickListener.onBookLongClick(book);
            return true;
        });
    }

    @Override public int getItemCount() { return books.size(); }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        final CardView cardBookCover;
        final ImageView imgBookCover, imgStatusBadge;
        final ProgressBar progressReading;
        final TextView tvProgressText, tvBookTitle;
        BookViewHolder(@NonNull View itemView) {
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
