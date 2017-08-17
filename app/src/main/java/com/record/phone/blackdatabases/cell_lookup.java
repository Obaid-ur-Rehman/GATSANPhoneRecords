package com.record.phone.blackdatabases;

import android.content.ClipData;
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

public class cell_lookup extends AppCompatActivity implements View.OnClickListener {

    Button btnSubmit = null;
    Button btnCpy = null;
    EditText cell = null;
    static EditText output = null;
    static String cnic = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cell_lookup);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);
        output = (EditText) findViewById(R.id.outputText);
        cell = (EditText) findViewById(R.id.inputText);
        btnCpy = (Button) findViewById(R.id.btnCopyCNIC);
        btnCpy.setOnClickListener(this);
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
                copyCNICToClipBoard();


        }
    }

    private void sendPacket()
    {
        cnic = "";
        hideKeypad();
        String cellStr = cell.getText().toString();

        if(cellStr.length() == 10) {
            output.setText("");
            cellStr = "Info," + MainActivity.getDeviceUniqueID(this) + ",m" + cellStr + "," + getLoc();
            sendRequest(cellStr);
        }
        else
        {
            Toast.makeText(this, "Invalid phone number entered", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyCNICToClipBoard()
    {
        if(cnic.length() > 0) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("CNIC", cnic);
                clipboard.setPrimaryClip(clip);
            } else {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(cnic);
            }
            Toast.makeText(getApplicationContext(), "CNIC copied to Clipboard", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "No CNIC number saved", Toast.LENGTH_SHORT).show();
        }
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

    public static void showOutput(String out)
    {
        String[] parts = out.split(",");
        cnic = parts[2];
        String outputMsg = "Name: " + parts[1] + "\n\nCNIC: " + parts[2] + "\n\nSearch Completed";
        output.setText(outputMsg);
    }

}
