package com.example.jevil.fixer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ListHolder> {

    private List<Item> items;

    ListAdapter(List<Item> items) {
        this.items = items;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ListHolder holder = new ListHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ListHolder holder, final int position) {
        holder.tvCurrency.setText(items.get(position).getCurrency());
        holder.tvValue.setText(String.valueOf(items.get(position).getValue()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}