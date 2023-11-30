package appCore.autoUpdateApk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.FileProvider;


import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import appCore.R;

public class UpdateAPP {
    private static int progress; // 定义进度值
    private static String savePath; // APK下载之后保存的地址
    private static String saveFileName; // APK的文件名
    private static RemoteViews view = null; // 用来设置通知的View
    private static NotificationManager nm = null;
    private static Notification nn = null; // 引入通知
    private static FlikerProgressBar flikerProgressBar;
    private static int a;
    private static Dialog DownLoad;
    private static int handmsg = 1;
    private static final int DOWN_UPDATE = 0;// 下载中消息
    private static final int DOWN_OVER = 1;// 下载完成消息
    private static String apkDownloadPath; // 应用下载的地址
    private static Context mContext;
    public  static  UpdateDeatilCallBack mUpdateDeatilCallBack;
    public  static ArrayList updateInfo ;
    private static BridgeWebView mwebView;

    public static void showUpdateApp(final Context context,HashMap updateInfo,BridgeWebView webView) {
        String url = (String) updateInfo.get("URL");
        String updateLog= (String) updateInfo.get("UPDATE_LOG_LIST");
        String items[] = {};
        if(updateLog != null ){
            items = updateLog.split(",");
        }else {
            items = insert(items, "修复部分异常");
        }

        mwebView = webView;

        mContext = context;
        nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);//获取系统通知的服务
        nn = new Notification();//创建一个通知对象
        apkDownloadPath = url;
        savePath = mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/";
//        savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"
//              ;  //自定义下载文件保存地址
        saveFileName = savePath + "aikuaixiao.apk"; //自定义文件名



        //可根据需求加入自己设计的dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("版本更新")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File apkfile = new File(saveFileName);
                        if (apkfile.exists()) {
                            apkfile.delete();
                        }
                        a = 0;
                        view = new RemoteViews(context.getPackageName(), R.layout.download_progress_state_view);
                        nn.icon = R.mipmap.logo;
                        showDownLoad(context);
                        new Thread(mdownApkRunnable).start();

                    }
                }).create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public static void showDownLoad(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DownLoad = new Dialog(context, R.style.dialog);

        View layout = inflater.inflate(R.layout.dialog_download, null);
        DownLoad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DownLoad.addContentView(layout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        flikerProgressBar = (FlikerProgressBar) layout.findViewById(R.id.flikerbar);
        flikerProgressBar.setProgress(handmsg);

        LinearLayout ll_btn_cancel = layout.findViewById(R.id.ll_btn_cancel);
        ll_btn_cancel.setVisibility(View.VISIBLE);

        Button btn_cancel = (Button) layout.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                a = 1;
                DownLoad.dismiss();
            }
        });
        DownLoad.show();
        if (flikerProgressBar.getProgressText().equals("下载完成")) {
            a = 1;
            DownLoad.dismiss();

        }
    }

    // 下载APK的线程匿名类START
    public static Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkDownloadPath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);

                    if (handmsg < progress) {
                        handmsg++;
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                    }

                    if (a == 1) {
                        fos.close();
                        is.close();
                        break;
                    }
                    // 更新进度
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true || a == 1);// 点击取消就停止下载.
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
// 下载APK的线程匿名类END

    // 处理下载进度的Handler Start
    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    flikerProgressBar.setProgress(handmsg);
                    view.setProgressBar(R.id.download_progressbar, 100, handmsg, false);
                    view.setTextViewText(R.id.download_progress_text, handmsg + "%");
                    nn.contentView = view;
                    nn.flags = Notification.FLAG_AUTO_CANCEL;
                    nm.notify(0, nn);
                    super.handleMessage(msg);
                    break;
                case DOWN_OVER:
                    nm.cancel(0);
                    flikerProgressBar.finishLoad();
                    DownLoad.dismiss();


                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mwebView.loadUrl("javascript:$updateAppState('" + new HashMap() {{
                                put("updateApp", true);
                            }} + "')");
                        }
                    });


                    Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT);

                    installApk();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    // 安装apk
    public static void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            Toast.makeText(mContext, "安装文件不存在，请检查", Toast.LENGTH_SHORT);
            return;
        }

        //项目的包名 自己改
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.aikuaixiao.shouyin" + ".fileProvider", apkfile);//在AndroidManifest中的android:authorities值
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(install);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setAction(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(apkfile),
                    "application/vnd.android.package-archive");
            mContext.startActivity(i);
        }

    }


    /**
     * 浏览器下载apk
     *
     * @param apkDownloadPath
     */
    public static void downloadByBrowser(String apkDownloadPath) {
        Uri uri = Uri.parse(apkDownloadPath);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);

    }


    /**
     * 添加更新日志
     *
     * @param strings
     * @param string
     * @return
     */
    private static String[] insert(String[] strings, String string) {
        if (strings == null) {
            strings = new String[0];
        }
        if (string.isEmpty()) {

            return null;
        }
        String[] resultString = new String[strings.length + 1];
        for (int i = 0; i < strings.length; i++) {
            resultString[i] = strings[i];
        }
        resultString[strings.length] = string;
        return resultString;
    }

}
