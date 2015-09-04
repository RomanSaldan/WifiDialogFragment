package com.lynx.wifidialogfragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WORK on 03.09.2015.
 */
public class WifiListAdapter extends ArrayAdapter<ScanResult> {

    private Context mCtx;
    private ArrayList<ScanResult> mNetworksList = new ArrayList<>();

    public WifiListAdapter(Context context) {
        super(context, 0, new ArrayList<ScanResult>());
        mCtx = context;
    }

    @Override
    public int getCount() {
        return mNetworksList.size();
    }

    @Override
    public ScanResult getItem(int position) {
        return mNetworksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) convertView = LayoutInflater.from(mCtx).inflate(R.layout.wifi_list_item, parent, false);
        ScanResult  currentNetwork          = mNetworksList.get(position);

        ImageView   ivSygnal_WLI            = (ImageView) convertView.findViewById(R.id.ivSygnal_WLI);
        TextView    tvNetworkName_WLI       = (TextView) convertView.findViewById(R.id.tvNetworkName_WLI);
        TextView    tvConnected_WLI         = (TextView) convertView.findViewById(R.id.tvConnected_WLI);
        ImageView   ivLock_WLI              = (ImageView) convertView.findViewById(R.id.ivLock_WLI);

        ivSygnal_WLI        .setImageDrawable(getWifiPower(currentNetwork));
        tvNetworkName_WLI   .setText(getWifiName(currentNetwork));
        tvConnected_WLI     .setText(getWifiState(currentNetwork));
        ivLock_WLI          .setImageDrawable(getWifiLock(currentNetwork));

        return convertView;
    }

    /*Update adapter with new data*/
    public void update(ArrayList<ScanResult> _list) {
        mNetworksList.clear();
        mNetworksList.addAll(_list);
        notifyDataSetChanged();
    }

    /* ------------- Methods for build list item* ------------- */

    private Drawable getWifiPower(ScanResult sr) {
        Drawable result = null;
        int level = WifiManager.calculateSignalLevel(sr.level, 5);
        switch (level) {
            case 0:
                result = mCtx.getResources().getDrawable(R.drawable.wf1);
                break;
            case 1:
                result = mCtx.getResources().getDrawable(R.drawable.wf2);
                break;
            case 2:
                result = mCtx.getResources().getDrawable(R.drawable.wf3);
                break;
            case 3:
                result = mCtx.getResources().getDrawable(R.drawable.wf4);
                break;
            case 4:
                result = mCtx.getResources().getDrawable(R.drawable.wf5);
                break;
        }
        return result;
    }

    private String getWifiName(ScanResult sr) {
        return sr.SSID;
    }

    private String getWifiState(ScanResult scanResult) {
        String current = "\"" + scanResult.SSID + "\"";
        if(current.equals(getWifiName())) return "Connected";
        return "";
    }

    public String getWifiName() {
        WifiManager manager = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }


    private Drawable getWifiLock(ScanResult sr) {
        String capabilities = sr.capabilities;
        if (capabilities.toUpperCase().contains("WEP")) {
            return mCtx.getResources().getDrawable(R.drawable.wf_lock);
        } else if (capabilities.toUpperCase().contains("WPA") || capabilities.toUpperCase().contains("WPA2")) {
            return mCtx.getResources().getDrawable(R.drawable.wf_lock);
        } else {
            return mCtx.getResources().getDrawable(R.drawable.wf_unlock);
        }
    }


}
