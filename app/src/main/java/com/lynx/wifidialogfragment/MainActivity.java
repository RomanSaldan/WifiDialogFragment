package com.lynx.wifidialogfragment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnShowDialogFragment_AM;
    Button btnShowDialog_AM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowDialogFragment_AM = (Button) findViewById(R.id.btnShowDialogFragment_AM);
        btnShowDialogFragment_AM.setOnClickListener(this);

        btnShowDialog_AM = (Button) findViewById(R.id.btnShowDialog_AM);
        btnShowDialog_AM.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())  {
            case R.id.btnShowDialogFragment_AM:
                getFragmentManager().beginTransaction().add(new WifiDialogFragment(), "wifi_fragment").commit();
                break;
            case R.id.btnShowDialog_AM:

                break;
        }
    }
}
