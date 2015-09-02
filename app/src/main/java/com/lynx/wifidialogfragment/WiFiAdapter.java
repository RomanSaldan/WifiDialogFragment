package com.lynx.wifidialogfragment;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WORK on 02.09.2015.
 */
public class WiFiAdapter extends ArrayAdapter<ScanResult> {

    private ArrayList<ScanResult> sr = new ArrayList<>();
    private Context mCtx;

    public WiFiAdapter(Context context, ArrayList<ScanResult> sr) {
        super(context, 0, sr);
        mCtx = context;
    }

    public void update(ArrayList<ScanResult> _sr){
        sr.clear();
        sr.addAll(_sr);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sr.size();
    }

    @Override
    public ScanResult getItem(int position) {
        return sr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScanResult sc = sr.get(position);
        if(convertView == null) convertView = LayoutInflater.from(mCtx).inflate(R.layout.row, parent, false);
        TextView tvName_R = (TextView) convertView.findViewById(R.id.tvName_R);
        TextView tvType_R = (TextView) convertView.findViewById(R.id.tvType_R);
        tvName_R.setText(sc.SSID);
        tvType_R.setText(getConnType(sc));
        return convertView;
    }

    /*Define type of wifi network*/
    private String getConnType(ScanResult _sc) {
        String capabilities = _sc.capabilities;
        if (capabilities.toUpperCase().contains("WEP")) {
            return "WEP";
        } else if (capabilities.toUpperCase().contains("WPA") || capabilities.toUpperCase().contains("WPA2")) {
            return "WPA/WPA2";
        } else {
            return "OPEN";
        }
    }

}
