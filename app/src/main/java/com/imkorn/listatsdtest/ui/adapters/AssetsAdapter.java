package com.imkorn.listatsdtest.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imkorn.listatsdtest.R;
import com.imkorn.listatsdtest.databinding.ItemAssetBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by imkorn on 23.09.17.
 */
public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.AssetHolder> {

    // Util
    @NonNull
    private LayoutInflater inflater;

    // Listeners
    @Nullable
    private OnItemClickListener onItemClickListener;

    // Data
    @NonNull
    private List<String> items = Collections.emptyList();

    public AssetsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public AssetHolder onCreateViewHolder(ViewGroup parent,
                                          int viewType) {
        return new AssetHolder(inflater.inflate(R.layout.item_asset, parent, false));
    }

    public void setItems(@Nullable Collection<String> items) {
        if (items != null) {
            this.items = new ArrayList<>(items);
        } else {
            this.items = Collections.emptyList();
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(AssetHolder holder,
                                 int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class AssetHolder extends RecyclerView.ViewHolder {

        private ItemAssetBinding binding;

        private AssetHolder(final View itemView) {
            super(itemView);
            binding = ItemAssetBinding.bind(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION &&
                        onItemClickListener != null) {
                        onItemClickListener.onClick(itemView,
                                                    position,
                                                    items.get(position));
                    }
                }
            });
        }

        public void bind(String assetName) {
            binding.setAssetName(assetName);
        }
    }

    public interface OnItemClickListener {
        void onClick(View v, int position, String assetName);
    }
}
