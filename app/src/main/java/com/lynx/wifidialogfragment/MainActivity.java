package com.lynx.wifidialogfragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity implements View.OnClickListener {

    Button btnShowDialog_AM;
    Button btnHideNavBar_AM;
    Button btnShowNavBar_AM;
    Button btnFragmentWifi_AM;
    Button btnFragmentBT_AM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ============ Init UI ============= */

        btnShowDialog_AM = (Button) findViewById(R.id.btnSystemWifi_AM);
        btnShowDialog_AM.setOnClickListener(this);

        btnHideNavBar_AM = (Button) findViewById(R.id.btnHideNavBar_AM);
        btnHideNavBar_AM.setOnClickListener(this);

        btnShowNavBar_AM = (Button) findViewById(R.id.btnShowNavBar_AM);
        btnShowNavBar_AM.setOnClickListener(this);

        btnFragmentWifi_AM = (Button) findViewById(R.id.btnFragmentWifi_AM);
        btnFragmentWifi_AM.setOnClickListener(this);

        btnFragmentBT_AM = (Button) findViewById(R.id.btnFragmentBT_AM);
        btnFragmentBT_AM.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())  {
            case R.id.btnFragmentWifi_AM:    // show wifi fragment
                getFragmentManager().beginTransaction().add(new WifiFragment(), "wf").commit();
                break;
            case R.id.btnFragmentBT_AM:
                getFragmentManager().beginTransaction().add(new BTFragment(), "bt").commit();
                break;
            case R.id.btnSystemWifi_AM:     // display system Wi-Fi settings activity

//                Intent i = new Intent(Settings.ACTION_SETTINGS);
//                i.putExtra(":android:show_fragment", "com.android.settings.wifi.WifiSettings");
//                i.putExtra(":android:no_headers", true);
//                i.putExtra("wifi_show_custom_button", true);
//                i.putExtra("extra_prefs_show_button_bar", true);
//                i.putExtra("extra_prefs_set_next_text", "");
//                startActivity(i);


                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                i.putExtra(":android:show_fragment", "com.android.settings.wifi.WifiSettings");
                i.putExtra(":android:no_headers", true);
                i.putExtra("wifi_show_action_bar", false);
                i.putExtra("extra_prefs_show_button_bar", true);
                startActivityForResult(i, 1);

//                startLockTask(); // REQUIRES API 21+ :(

                break;
            case R.id.btnHideNavBar_AM:     // hide navigation bar
                hideNavBar();
                break;
            case R.id.btnShowNavBar_AM:     // show navigation bar
                showNavBar();
                break;
        }
    }

    /*Hide navigation bar*/
    private void hideNavBar() {
        Process proc = null;

        String ProcID = "79"; //HONEYCOMB AND OLDER

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            ProcID = "42"; //ICS AND NEWER
        }

        try {
            proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "service call activity "+ProcID+" s16 com.android.systemui" });
        } catch (Exception e) {
            Log.d("myLogs", "Failed to kill task bar (1).");
            e.printStackTrace();
        }
        try {
            proc.waitFor();
        } catch (Exception e) {
            Log.d("myLogs", "Failed to kill task bar (2).");
            e.printStackTrace();
        }
    }

    /*Show navigation bar*/
    private void showNavBar() {
        Process proc2 = null;
        try {
            proc2 = Runtime.getRuntime().exec(new String[] { "su", "-c", "am startservice -n com.android.systemui/.SystemUIService" });
        } catch (Exception e) {
            Log.d("myLogs", "Failed to kill task bar (1).");
            e.printStackTrace();
        }
        try {
            proc2.waitFor();
        } catch (Exception e) {
            Log.d("myLogs","Failed to kill task bar (2).");
            e.printStackTrace();
        }
    }

}
