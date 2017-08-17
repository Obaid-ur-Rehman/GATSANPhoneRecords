package com.record.phone.blackdatabases;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.record.phone.blackdatabases.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    private static final int REQUEST_READ_PHONE_STATE = 1;
    Button btnCell, btnCnic;
    ImageButton web = null;
    static String uid = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnCell = (Button) findViewById(R.id.btnCell);
        btnCnic = (Button) findViewById(R.id.btnCnic);
        web = (ImageButton) findViewById(R.id.btnweb);
        web.setOnClickListener(this);
        uid = getDeviceUniqueID(this);
        TextView uid = (TextView) findViewById(R.id.txtUID);
        uid.setText(getDeviceUniqueID(this));
        uid.setOnClickListener(this);


        if(checkAllPermissions()) {
            btnCnic.setOnClickListener(this);
            btnCell.setOnClickListener(this);
            startServices();
        }
        else {
            allowedPermissions();
            Toast.makeText(this, "Permissions not set", Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    private void allowedPermissions()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, REQUEST_READ_PHONE_STATE);
        }

    }

    private void startServices()
    {
        Intent service = new Intent(getBaseContext(), connectionService.class);
        startService(service);
        Intent loc = new Intent(getBaseContext(), info_sender.class);
        startService(loc);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnCell:
                searchByCell();
                break;
            case R.id.btnCnic:
                searchByCnic();
                break;
            default:
                break;
            case R.id.btnweb:
                openWeb();
                break;
            case R.id.txtUID:
                Toast.makeText(this, "Fill the form to get register", Toast.LENGTH_LONG).show();
                copyUIDToClipBoard();
                break;

        }
    }

    public void searchByCell()
    {
        try {
            Intent cellSearch = new Intent(getBaseContext(), cell_lookup.class);
            startActivity(cellSearch);
        }catch (Exception e){ Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();}

    }

    public void searchByCnic()
    {
        Intent cnicSearch = new Intent(getBaseContext(), cnic_lookup.class);
        startActivity(cnicSearch);
    }

    public static String getDeviceUniqueID(Activity activity){
        String device_unique_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return device_unique_id;
    }

    private void openWeb()
    {
        copyUIDToClipBoard();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/forms/biR6B2SLueAVB7GB3"));
        startActivity(browserIntent);
    }

    private void copyUIDToClipBoard()
    {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", uid);
                clipboard.setPrimaryClip(clip);
            } else {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(uid);
            }
            Toast.makeText(getApplicationContext(), "UID copied to Clipboard", Toast.LENGTH_SHORT).show();

    }

    private boolean checkAllPermissions()
    {

        String permission = "android.permission.ACCESS_COARSE_LOCATION";
        int res = getBaseContext().checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        return false;
    }

    public void quit() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }




}



