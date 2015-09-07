package com.lynx.wifidialogfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WORK on 03.09.2015.
 */
public class WifiFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemClickListener, DialogInterface.OnClickListener, AdapterView.OnItemLongClickListener {

    private Context                     mCtx;

    private Switch                      swWifi_WF;
    private ImageButton                 ibExit_WF;
    private ProgressBar                 pbStatus_WF;
    private TextView                    tvStatus_WF;
    private ListView                    lvNetworks_WF;

    private WifiManager                 mWifiManager;
    private WifiListAdapter             mWifiListAdapter;
    private List<ScanResult>            mNetworkList;
    private WifiReceiver                mWifiReceiver;
    private SharedPreferences           mSharedPreferences;
    private SharedPreferences.Editor    editor;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCtx = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiListAdapter    = new WifiListAdapter(mCtx);
        mWifiManager        = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver       = new WifiReceiver();
        mSharedPreferences  = mCtx.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor              = mSharedPreferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.wifi_fragment, container, false);
        initUI(dialogView);
//        lvNetworks_WF.setAdapter(mWifiListAdapter);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return dialogView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mWifiManager.isWifiEnabled()) onWiFi();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) onWiFi();
        else offWifi();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibExit_WF:
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // get SSID & Type of network and connect if network require password
        String ssid = ((TextView) view.findViewById(R.id.tvNetworkName_WLI)).getText().toString();
        Drawable lock = ((ImageView) view.findViewById(R.id.ivLock_WLI)).getDrawable();
        if(lock.getConstantState().equals(mCtx.getResources().getDrawable(R.drawable.wf_lock).getConstantState())) {
            if(mSharedPreferences.contains(ssid)) {
                String pass = mSharedPreferences.getString(ssid, "");
                connectToLockedNetwork(ssid, pass);
            } else
                displayPassDialog(ssid);
        } else {
            connectToOpenNetwork(ssid);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final String ssid = ((TextView) view.findViewById(R.id.tvNetworkName_WLI)).getText().toString();
        AlertDialog.Builder adb = new AlertDialog.Builder(mCtx)
                .setMessage("Forget network " + ssid + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(mSharedPreferences.contains(ssid)) mSharedPreferences.edit().remove(ssid).commit();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
        adb.create().show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                String ssid = ((TextView) ((Dialog) dialogInterface).findViewById(R.id.tvNetworkName_PD)).getText().toString();
                String pass = ((EditText) ((Dialog) dialogInterface).findViewById(R.id.etPass_PD)).getText().toString();
                if(pass.length()<8) {
                    dialogInterface.dismiss();
                    displayPassDialog(ssid);
                    Toast.makeText(mCtx, "Password required 8 symbols or more", Toast.LENGTH_SHORT).show();
                    break;
                }
                CheckBox cb = ((CheckBox) ((Dialog)dialogInterface).findViewById(R.id.cbSavePassword_PD));
                if(cb.isChecked()) savePassword(ssid, pass);   // here save pass to SP
                connectToLockedNetwork(ssid, pass);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialogInterface.dismiss();
                break;
        }
    }

    /*-------------------------------------------------------*/
    /* ---------------- Utility methods -------------------- */
    /*-------------------------------------------------------*/

    /*Save network password to shared preferences*/
    private void savePassword(String _ssid, String _pass) {
        editor.putString(_ssid, _pass);
    }

    private void initUI(View dialogView) {
        swWifi_WF       = (Switch) dialogView.findViewById(R.id.swWifi_WF);
        ibExit_WF       = (ImageButton) dialogView.findViewById(R.id.ibExit_WF);
        pbStatus_WF     = (ProgressBar) dialogView.findViewById(R.id.pbStatus_WF);
        tvStatus_WF     = (TextView) dialogView.findViewById(R.id.tvStatus_WF);
        lvNetworks_WF   = (ListView) dialogView.findViewById(R.id.lvNetworks_WF);

        swWifi_WF           .setOnCheckedChangeListener(this);
        swWifi_WF           .setChecked(false);
        ibExit_WF           .setImageDrawable(getResources().getDrawable(R.drawable.button_exit));
        ibExit_WF           .setOnClickListener(this);
        pbStatus_WF         .setIndeterminate(true);
        lvNetworks_WF       .setAdapter(mWifiListAdapter);
        lvNetworks_WF       .setOnItemClickListener(this);
        lvNetworks_WF       .setOnItemLongClickListener(this);
    }

    private void onWiFi() {
        mWifiManager.setWifiEnabled(true);
        swWifi_WF.setChecked(true);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        iFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mCtx.registerReceiver(mWifiReceiver, iFilter);
        mWifiManager.startScan();
        tvStatus_WF.setVisibility(View.VISIBLE);
        pbStatus_WF.setVisibility(View.VISIBLE);
        tvStatus_WF.setText("Scanning");
    }

    private void offWifi() {
        mWifiManager.setWifiEnabled(false);
        swWifi_WF.setChecked(false);
        mCtx.unregisterReceiver(mWifiReceiver);
        mWifiListAdapter.update(new ArrayList<ScanResult>());
        tvStatus_WF.setVisibility(View.INVISIBLE);
        pbStatus_WF.setVisibility(View.INVISIBLE);
        tvStatus_WF.setText("");
    }

    private void displayPassDialog(String _ssid) {
        AlertDialog.Builder adb = new AlertDialog.Builder(mCtx);
        View        dialogView          = LayoutInflater.from(mCtx).inflate(R.layout.pass_dialog, null);
        TextView    tvNetworkName_PD    = (TextView) dialogView.findViewById(R.id.tvNetworkName_PD);
        tvNetworkName_PD.setText(_ssid);
        adb.setView(dialogView);
        adb.setCancelable(false);
        adb.setPositiveButton("OK", this);
        adb.setNegativeButton("Cancel", this);
        adb.create().show();
    }

    private void connectToLockedNetwork(String _ssid, String _pass) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", _ssid);
        wifiConfiguration.preSharedKey = String.format("\"%s\"", _pass);

        int netId = mWifiManager.addNetwork(wifiConfiguration);

        mWifiManager.disconnect();
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect();
    }

    private void connectToOpenNetwork(String _ssid) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + _ssid + "\"";    // tak nada
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int id = mWifiManager.addNetwork(conf);
        mWifiManager.enableNetwork(id, true);
        mWifiManager.reconnect();
    }

    /*Custom broadcast receiver for handle wifi state*/
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:     // update list view with scan values
                    mNetworkList = mWifiManager.getScanResults();
                    mWifiListAdapter.update(new ArrayList<>(mNetworkList));
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
                    switch (supl_state) {
                        case SCANNING: // scanning
                            tvStatus_WF.setVisibility(View.VISIBLE);
                            tvStatus_WF.setText("Scanning");
                            pbStatus_WF.setVisibility(View.VISIBLE);
                            mWifiListAdapter.notifyDataSetChanged();
                            break;
                        case ASSOCIATING: // connecting
                            tvStatus_WF.setVisibility(View.VISIBLE);
                            tvStatus_WF.setText("Connecting");
                            pbStatus_WF.setVisibility(View.VISIBLE);
                            mWifiListAdapter.notifyDataSetChanged();
                            break;
                        case COMPLETED: // connected
                            tvStatus_WF.setVisibility(View.INVISIBLE);
                            tvStatus_WF.setText("Connected");
                            pbStatus_WF.setVisibility(View.INVISIBLE);
                            editor.commit();
                            mWifiListAdapter.notifyDataSetChanged();
                            break;
                    }
                    int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    if(supl_error == WifiManager.ERROR_AUTHENTICATING) {    // wrong pass
                        pbStatus_WF.setVisibility(View.INVISIBLE);
                        tvStatus_WF.setVisibility(View.VISIBLE);
                        tvStatus_WF.setText("Wrong password");
                        editor.clear().commit();
                        mWifiListAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }
}