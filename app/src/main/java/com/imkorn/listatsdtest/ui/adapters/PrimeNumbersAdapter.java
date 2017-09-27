package com.imkorn.listatsdtest.ui.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imkorn.listatsdtest.R;
import com.imkorn.listatsdtest.databinding.ItemEvenPrimeNumberBinding;
import com.imkorn.listatsdtest.databinding.ItemOddPrimeNumberBinding;
import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by imkorn on 22.09.17.
 */

public class PrimeNumbersAdapter extends RecyclerView.Adapter<PrimeNumbersAdapter.PrimeNumberHolder> {

    private static final int EVEN_VIEW = 2;
    private static final int ODD_VIEW = 1;

    private List<PrimeNumber> items = new ArrayList<>();

    private LayoutInflater inflater;

    public PrimeNumbersAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getThreadId() % 2 == 0? EVEN_VIEW : ODD_VIEW;
    }

    @Override
    public PrimeNumberHolder onCreateViewHolder(ViewGroup parent,
                                                int viewType) {
        if (viewType == EVEN_VIEW) {
            return new EvenThreadPrimeNumberHolder(inflater.inflate(R.layout.item_even_prime_number,
                                                                    parent,
                                                                    false));
        } else {
            return new OddThreadPrimeNumberHolder(inflater.inflate(R.layout.item_odd_prime_number,
                                                                    parent,
                                                                    false));
        }
    }

    @Override
    public void onBindViewHolder(PrimeNumberHolder holder,
                                 int position) {
        holder.bind(items.get(position));
    }

    public void addItems(@Nullable Collection<PrimeNumber> items) {
        if (items != null) {
            final int size = this.items.size();
            this.items.addAll(items);
            notifyItemRangeInserted(size, items.size());
        }
    }

    public void setItems(@Nullable Collection<PrimeNumber> items) {
        if (items != null) {
            this.items = new ArrayList<>(items);
        } else {
            this.items = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public Collection<PrimeNumber> getAll() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    abstract class PrimeNumberHolder extends RecyclerView.ViewHolder {

        public PrimeNumberHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(PrimeNumber number);
    }

    private class OddThreadPrimeNumberHolder extends PrimeNumberHolder {
        private ItemOddPrimeNumberBinding binding;

        private OddThreadPrimeNumberHolder(View itemView) {
            super(itemView);
            binding = ItemOddPrimeNumberBinding.bind(itemView);
        }

        @Override
        public void bind(PrimeNumber number) {
            binding.setPrimeNumber(number);
        }
    }

    private class EvenThreadPrimeNumberHolder extends PrimeNumberHolder {
        private ItemEvenPrimeNumberBinding binding;

        private EvenThreadPrimeNumberHolder(View itemView) {
            super(itemView);
            binding = ItemEvenPrimeNumberBinding.bind(itemView);
        }

        @Override
        public void bind(PrimeNumber number) {
            binding.setPrimeNumber(number);
        }
    }
}
