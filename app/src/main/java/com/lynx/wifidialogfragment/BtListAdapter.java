package com.lynx.wifidialogfragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WORK on 10.09.2015.
 */
public class BtListAdapter extends ArrayAdapter<BluetoothDevice> {

    private Context mCtx;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    public BtListAdapter(Context context) {
        super(context, 0, new ArrayList<BluetoothDevice>());
        mCtx = context;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) convertView = LayoutInflater.from(mCtx).inflate(R.layout.bt_list_item, parent, false);
        BluetoothDevice device = deviceList.get(position);
        TextView tvDeviceName_BLI = (TextView) convertView.findViewById(R.id.tvDeviceName_BLI);
        TextView tvStatus_BLI = (TextView) convertView.findViewById(R.id.tvStatus_BLI);
        tvDeviceName_BLI.setText(device.getName());
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                tvStatus_BLI.setText("");
                break;
            case BluetoothDevice.BOND_BONDING:
                tvStatus_BLI.setText("Pairing...");
                break;
            case BluetoothDevice.BOND_BONDED:
                tvStatus_BLI.setText("Paired");
                break;
        }
        return convertView;
    }

    /*Custom methods*/
    public void update(ArrayList<BluetoothDevice> _list) {
        deviceList.clear();
        deviceList.addAll(_list);
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device) {
        deviceList.add(device);
        notifyDataSetChanged();
    }

    public ArrayList<BluetoothDevice> getData() {
        return deviceList;
    }

}
