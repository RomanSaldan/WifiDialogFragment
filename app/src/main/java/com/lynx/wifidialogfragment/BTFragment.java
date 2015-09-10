package com.lynx.wifidialogfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    private Button              btnScan_BF;
    private CheckBox            cbVisibility_BF;
    private ImageButton         ibExit_BF;
    private ListView            lvDevices_BF;
    private ProgressBar         pbSearch_BLI;

    private BluetoothAdapter    btAdapter;
    private BtListAdapter       btListAdapter;
    private BtReceiver          mBtReceiver;

    private final int REQUEST_BT_ON         = 0;
    private final int REQUEST_BT_VISIBLE    = 1;
    private final int REQUEST_BT_INVISIBLE  = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtReceiver = new BtReceiver();
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
        switch (compoundButton.getId()) {
            case R.id.swBT_BF:
                if(b) onBT();
                else offBT();
                break;
            case R.id.cbVisibility_BF:
                if(b) {
                    if(btAdapter.isEnabled()) {
                        compoundButton.setText("Visible");
                        goVisible();
                    }
                    else Toast.makeText(getActivity(), "Turn bluetooth on first", Toast.LENGTH_SHORT).show();
                }
                else {
                    cbVisibility_BF.setText("Invisible");
                    goInvisible();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BT_ON) {
            switch (resultCode) {
                case Activity.RESULT_OK:    // BT is ON (for 2 min)
                    swBT_BF.setChecked(true);
                    cbVisibility_BF.setEnabled(true);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                    getActivity().registerReceiver(mBtReceiver, intentFilter);
                    btAdapter.startDiscovery();
                    break;
                case Activity.RESULT_CANCELED:
                    swBT_BF.setChecked(false);
                    break;
            }
        } else if(requestCode == REQUEST_BT_VISIBLE) {  // VISIBLE
            switch (resultCode) {
                case Activity.RESULT_OK:
                    break;
                case Activity.RESULT_CANCELED:
                    cbVisibility_BF.setChecked(false);
                    break;
            }
        } else if(requestCode == REQUEST_BT_INVISIBLE) {    // INVISIBLE
            switch (resultCode){
                case Activity.RESULT_OK:
                    // im invisible
                    break;
                case Activity.RESULT_CANCELED:
                    // deny
                    cbVisibility_BF.setChecked(false);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScan_BF:
                if(btAdapter.isEnabled()) btAdapter.startDiscovery();
                break;
            case R.id.ibExit_BF:
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BluetoothDevice device = btListAdapter.getItem(i);
        pairToDevice(device);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final BluetoothDevice device = btListAdapter.getItem(i);
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setMessage("Unpair from " + device.getName() + "?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        unpairFromDevice(device);
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
        cbVisibility_BF     = (CheckBox)    dialogView.findViewById(R.id.cbVisibility_BF);
        btnScan_BF          = (Button)      dialogView.findViewById(R.id.btnScan_BF);
        ibExit_BF           = (ImageButton) dialogView.findViewById(R.id.ibExit_BF);
        lvDevices_BF        = (ListView)    dialogView.findViewById(R.id.lvDevices_BF);
        pbSearch_BLI        = (ProgressBar) dialogView.findViewById(R.id.pbSearch_BLI);

        swBT_BF         .setOnCheckedChangeListener(this);
        cbVisibility_BF .setOnCheckedChangeListener(this);
        btnScan_BF      .setOnClickListener(this);
        ibExit_BF       .setOnClickListener(this);
        ibExit_BF       .setImageDrawable(getResources().getDrawable(R.drawable.button_exit));
        lvDevices_BF    .setAdapter(btListAdapter);
        lvDevices_BF    .setOnItemClickListener(this);
        lvDevices_BF    .setOnItemLongClickListener(this);
        pbSearch_BLI    .setVisibility(View.INVISIBLE);
        cbVisibility_BF .setEnabled(false);
        if(btAdapter.isEnabled())
            swBT_BF     .setChecked(true);
    }

    /*Turn on Bluetooth*/
    private void onBT() {
        tvStatus_BF.setVisibility(View.VISIBLE);
        pbSearch_BLI.setVisibility(View.VISIBLE);
        Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(btIntent, REQUEST_BT_ON);
    }

    /*Turn off Bluetooth*/
    private void offBT() {
        if(btAdapter.isEnabled()) {
            btAdapter.disable();
            cbVisibility_BF.setEnabled(false);
        }
        btListAdapter.update(new ArrayList<BluetoothDevice>());
        tvStatus_BF.setVisibility(View.INVISIBLE);
        pbSearch_BLI.setVisibility(View.INVISIBLE);
        try {
            getActivity().unregisterReceiver(mBtReceiver);
        } catch (IllegalArgumentException e) {}
    }

    /*Make device visible for 120 seconds*/
    private void goVisible() {
        Intent intentVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(intentVisible, REQUEST_BT_VISIBLE);
    }

    /*Make device invisible for other bluetooth devices*/
    private void goInvisible() {
        Intent intentInvisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentInvisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
        startActivityForResult(intentInvisible, REQUEST_BT_INVISIBLE);
    }

    /*Pair with passed bluetooth device*/
    private void pairToDevice(BluetoothDevice _device) {
        try {
            tvStatus_BF.setText("Pairing to " + _device.getName());
            Method m = _device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(_device, (Object[]) null);
        } catch (Exception e) {
            Log.e("myLogs", e.getMessage());
        }
    }

    /*Unpair with passed bluetooth device*/
    private void unpairFromDevice(BluetoothDevice _device) {
        try {
            Method m = _device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(_device, (Object[]) null);
        } catch (Exception e) {
            Log.e("myLogs", e.getMessage());
        }
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
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    tvStatus_BF.setText("Scanning...");
                    pbSearch_BLI.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    tvStatus_BF.setText("");
                    pbSearch_BLI.setVisibility(View.INVISIBLE);
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                    switch (mode) {
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            Log.d("myLogs", "VISIBILITY: Visible");    // VISIBLE
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            Log.d("myLogs", "VISIBILITY: Invisible");    // INVISIBLE
                            break;
                    }
                    break;
            }
        }
    }

}
