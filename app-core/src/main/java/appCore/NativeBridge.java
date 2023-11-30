package appCore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cretin.www.cretinautoupdatelibrary.activity.UpdateType2Activity;
import com.cretin.www.cretinautoupdatelibrary.interfaces.UpdateDetailListener;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.google.gson.Gson;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import appCore.ReceiptPrinter.PrintActivity;
import appCore.UsbModule.SerialPortActivity;
import appCore.UsbModule.UsbActivity;
import appCore.autoUpdateApk.AutoUpdateApp;
import appCore.common.ActivityPoolManager;


public class NativeBridge extends Activity {
    MainActivity mainActivity;
    Context context;
    BridgeWebView webView;


    public NativeBridge(Context context, BridgeWebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void call(final String callback, final int which) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                // 更新 UI 的操作
                webView.loadUrl("javascript:" + callback + "(" + which + ")"); // 执行 JavaScript 回调函数
            }
        });
    }

    public void callDebug(final String callback, final int which, final String code) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                String javascript = String.format("javascript:%s(%s,%s)", callback, which, code);
                // 更新 UI 的操作
                webView.loadUrl(javascript); // 执行 JavaScript 回调函数
            }
        });
    }


    @JavascriptInterface
    public void showDialog(String content, String title, String jsonObjectString, final String callback) {
        System.out.println("调试222");

        Gson gson = new Gson();
        Alert alertInfo = gson.fromJson(jsonObjectString, Alert.class);
        // 在这里显示 Android 弹框
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(alertInfo.getConfirmText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        call(callback, 1);
                    }
                })
                .setNegativeButton(alertInfo.getCancelText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        call(callback, -1);
                    }
                })
                .show();
    }

    @JavascriptInterface
    public void showDebugDialog(final String callback) {
        Gson gson = new Gson();
        final EditText prev = new EditText(this.context);
        prev.setWidth(300);
        final EditText suffix = new EditText(this.context);
        suffix.setWidth(300);
        final TextView textView = new TextView(this.context);
        textView.setText("udbu");
        final LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(prev);
        linearLayout.addView(textView);
        linearLayout.addView(suffix);
        linearLayout.setPadding(30, 0, 30, 0);
//        linearLayout.setGravity(LinearLayout.TEXT_ALIGNMENT_CENTER);
        //获取ip而已，不用在乎
        new AlertDialog.Builder(this.context).setTitle("请输入调试吗")
                .setView(linearLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        callDebug(callback, 1, prev.getText().toString() + "udbu" + suffix.getText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        callDebug(callback, -1, "");
                    }
                })
                .show();


    }

    @JavascriptInterface
    public void showPromptDialog(String title, String jsonObjectString, final String callback) {
        Gson gson = new Gson();
        Alert alertInfo = gson.fromJson(jsonObjectString, Alert.class);
        final EditText et = new EditText(this.context);
        //获取ip而已，不用在乎
        new AlertDialog.Builder(this.context).setTitle(title)
                .setView(et)
                .setPositiveButton(alertInfo.getConfirmText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        call(callback, 1);
                    }
                })
                .setNegativeButton(alertInfo.getCancelText(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 执行回调函数
                        call(callback, -1);
                    }
                })
                .show();
    }


    @JavascriptInterface
    public void showToast(String msg) {
        Toast makeText = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        makeText.setGravity(Gravity.CENTER, 0, 0);
        makeText.show();
        System.out.println("调试");

    }

    @JavascriptInterface
    public void exitApp() {
        System.out.println("app监听关闭app222");
        mainActivity.finish();
        android.os.Process.killProcess(android.os.Process.myPid()); // 杀死进程
//        ActivityPoolManager.exitClient();
    }


    @JavascriptInterface
    public void initWeight() {

        SerialPortActivity serialPort = new SerialPortActivity();
        serialPort.initAction(webView);


    }


    ServiceConnection mSerconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NativeBridge.myBinder = (IMyBinder) service;
            Log.e("myBinder", "connect");
//            System.out.println("打印初始化connect");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("myBinder", "disconnect");
//            System.out.println("打印初始化disconnect");
        }
    };

    public static IMyBinder myBinder;

    @JavascriptInterface
    public void initUsbPrinter() {



//        初始化usd
        Intent intent = new Intent(context, PosprinterService.class);
        context.bindService(intent, mSerconnection, BIND_AUTO_CREATE);


        Intent usbIntent = new Intent(context, UsbActivity.class);
        context.startActivity(usbIntent);


    }


    @JavascriptInterface
    public void printData(String printlist) {
        PrintActivity printActivity = new PrintActivity(context);
        printActivity.printData(printlist);


    }


    @JavascriptInterface
    public void reload() {
        webView.reload();
    }


    @JavascriptInterface
    public void updateApp(String updateInfo) {

        JSONObject info = JSONObject.parseObject(updateInfo);
        AutoUpdateApp.autoUpdateApp(info);


        UpdateType2Activity.mUpdateDetailListener = new UpdateDetailListener() {
            @Override
            public void updateApp(boolean isUpdate) {
                if (isUpdate) {

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:$updateAppState('" + new HashMap() {{
                                put("updateApp", true);
                            }} + "')");
                        }
                    });

                }
            }
        };


    }


    @JavascriptInterface
    public String getAppInfo() {

        return Constant.VERSION_NAME;

    }



    @JavascriptInterface
    public void closeApp() {
//        System.out.println("app监听关闭app");
//        ActivityPoolManager.exitClient();
    }




}