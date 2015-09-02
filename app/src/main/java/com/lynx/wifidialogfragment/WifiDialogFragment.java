package com.lynx.wifidialogfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WORK on 01.09.2015.
 */
public class WifiDialogFragment extends DialogFragment {

    private Context             mCtx;
    private WifiManager         mWifi;
    private ListView            lvWifi_WDF;
    private Switch              swWifi_WDF;
    private List<ScanResult>    results;
    private WiFiAdapter         mWifiAdapter;
    private MyWifiReceiver      mReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCtx = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiAdapter = new WiFiAdapter(mCtx, new ArrayList<ScanResult>());
        mWifi = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
        mReceiver = new MyWifiReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.wifi_dialog_fragment, container, false);
        /*Init views*/
        lvWifi_WDF = (ListView) dialogView.findViewById(R.id.lvWifi_WDF);
        lvWifi_WDF.setAdapter(mWifiAdapter);
        swWifi_WDF = (Switch) dialogView.findViewById(R.id.swWifi_WDF);
        swWifi_WDF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) onWifi();
                else offWifi();
            }
        });
        lvWifi_WDF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String ssid = ((TextView) view.findViewById(R.id.tvName_R)).getText().toString();
                String type = ((TextView) view.findViewById(R.id.tvType_R)).getText().toString();
                if(type.equals("OPEN")) {
                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + ssid + "\"";
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int id = mWifi.addNetwork(conf);
                    mWifi.enableNetwork(id, true);
                    mWifi.reconnect();
                } else showPassDialog(mCtx, ssid);
            }
        });
        return dialogView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mWifi.isWifiEnabled()) {
            onWifi();
        } else offWifi();
    }

    /*Turn on wifi*/
    private void onWifi() {
        mWifi.setWifiEnabled(true);
        swWifi_WDF.setChecked(true);
        swWifi_WDF.setText("ON");
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mCtx.registerReceiver(mReceiver, iFilter);
        mWifi.startScan();
    }

    /*Turn off wifi*/
    private void offWifi() {
        mWifi.setWifiEnabled(false);
        swWifi_WDF.setChecked(false);
        swWifi_WDF.setText("OFF");
        mCtx.unregisterReceiver(mReceiver);
        mWifiAdapter.update(new ArrayList<ScanResult>());
    }

    /*Custom broadcast receiver for handle new wifi connections*/
    public class MyWifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    results = mWifi.getScanResults();
                    mWifiAdapter.update(new ArrayList<ScanResult>(results));
                    break;
            }
        }
    }

    private void showPassDialog(final Context _context, final String _ssid) {
        AlertDialog.Builder adb = new AlertDialog.Builder(_context);
        View dialogView = LayoutInflater.from(_context).inflate(R.layout.dialog_password, null);
        final EditText etPasswordDP = (EditText) dialogView.findViewById(R.id.etPassword_DP);
        adb.setView(dialogView);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // try connect to wifi
                String wifiPass = etPasswordDP.getText().toString();
                if(TextUtils.isEmpty(wifiPass)) Toast.makeText(_context, "Enter pass", Toast.LENGTH_SHORT).show();
                else {
                    // HERE WORK WITH PASS
                    WifiConfiguration wifiConfiguration = new WifiConfiguration();
                    wifiConfiguration.SSID = String.format("\"%s\"", _ssid);
                    wifiConfiguration.preSharedKey = String.format("\"%s\"",
                            wifiPass);

                    int netId = mWifi.addNetwork(wifiConfiguration);

                    mWifi.disconnect();
                    mWifi.enableNetwork(netId, true);
                    mWifi.reconnect();
                }
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss
                dialogInterface.dismiss();
            }
        });
        adb.setCancelable(false);
        adb.create().show();
    }

}
