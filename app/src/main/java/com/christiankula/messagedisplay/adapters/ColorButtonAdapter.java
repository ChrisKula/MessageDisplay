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
import com.christiankula.messagedisplay.models.ColorModel;

import java.util.List;

public class ColorButtonAdapter extends ArrayAdapter<ColorModel> {

    private Context context;
    private int resource;
    private List<ColorModel> data;


    public ColorButtonAdapter(Context context, List<ColorModel> data) {
        super(context, R.layout.list_item_colour_button, data);
        this.context = context;
        this.data = data;
        this.resource = R.layout.list_item_colour_button;
    }

    public ColorModel getItem(int index) {
        return data.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resource, null);
        }
        ColorModel colorModel = data.get(position);

        String color = colorModel.getHexColor();

        if (color != null) {
            Button b = ((Button) v.findViewById(R.id.colour_button));
            b.setText(colorModel.getName());

            b.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
        }
        return v;
    }
}
