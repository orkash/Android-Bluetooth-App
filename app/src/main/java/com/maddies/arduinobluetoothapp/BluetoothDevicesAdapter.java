package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView address;
    }

    public BluetoothDevicesAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_bluetooth_device, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.address = (TextView) convertView.findViewById(R.id.device_address);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        viewHolder.name.setText(device.getName());
        viewHolder.address.setText(device.getAddress());
        // Return the completed to render on screen
        return convertView;
    }
}
