package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Maarten on 5-2-2015.
 */
public class FileArrayAdapter extends ArrayAdapter<Item> {

    // View lookup cache
    private static class ViewHolder {
        TextView string;
        ImageView icon;
    }

    public FileArrayAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Item file = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_file, parent, false);
            viewHolder.string = (TextView) convertView.findViewById(R.id.file_string);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.file_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // put the image on the text view
        viewHolder.icon.setImageResource(file.icon);

        // Populate the data into the template view using the data object
        viewHolder.string.setText(file.file);
        // Return the completed to render on screen
        return convertView;
    }
}