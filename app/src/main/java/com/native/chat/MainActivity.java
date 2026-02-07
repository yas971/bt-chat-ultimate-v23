package com.native.chat;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bA;
    private BluetoothSocket bS;
    private TextView logs;
    private EditText input;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 40, 40, 40);
        logs = new TextView(this);
        logs.setText("V23 Ultimate - Prêt\n");
        l.addView(logs);
        Button btn = new Button(this);
        btn.setText("SCAN & CONNECT");
        btn.setOnClickListener(v -> connectAll());
        l.addView(btn);
        input = new EditText(this);
        l.addView(input);
        Button s = new Button(this);
        s.setText("ENVOYER");
        s.setOnClickListener(v -> send());
        l.addView(s);
        setContentView(l);
        bA = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT","android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        listen();
    }

    private void listen() {
        new Thread(() -> {
            try {
                BluetoothServerSocket s = bA.listenUsingInsecureRfcommWithServiceRecord("Chat", MY_UUID);
                bS = s.accept();
                manage();
            } catch (Exception e) {}
        }).start();
    }

    private void connectAll() {
        Set<BluetoothDevice> bonded = bA.getBondedDevices();
        for (BluetoothDevice d : bonded) {
            new Thread(() -> {
                try {
                    bS = d.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    bS.connect();
                    manage();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void manage() {
        runOnUiThread(() -> logs.append("CONNECTÉ\n"));
        try {
            InputStream is = bS.getInputStream();
            byte[] buf = new byte[1024];
            while (true) {
                int len = is.read(buf);
                String m = new String(buf, 0, len);
                runOnUiThread(() -> logs.append("Ami: " + m + "\n"));
            }
        } catch (Exception e) {}
    }

    private void send() {
        try {
            String m = input.getText().toString();
            bS.getOutputStream().write(m.getBytes());
            logs.append("Moi: " + m + "\n");
            input.setText("");
        } catch (Exception e) {}
    }
}