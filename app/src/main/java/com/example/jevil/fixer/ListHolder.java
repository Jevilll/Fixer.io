package com.example.jevil.fixer;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


class ListHolder extends RecyclerView.ViewHolder{

    TextView tvCurrency, tvValue;
    private CardView cv;

    ListHolder(View itemView) {
        super(itemView);
        tvCurrency = (TextView) itemView.findViewById(R.id.tvCurrency);
        tvValue = (TextView) itemView.findViewById(R.id.tvValue);
        cv = (CardView) itemView.findViewById(R.id.cv);
    }
}