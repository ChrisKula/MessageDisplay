package com.christiankula.messagedisplay.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.christiankula.messagedisplay.R;

import java.util.List;

public class ColourButtonAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resource;
    private List<String> data;


    public ColourButtonAdapter(Context context, List<String> data) {
        super(context, R.layout.list_item_colour_button, data);
        this.context = context;
        this.data = data;
        this.resource = R.layout.list_item_colour_button;
    }

    public String getItem(int index) {
        return data.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resource, null);
        }
        String color = data.get(position);
        color = color.substring(2);
        color = "#" + color;
        final String f_color = color;

        if (color != null) {
            Button b = ((Button) v.findViewById(R.id.colour_button));
            b.setText(color);
            b.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
        }
        return v;
    }

}
