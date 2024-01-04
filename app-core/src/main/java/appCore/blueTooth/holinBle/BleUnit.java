package appCore.blueTooth.holinBle;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import com.hoin.btsdk.BluetoothService;

import java.util.Set;

public class BleUnit {
    private BluetoothService mService;
    private Context mContext;
    private PrinterCallback mCallBack;
    private boolean mConnected = false;
    private Handler mHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {

            if (BleUnit.this.mCallBack != null) {
                switch (msg.what) {
                    case 1:
                        switch (msg.arg1) {
                            case 1:
                                BleUnit.this.mCallBack.onState(1);
                            case 0:
                                BleUnit.this.mCallBack.onState(0);
                                return;
                            case 2:
                                BleUnit.this.mCallBack.onState(2);
                                return;
                            case 3:
                                System.err.println("device has connected.");
                                BleUnit.this.mConnected = true;
                                BleUnit.this.mCallBack.onState(3);
                                return;
                            default:
                                return;
                        }
                    case 2:
                        byte[] data = (byte[]) ((byte[]) msg.obj);
                        BleUnit.this.mCallBack.onEvent(new PrinterEvent(3, data));
                    case 3:
                    case 4:
                    default:
                        break;
                    case 5:
                        System.err.println("device has disconnected.");
                        BleUnit.this.mConnected = false;
                        BleUnit.this.mCallBack.onError(1002);
                        break;
                    case 6:
                        System.err.println("unable connect to device.");
                        BleUnit.this.mCallBack.onError(1001);
                }

            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleUnit.this.mCallBack != null) {
                if ("android.bluetooth.device.action.FOUND".equals(action)) {
                    BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                    PrinterEvent event = new PrinterEvent(1, device);
                    BleUnit.this.mCallBack.onEvent(event);
                    System.err.println("find bluetooth device, mac: " + device.getAddress() + ", name: " + device.getName());
                } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                    BleUnit.this.mCallBack.onEvent(new PrinterEvent(2, (Object) null));
                    System.err.println("finished discovery bluetooth.");
                }

            }
        }
    };

    public BleUnit(Context context, PrinterCallback cb) {
        this.mContext = context;
        this.mCallBack = cb;
        this.mService = new BluetoothService(this.mContext, this.mHandler);
        if (!this.mService.isAvailable()) {
            System.err.println("bluetooth not avalible.");
            if (this.mCallBack != null) {
                this.mCallBack.onError(1000);
            }

        } else {
            this.openBt();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.bluetooth.device.action.FOUND");
            filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
    }

    public void destroy() {
        try {
            this.mContext.unregisterReceiver(this.mReceiver);
            if (this.mService != null) {
                this.mService.stop();
            }

            this.mService = null;
        }catch (Exception e){

        }
    }

    public void openBt() {
        if (!this.mService.isBTopen()) {
            Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            if (this.mContext instanceof Activity) {
                ((Activity) this.mContext).startActivityForResult(enableIntent, 2);
            } else {
                if (this.mCallBack == null) {
                    return;
                }

                System.err.println("Context must be activity");
                this.mCallBack.onError(1003);
            }
        }

    }

    private void closeBt() {
        if (this.mService.isBTopen()) {
            Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            if (this.mContext instanceof Activity) {
                ((Activity) this.mContext).startActivityForResult(enableIntent, 1);
            } else {
                if (this.mCallBack == null) {
                    return;
                }

                System.err.println("Context must be activity");
                this.mCallBack.onError(1003);
            }
        }

    }

    public void startDiscovery() {
        if (this.mService.isDiscovering()) {
            this.mService.cancelDiscovery();
        }

        this.mService.startDiscovery();
    }

    public void cancleDiscovery() {
        if (this.mService.isDiscovering()) {
            this.mService.cancelDiscovery();
        }

    }

    public Set<BluetoothDevice> getPairedDevice() {
        return this.mService != null ? this.mService.getPairedDev() : null;
    }

    public void connect(String macAddress) {
        this.mService.cancelDiscovery();
        BluetoothDevice con_dev = this.mService.getDevByMac(macAddress);
        try {
            this.mService.connect(con_dev);
        } catch (Exception e) {
            try {
//                重试一次
                this.mService = new BluetoothService(this.mContext, this.mHandler);
                this.mService.connect(con_dev);
            } catch (Exception e1) {

            }
        }
    }

    public void sendData(byte[] data) {
        if (this.mService != null) {
            if (!this.mConnected) {
                this.mCallBack.onError(1009);
            } else {
                this.mService.write(data);
            }
        }
    }

    public void sendMessage(String message, String charset) {
        if (this.mService != null) {
            if (!this.mConnected) {
                this.mCallBack.onError(1009);
            } else {
                this.mService.sendMessage(message, charset);
            }
        }
    }

    public BluetoothService getService() {
        return mService;
    }
}
