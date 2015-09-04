package com.lynx.wifidialogfragment;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends ActivityGroup implements View.OnClickListener {

    Button btnShowDialogFragment_AM;
    Button btnShowDialog_AM;
    Button btnHideNavBar_AM;
    Button btnShowNavBar_AM;
    Button btnNewFragment_AM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ============ Init UI ============= */
        btnShowDialogFragment_AM = (Button) findViewById(R.id.btnShowDialogFragment_AM);
        btnShowDialogFragment_AM.setOnClickListener(this);

        btnShowDialog_AM = (Button) findViewById(R.id.btnShowDialog_AM);
        btnShowDialog_AM.setOnClickListener(this);

        btnHideNavBar_AM = (Button) findViewById(R.id.btnHideNavBar_AM);
        btnHideNavBar_AM.setOnClickListener(this);

        btnShowNavBar_AM = (Button) findViewById(R.id.btnShowNavBar_AM);
        btnShowNavBar_AM.setOnClickListener(this);

        btnNewFragment_AM = (Button) findViewById(R.id.btnNewFragment_AM);
        btnNewFragment_AM.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())  {
            case R.id.btnShowDialogFragment_AM:     // display custom Wi-Fi fragment
                WifiDialogFragment wifiFragment = new WifiDialogFragment();
                getFragmentManager().beginTransaction().add(wifiFragment, "tag").commit();
                break;
            case R.id.btnShowDialog_AM:     // display system Wi-Fi settings activity

//                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                i.putExtra(":android:show_fragment", "com.android.settings.wifi.WifiSettings");
//                i.putExtra(":android:no_headers", true);
//                startActivity(i);


                break;
            case R.id.btnHideNavBar_AM:     // hide navigation bar
                hideNavBar();
                break;
            case R.id.btnShowNavBar_AM:     // show navigation bar
                showNavBar();
                break;
            case R.id.btnNewFragment_AM:    // show new fragment
                getFragmentManager().beginTransaction().add(new WifiFragment(), "tag2").commit();
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
