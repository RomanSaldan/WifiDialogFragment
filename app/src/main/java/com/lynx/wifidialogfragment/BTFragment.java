package com.lynx.wifidialogfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by WORK on 09.09.2015.
 */
public class BTFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private Switch              swBT_BF;
    private TextView            tvStatus_BF;
    private Button              btnVisible_BF;
    private ImageButton         ibExit_BF;
    private ListView            lvDevices_BF;

    private BluetoothAdapter    btAdapter;
    private BtListAdapter       btListAdapter;
    private BtReceiver mBtReceibver;

    private final int REQUEST_BT_ON         = 0;
    private final int REQUEST_BT_VISIBLE    = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtReceibver = new BtReceiver();
        btListAdapter = new BtListAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.bt_fragment, container, false);
        initUI(dialogView);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return dialogView;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) onBT();
        else offBT();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BT_ON) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    swBT_BF.setChecked(true);
                    // BT is ON (for 2 min)
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    getActivity().registerReceiver(new BtReceiver(), intentFilter);
                    btAdapter.startDiscovery();
                    break;
                case Activity.RESULT_CANCELED:
                    swBT_BF.setChecked(false);
                    break;
            }
        } else if(requestCode == REQUEST_BT_VISIBLE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // Visibility request success
                    break;
                case Activity.RESULT_CANCELED:
                    // Visibility request deny
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnVisible_BF:
                // handle button VISIBLE
                if(btAdapter.isEnabled()) goVisible();
                else Toast.makeText(getActivity(), "Turn on Bluetooth first", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ibExit_BF:
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BluetoothDevice device = btListAdapter.getItem(i);
        try {
            tvStatus_BF.setText("Pairing to " + device.getName());
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            tvStatus_BF.setText("");
        } catch (Exception e) {
            Log.e("myLogs", e.getMessage());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final BluetoothDevice device = btListAdapter.getItem(i);
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setMessage("Unpair from " + device.getName())
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(device, (Object[]) null);
                        } catch (Exception e) {
                            Log.e("myLogs", e.getMessage());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        Dialog d = adb.create();
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
        return true;
    }

    /*Init UI*/
    private void initUI(View dialogView) {
        swBT_BF             = (Switch)      dialogView.findViewById(R.id.swBT_BF);
        tvStatus_BF         = (TextView)    dialogView.findViewById(R.id.tvStatus_BF);
        btnVisible_BF       = (Button)      dialogView.findViewById(R.id.btnVisible_BF);
        ibExit_BF           = (ImageButton) dialogView.findViewById(R.id.ibExit_BF);
        lvDevices_BF        = (ListView)    dialogView.findViewById(R.id.lvDevices_BF);

        swBT_BF         .setOnCheckedChangeListener(this);
        if(btAdapter.isEnabled())
            swBT_BF     .setChecked(true);
        btnVisible_BF   .setOnClickListener(this);
        ibExit_BF       .setOnClickListener(this);
        ibExit_BF       .setImageDrawable(getResources().getDrawable(R.drawable.button_exit));
        lvDevices_BF    .setAdapter(btListAdapter);
        lvDevices_BF    .setOnItemClickListener(this);
        lvDevices_BF    .setOnItemLongClickListener(this);
    }

    /*Turn on Bluetooth*/
    private void onBT() {
        Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(btIntent, REQUEST_BT_ON);
    }

    /*Turn off Bluetooth*/
    private void offBT() {
        if(btAdapter.isEnabled()) btAdapter.disable();
        btListAdapter.update(new ArrayList<BluetoothDevice>());

    }

    /*Make device visible for 120 seconds*/
    private void goVisible() {
        Intent intentVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(intentVisible, REQUEST_BT_VISIBLE);
    }

    /*Broadcast Receiver that handle Bluetooth events*/
    private class BtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(!btListAdapter.getData().contains(device))btListAdapter.addDevice(device);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    btListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

}
