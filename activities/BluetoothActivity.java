package com.brandon.apps.groupstudio.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.ResponseCode;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.inflaters.CloudGroupListInflater;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class BluetoothActivity extends BaseActivity {

    private ListView deviceListView;
    private ArrayAdapter<String> adapter;
    private List<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> discoveredDevices;
    private Toolbar toolbar;
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver receiver;
    private IntentFilter filter1;
    private IntentFilter filter2;
    private final UUID uuid = UUID.fromString("8ba95441-69b3-4f18-9010-e3a8e981bff1");
    private BluetoothServerThread server;
    private BluetoothClientThread client;
    private ConnectedThread ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_overlay_activity);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        deviceListView = (ListView) findViewById(R.id.object_list);
        pairedDevices = new ArrayList<BluetoothDevice>();
        discoveredDevices = new ArrayList<BluetoothDevice>();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        turnOnBluetooth();

        filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case (BluetoothDevice.ACTION_FOUND): {
                        System.out.println("device found");
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        discoveredDevices.add(device);
                        populateList();
                        break;
                    }
                    case (BluetoothAdapter.ACTION_DISCOVERY_STARTED): {

                        break;
                    }
                    case (BluetoothAdapter.ACTION_DISCOVERY_FINISHED): {
                        break;
                    }
                    case (BluetoothAdapter.ACTION_STATE_CHANGED): {
                        if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                            turnOnBluetooth();
                        }
                        break;
                    }
                }
            }
        };
        registerReceiver(receiver, filter1);
        registerReceiver(receiver, filter2);
        getPairedDevices();
        btAdapter.startDiscovery();
        populateList();

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = discoveredDevices.get(position);
                client = new BluetoothClientThread(device);
                Toast.makeText(getApplicationContext(), device.getName() + ", " + position, Toast.LENGTH_SHORT).show();
            }
        });

        final ImageButton refreshButton = (ImageButton) findViewById(R.id.new_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateList();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) client.cancel();
        if (server != null) server.cancel();
        if (ct != null) ct.cancel();
        btAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

    private void turnOnBluetooth() {
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support Bluetooth!", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ResultCode.MANAGE_CODE);
        }
    }

    private void populateList() {
        String[] names = new String[discoveredDevices.size()];
        for (int i = 0; i < discoveredDevices.size(); i++) {
            names[i] = discoveredDevices.get(i).getName() == null ? "No Name" : discoveredDevices.get(i).getName();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(discoveredDevices.get(i).getName())) {
                    names[i] += " (Paired)";
                }
            }
        }
        System.out.println("populated");
        adapter = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1, names);
        deviceListView.setAdapter(adapter);
    }

    private void getPairedDevices() {
        pairedDevices.clear();
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                pairedDevices.add(device);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (requestCode == ResultCode.CREATE_CODE) {
            server = new BluetoothServerThread();
            server.start();
        } else if (requestCode == ResultCode.DELETE_CODE) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            finish();
            return true;
        } else if (id == R.id.action_discover_start) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivityForResult(discoverableIntent, ResultCode.CREATE_CODE);
            return true;
        } else if (id == R.id.action_discover_cancel) {
            if (server != null) {
                server.cancel();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        return null;
    }

    private class BluetoothServerThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public BluetoothServerThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(getName(), uuid);
            } catch (IOException e) {
                System.out.println("Unable to connect server");
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                } catch (NullPointerException e) {
                    System.out.println("Server Socket is null");
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class BluetoothClientThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public BluetoothClientThread(BluetoothDevice d) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            device = d;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
            }
            socket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(socket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket s) {
            socket = s;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = s.getInputStream();
                tmpOut = s.getOutputStream();
            } catch (IOException e) {
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inStream.read(buffer);
                    // Send the obtained bytes to the UI activity

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // manage bt connections
        ct = new ConnectedThread(socket);
        ct.start();
    }
}
