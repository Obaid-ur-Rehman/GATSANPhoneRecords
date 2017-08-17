package com.record.phone.blackdatabases;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.record.phone.blackdatabases.R;

/**
 * Created by XSS on 14/08/2017.
 */

public class cnic_lookup extends AppCompatActivity implements View.OnClickListener{
    TCPClient m = null;
    Button btnSubmit = null;
    EditText cnic = null;
    static EditText output = null;
    Button btnPastCnic = null;
    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        setContentView(R.layout.cnic_lookup);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        cnic = (EditText) findViewById(R.id.inputText);
        output = (EditText) findViewById(R.id.outputText);
        btnPastCnic = (Button) findViewById(R.id.btnCopyCNIC);
        btnPastCnic.setOnClickListener(this);

        btnSubmit.setOnClickListener(this);


    }

    private void sendRequest(String info)
    {
        Intent i = new Intent(getBaseContext(), connectionService.class);
        i.putExtra("data", info);
        startService(i);
    }

    private void hideKeypad()
    {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnSubmit:
                sendPacket();
                break;
            case R.id.btnCopyCNIC:
                pasteCNIC();
                break;

        }
    }

    private void pasteCNIC()
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String cnicStr = clipboard.getText().toString();
        if(cnicStr.length() > 0)
        {
            cnic.setText(cnicStr);
        }
        else
        {
            Toast.makeText(this, "No CNIC number is present in clipboard", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendPacket()
    {
        hideKeypad();
        String cnicStr = cnic.getText().toString();
        if(cnicStr.length() == 13) {
            output.setText("");
            cnicStr = "Info," + MainActivity.getDeviceUniqueID(this) + ",c" + cnicStr + "," + getLoc();
            sendRequest(cnicStr);
        }
        else
            Toast.makeText(this, "Invalid CNIC entered", Toast.LENGTH_LONG).show();
    }

    private String getLoc()
    {
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
            String op = telephony.getNetworkOperatorName();

            if (location != null) {

                int newCid = location.getCid() & 0xffff;
                int newLac = location.getLac() & 0xffff;

                String locPacket = newCid + "," + newLac + "," + op;
                return locPacket;

            }
            else
            {
                return "unknown,unknown,unknown,";
            }
        }
        else
        {
            return "unknown,unknown,unknown,";
        }
    }




}
