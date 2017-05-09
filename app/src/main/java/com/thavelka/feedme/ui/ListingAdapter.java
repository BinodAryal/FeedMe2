package com.thavelka.feedme.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thavelka.feedme.R;
import com.thavelka.feedme.models.Listing;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Listing> listings;
    private OnListingClickListener listener;

    public ListingAdapter(Context context, RealmResults<Listing> listings) {
        this.context = context;
        this.listings = listings;
    }

    public ListingAdapter(Context context, RealmResults<Listing> listings,
                          OnListingClickListener listener) {
        this.context = context;
        this.listings = listings;
        this.listener = listener;
    }

    public void setListings(RealmResults<Listing> listings) {
        this.listings = listings;
        notifyDataSetChanged();
    }

    public void setOnListingClickListener(OnListingClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_listing, parent, false);
        return new ViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (listings == null || !listings.isValid()) return;
        Listing listing = listings.get(position);
        holder.configure(listing);
        holder.itemView.setOnClickListener((listener != null && listing != null)
                ? v -> listener.onClickListing(listing) : null);
    }

    @Override
    public int getItemCount() {
        if (listings == null || !listings.isValid()) return 0;
        return listings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        @BindView(R.id.listing_img) ImageView image;
        @BindView(R.id.listing_text_primary) TextView primary;
        @BindView(R.id.listing_text_secondary) TextView secondary;
        @BindView(R.id.listing_text_tertiary) TextView tertiary;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.context = context;
        }

        public void configure(Listing listing) {
            if (listing == null || !listing.isValid()) return;
            if (listing.place != null) {
                byte[] thumbnailBytes = listing.place.thumbnail;
                if (thumbnailBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                    image.setImageBitmap(bitmap);
                }
                primary.setText(listing.place.name);
                secondary.setText(listing.place.address);
            }
            tertiary.setText(listing.description);
        }
    }

    public interface OnListingClickListener {
        void onClickListing(Listing listing);
    }
}
