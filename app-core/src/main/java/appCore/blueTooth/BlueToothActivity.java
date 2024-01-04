package appCore.blueTooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

import appCore.MainActivity;
import appCore.NativeBridge;
import appCore.R;
import appCore.blueTooth.adapter.BlueToothDeviceAdapter;

public class BlueToothActivity extends Activity  {

    private BluetoothAdapter bTAdatper;
    private ListView listView;
    private BlueToothDeviceAdapter adapter;

    private TextView text_state;
    private TextView text_connect_info;
    private TextView text_msg;

    private final int BUFFER_SIZE = 1024;
    private static final String PIN = "1234";
    private static final String NAME = "BT_DEMO";
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private TextView btDeviceScan = null;
    public static final int REQUEST_ENABLE_BT = 2;




     public static BridgeWebView mwebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);
        initView();
        bTAdatper = BluetoothAdapter.getDefaultAdapter();
        initReceiver();
        acceptThread = new AcceptThread();
        acceptThread.start();

        openBlueTooth();
    }


    private void initView() {


        listView = (ListView) findViewById(R.id.bluetooth_list);
        btDeviceScan = (TextView) findViewById(R.id.btBluetoothScan);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bTAdatper.isDiscovering()) {
                    bTAdatper.cancelDiscovery();
                }
                BluetoothDevice device = adapter.getItem(position);
                //连接设备
                connectDevice(device);
            }
        });
        btDeviceScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();
            }
        });
    }

    private void initReceiver() {
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //避免重复添加已经绑定过的设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // Add the name and address to an array adapter to show in a ListView
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "开始搜索", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "搜索完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_openBT:
//                openBlueTooth();
//                break;
//            case R.id.btn_search:
//                searchDevices();
//                break;
//            case R.id.btn_connect_serial_channel:
//                if (connectThread != null) {
//                    connectThread.write("这是蓝牙发送过来的消息");
//                }
//                break;
//        }
//    }

    /**
     * 开启蓝牙
     *
     */
    private void openBlueTooth() {
        if (bTAdatper == null) {
            Toast.makeText(this, "当前设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
        }
        if (!bTAdatper.isEnabled()) {
           /* Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);*/
            bTAdatper.enable();

            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,
                    REQUEST_ENABLE_BT);

        }else{
            getBoundedDevices();
        }

    }

    /**
     * 搜索已配对蓝牙设备
     */
    private void searchDevices() {
        if (bTAdatper.isDiscovering()) {
            bTAdatper.cancelDiscovery();
        }
        getBoundedDevices();
        bTAdatper.startDiscovery();
    }

    /**
     * 获取已经配对过的设备
     */
    private void getBoundedDevices() {
        //获取已经配对过的设备
        Set<BluetoothDevice> pairedDevices = bTAdatper.getBondedDevices();
        //将其添加到设备列表中
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }

    /**
     * 连接蓝牙设备
     */
    private void connectDevice(BluetoothDevice btDevice) {

//        text_state.setText(getResources().getString(R.string.connecting));

        try {
            //通过工具类ClsUtils,调用createBond方法
            ClsUtils.createBond(btDevice.getClass(), btDevice);
            //1.确认配对
//            ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);
            //2.调用setPin方法进行配对...
            boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, PIN);
//            text_connect_info.setText("已配对设备：" + btDevice.getName() + "，匹配结果：" + (ret ? "成功" : "失败"));
            Toast.makeText(getApplicationContext(), "已配对设备：" + btDevice.getName() + "，匹配结果：" + (ret ? "成功" : "失败"), Toast.LENGTH_SHORT).show();

            //创建Socket
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            // BT_UUID is the app's UUID string, also used by the server code
            BluetoothSocket socket = btDevice.createRfcommSocketToServiceRecord(BT_UUID);
            //启动连接线程
            connectThread = new ConnectThread(socket, true);
            connectThread.start();

            if(ret){
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        //取消搜索
//        if (bTAdatper != null && bTAdatper.isDiscovering()) {
//            bTAdatper.cancelDiscovery();
//        }
        //注销BroadcastReceiver，防止资源泄露
//        unregisterReceiver(mReceiver);
//        if (null != acceptThread)
//            acceptThread.cancel();
//        if (null != connectThread)
//            connectThread.cancel();
    }

    /**
     * 连接线程
     */
    private class ConnectThread extends Thread {

        private BluetoothSocket mmSocket;
        private boolean activeConnect;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private ConnectThread(BluetoothSocket socket, boolean connect) {
            mmSocket = socket;
            this.activeConnect = connect;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                System.out.println("异常数据");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            bTAdatper.cancelDiscovery();
            try {
                //如果是自动连接 则调用连接方法
                if (activeConnect) {
                    mmSocket.connect();
                }
//
                // Do work to manage the connection (in a separate thread)
                byte[] buffer = new byte[BUFFER_SIZE];// buffer store for the stream
                int bytes;// bytes returned from read()
                while (true) {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    if (bytes > 0) {
                        final byte[] data = new byte[bytes];
                        System.arraycopy(buffer, 0, data, 0, bytes);


                        String tempData =    new String(data);

                        if(tempData==null){
                            return;
                        }

                        String  tempNewData ="0.00";
                        if(tempData.indexOf("kg")!=-1){
                            if(tempData.indexOf(".")+3>tempData.length()){
                                return;
                            }

                            //                         获取有效重量
                            String  newData = tempData.substring(0,tempData.indexOf(".")+3);

//                            System.out.println("电子秤data"+newData);


                            try {
                                if(newData!=null && !newData.equals(" ")){
                                    newData = newData.replaceAll("[a-zA-Z]", "");
                                    for (int i = 0; i <newData.length() ; i++) {
                                        if(newData.startsWith("0")&&!newData.startsWith("0.")){
                                            newData=newData.substring(1);
                                        }
                                    }
                                    tempNewData =newData;


                                }else{
                                    tempNewData ="0.00";
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }else{
                            tempNewData=tempData;
                        }


//                        System.out.println("电子秤发送前重量"+tempNewData);
                       if(isDouble(tempNewData)){
                           tempNewData =String.valueOf(Double.parseDouble(tempNewData)*1000);
                           Handler handler = new Handler(Looper.getMainLooper());
                           final String finalNewData = tempNewData;
                           handler.post(new Runnable() {
                               @Override
                               public void run() {
                                   System.out.println("电子秤发送重量"+finalNewData);
                                   mwebView.loadUrl("javascript:$getWeight('" + finalNewData + "')");
                               }
                           });
                       }





                    }
                    try {
                        sleep(1000l);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                System.out.println("电子秤connectException"+connectException);
                try {
                    mmSocket.close();
                    System.out.println("电子秤 mmSocket.close()");
                    //如果是自动连接 则调用连接方法
                    if (activeConnect) {
                        mmSocket.connect();
                    }
                } catch (IOException closeException) {
                }
                return;
            }
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        /**
         * Call this from the main activity to send data to the remote device
         *
         * @param msg
         */
        public void write(final String msg) {
            byte[] bytes = msg.getBytes();
            if (mmOutStream != null) {
                try {
                    //发送数据
                    mmOutStream.write(bytes);
                    text_msg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.send_msgs), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    text_msg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.send_msg_error) + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    }

    /**
     * 监听线程
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bTAdatper.listenUsingRfcommWithServiceRecord(NAME, BT_UUID);
            } catch (IOException e) {
                System.out.println("电子秤异常"+e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            try {
                while (true) {
                    try {
//                        socket = mmServerSocket.accept();

                        if(mmServerSocket!=null){
                            socket = mmServerSocket.accept();
                        }else{
                            System.out.println("电子秤kong异常");
                        }

                    } catch (IOException e) {
                        System.out.println("电子秤ko异常"+e);

                        break;
                    }
                    // If a connection was accepted
                    if (socket != null) {
                        // Do work to manage the connection (in a separate thread)
                        connectThread = new ConnectThread(socket, false);
                        connectThread.start();
                        mmServerSocket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }



    /**
     * 格式化双精度数据
     * <p>
     * 1234567.123456
     *
     * @param d
     * @return 1, 234, 567.12
     */
    public static String getCurrencyDecimalByFormat(double d) {
        try {
            BigDecimal input = new BigDecimal(d);
            if (input.scale() > 2) {
                input = input.setScale(2, RoundingMode.HALF_UP);
            }
            DecimalFormat _df = new DecimalFormat("####,##0.00");
            return _df.format(input);
        } catch (Exception e) {
            e.printStackTrace();
            return "0.00";
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // bluetooth is opened
                getBoundedDevices();
            } else {
                Toast.makeText(getBaseContext(), "蓝牙未开启", Toast.LENGTH_LONG).show();

            }
        }
    }


    public static boolean isDouble(String str) {


        try {
//            str =str.substring(0,str.indexOf(".")+3);
            Double.parseDouble(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}

