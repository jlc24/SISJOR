package com.example.sisjor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class AlmacenListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> almacenes;

    public AlmacenListAdapter(Context context, List<String> almacenes) {
        this.context = context;
        this.almacenes = almacenes;
    }

    @Override
    public int getCount() {
        return almacenes.size();
    }

    @Override
    public Object getItem(int position) {
        return almacenes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_almacen_layout, null);
        }

        TextView nombreAlmacen = convertView.findViewById(R.id.nombreAlmacenTextView);
        CheckBox checkBoxAlmacen = convertView.findViewById(R.id.checkBoxAlmacen);

       String almacen = almacenes.get(position);

        nombreAlmacen.setText(almacen);
        //checkBoxAlmacen.setChecked(almacen.isSelected());

        return convertView;
    }
}
