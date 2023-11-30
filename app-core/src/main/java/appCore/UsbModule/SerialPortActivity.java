package appCore.UsbModule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android_serialport_api.SerialPort;
import top.maybesix.xhlibrary.serialport.ComPortData;
import top.maybesix.xhlibrary.serialport.SerialPortHelper;


public class SerialPortActivity  extends Activity {

    private byte[] mRevBuffer3 = new byte[100];
    private byte[] mBuffer = "12345".getBytes();//{1,2,3,4,5};
    private SerialPort uart3;
    private FileOutputStream mFileOutputStream3;
    private FileInputStream mFileInputStream3;
    private boolean run_flag = true;
    private SerialPortHelper mHelper;



    BridgeWebView mwebView;


    public void initAction(BridgeWebView webView) {

        //获取全部串口
        List<File> allSerial = getAllSerial();
        Log.e("串口", "所有串口:" + allSerial);

        if(allSerial==null){
            return;
        }


//        mHelper = initSerialPortHelper("/dev/ttyS0");
//        mHelper = initSerialPortHelper("/dev/ttyS1");



//        if(allSerial.size()>0){
//            String usbInfo = String.valueOf(allSerial.get(0));
//            mHelper = initSerialPortHelper(usbInfo);
//        }

//        获取可连接串口
        for (int i = 0; i <allSerial.size(); i++) {
            String usbInfo = String.valueOf(allSerial.get(i));
            System.out.println("选择默认串口"+usbInfo);
            //第二种使用串口的方式
             mHelper = initSerialPortHelper(usbInfo);

            System.out.println("串口状态"+mHelper);

//             if(mHelper.isOpen()){
//                 break;
//             }

        }


        mwebView=webView;

    }

    /**
     * 获取所有串口
     *
     * @return
     */
    private List<File> getAllSerial() {
        List<File> serialPorts = new ArrayList<>();
        Pattern pattern = Pattern.compile("^ttyS|^ttyUSB|^ttyACM|^ttyAMA|^rfcomm|^tty[^/]*$");
        File devDirectory = new File("/dev");

        File[] files = devDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (pattern.matcher(name).find()) {
                    serialPorts.add(file);
                    Log.e("串口", "扫描到的串口" + file);
                }
            }
        }
        return serialPorts;
    }

    /**
     * 初始化串口
     *
     * @param path 串口路径
     */
    private void initSerialPort(String path) {
        try {
            uart3 = new SerialPort(new File(path),9600,0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取输出流
        mFileOutputStream3 = (FileOutputStream) uart3.getOutputStream();
        //获取输入流
        mFileInputStream3 = (FileInputStream) uart3.getInputStream();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                final String data = (String) msg.obj;
                Log.e("串口", "扫描到的串口重量" + data);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mwebView.loadUrl("javascript:$getWeight('" + data + "')");
                    }
                });


            }
        }
    };

    /**
     * 发送串口数据
     */
    private void sendData() {
        try {
            if (mFileOutputStream3 != null) {
                mFileOutputStream3.write(mBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开机线程接收串口数据
     * 收到数据后发送的Handler刷新
     */
    private void readData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run_flag) {
                    int size3 = 0;
                    try {
                        size3 = mFileInputStream3.read(mRevBuffer3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (size3 > 0) {
                        String str3 = new String(mRevBuffer3, 0, size3);
                        Log.i("串口", "receiveData() --> " + str3);
                        mHandler.sendMessage(mHandler.obtainMessage(1, str3));
                        Arrays.fill(mRevBuffer3, (byte) 0x0);
                    }
                }
            }
        }).start();

//        new Thread(() -> {
//            while (run_flag) {
//                int size3 = 0;
//                try {
//                    size3 = mFileInputStream3.read(mRevBuffer3);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (size3 > 0) {
//                    String str3 = new String(mRevBuffer3, 0, size3);
//                    Log.i("串口", "receiveData() --> " + str3);
//                    mHandler.sendMessage(mHandler.obtainMessage(1, str3));
//                    Arrays.fill(mRevBuffer3, (byte) 0x0);
//                }
//            }
//        }, "串口接收线程").start();
    }


    /******************************第二种串口使用方式**********************************/


    private SerialPortHelper initSerialPortHelper(String port) {
        SerialPortHelper serialPort = new SerialPortHelper(port, 9600);
        serialPort.setSerialPortReceivedListener(new SerialPortHelper.OnSerialPortReceivedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSerialPortDataReceived(ComPortData comPortData) {
                String dataStr = comPortData.getRecTime() + " 收到: " + new String(comPortData.getRecData());
                mHandler.sendMessage(mHandler.obtainMessage(1, dataStr));
            }
        });
        serialPort.open();
        return serialPort;
    }

    private void sendDataHelper() {
        if (mHelper != null && mHelper.isOpen()) {
            mHelper.sendTxtString("我是SerialPortHelper");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            run_flag = false;
            if (mFileOutputStream3 != null) mFileOutputStream3.close();
            if (mFileInputStream3 != null) mFileInputStream3.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (mHelper != null) {
            mHelper.close();
        }
    }



    // 回调接口
    interface CallBack {
        void onResponse(String data);
    }



}
