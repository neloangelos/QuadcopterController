package com.skandi.quadcoptercontroller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class QuadcopterControllerActivity extends Activity {
    private TextView angleTextView;
    private TextView powerTextView;
    private JoystickView leftJoystick;
    private JoystickView rightJoystick;
    private final static String TAG = QuadcopterControllerActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final int REQUEST_CODE = 1;
    public static final int LEFT_XPOSITION = 3;
    public static final int LEFT_YPOSITION = 0;
    public static final int RIGHT_XPOSITION = 1;
    public static final int RIGHT_YPOSITION = 2;
    public static final int LEFT_RADIO = 5;
    public static final int RIGHT_RADIO = 4;


    private String mDeviceName;
    private String mDeviceAddress;
    private ControlFrame frame;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadcopter_controller);
        frame = new ControlFrame();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        angleTextView = (TextView) findViewById(R.id.xPosition);
        leftJoystick = (JoystickView) findViewById(R.id.leftJoystickView);
        leftJoystick.setYisAutoCenter(false);
        leftJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPosition, int yPosition) {
                frame.setValue(xPosition, LEFT_XPOSITION);
                frame.setValue(yPosition, LEFT_YPOSITION);
                String tmp = "";
                for(byte x : frame.getBytes()){
                    tmp += Integer.toHexString(x&0xff).toUpperCase()+" ";
                }
                angleTextView.setText(tmp);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
        rightJoystick = (JoystickView) findViewById(R.id.rightJoystickView);
        rightJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPosition, int yPosition) {
                frame.setValue(xPosition, RIGHT_XPOSITION);
                frame.setValue(yPosition, RIGHT_YPOSITION);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quadcopter_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(QuadcopterControllerActivity.this, DeviceScanActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 1 && resultCode == 1){
            mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
       // unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        if(mConnected)
        {
            mBluetoothLeService.disconnect();
            mConnected = false;
        }
        if(mBluetoothLeService != null)
        {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        Log.d(TAG, "We are in destroy");
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
                Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                mConnected = false;
                invalidateOptionsMenu();
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
            {
                mConnected = true;
                ShowDialog();

                Log.e(TAG, "In what we need");
                invalidateOptionsMenu();
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                Log.e(TAG, "RECV DATA");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
    private void ShowDialog()
    {
        Toast.makeText(this, "连接成功，现在可以正常通信！", Toast.LENGTH_SHORT).show();
    }

}
