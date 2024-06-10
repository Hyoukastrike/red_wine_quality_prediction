package com.example.redwinequality.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.redwinequality.R;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] items;

    public CustomSpinnerAdapter(Context context, String[] items) {
        super(context, R.layout.spinner_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        TextView tvText = view.findViewById(R.id.tv);
        TextView tvHint = view.findViewById(R.id.tv_hint);

        if (position == 0) {
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText("Chọn rượu"); // Hiển thị hint text
            tvText.setVisibility(View.GONE);
        } else {
            tvHint.setVisibility(View.GONE);
            tvText.setVisibility(View.VISIBLE);
            tvText.setText(items[position - 1]); // Hiển thị tên rượu
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }


}
