package com.example.hellosearch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hellosearch.model.Business;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Madhav Chhura on 8/30/15.
 */
public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_MORE_BUTTON = 2;

    private MoreImagesCallback moreImagesCallback;

    public List<Business> businesses;

    private OnItemClickListener itemClickListener;

    public ImagesAdapter(List<Business> images) {
        super();
        this.businesses = images;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResourceId = R.layout.images_grid_view;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);

        ImageHolder holder = new ImageHolder(view);
        holder.root = view;
        holder.image = (ImageView) view.findViewById(R.id.images_grid_image);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_IMAGE:
                final Business business = businesses.get(position);
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(holder.root, position);
                        }
                    }
                });

                Picasso.with(holder.image.getContext()).load(business.image_url).into(holder.image);
                holder.image.setContentDescription(business.businessName);
                break;
            case TYPE_MORE_BUTTON:
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (moreImagesCallback != null) {
                            moreImagesCallback.onMoreImagesPressed();
                        }
                    }
                });
                holder.image.setImageResource(R.drawable.more);
                holder.image.setContentDescription("More");
        }
    }

    @Override
    public int getItemCount() {
        if(businesses != null) {
            if (businesses.isEmpty()) {
                return 0;
            } else {
                return businesses.size() + 1;
            }
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < businesses.size()) {
            return TYPE_IMAGE;
        } else {
            return TYPE_MORE_BUTTON;
        }
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setMoreImagesCallback(MoreImagesCallback callback) {
        this.moreImagesCallback = callback;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        public View root;
        public ImageView image;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }

    public interface MoreImagesCallback {
        void onMoreImagesPressed();
    }
}