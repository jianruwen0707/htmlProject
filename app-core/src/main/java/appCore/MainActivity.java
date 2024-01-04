/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package appCore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;


import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import appCore.UsbModule.SerialPortActivity;
import appCore.common.ActivityPoolManager;
import appCore.common.SpeechUtils;
import appCore.presenter.ScalePresenter;
import appCore.utils.SunmiPrintHelper;

import static appCore.common.BaseAppUtil.isConnNet;
import static appCore.common.BaseAppUtil.tipsNoNet;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    public static Context activityContext;
    ActivityPoolManager.ActivityHolder activityHolder;
    BridgeWebView webView;
    Context context;


    private UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";

    private UsbDevice usbDevice;

    @SuppressLint("JavascriptInterface")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activityContext =this;

        //网络检查
        if (!isConnNet(this)) {
            tipsNoNet(this);
        }



        //获得控件
        webView = (BridgeWebView) findViewById(R.id.wv_web_view);
        // 清缓存和记录，缓存引起的白屏
        webView.clearCache(true);
        webView.clearHistory();
        webView.setBackgroundColor(Color.parseColor("#2E313A"));

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(1000);
        webView.startAnimation(alphaAnimation);

        WebSettings webSettings = webView.getSettings();
        //处理http和https混合的问题
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // 允许javascript出错
        try {
            Method method = Class.forName("android.webkit.WebView").getMethod("setWebContentsDebuggingEnabled", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(null, true);
        } catch (Exception e) {
            // do nothing
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
        //设置适应屏幕
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setPluginState(WebSettings.PluginState.ON);//设置是否支持插件
        webSettings.setSupportZoom(true); //支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //设置存储模式
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        //setDomStorageEnabled解决了webview白屏问题  设置支持DomStorage
        webSettings.setDomStorageEnabled(true);
        //设置支持本地存储
        webSettings.setDatabaseEnabled(true);
//        webView.addJavascriptInterface(new JsApi(this), "qsEvent");
//        webView.addJavascriptInterface(new MainJavascriptInterface(webView.getCallbacks(), webView), "WebViewJavascriptBridge");
        NativeBridge nativeBridge = new NativeBridge(context, webView);
        nativeBridge.setMainActivity(this);
        webView.addJavascriptInterface(nativeBridge, "NativeBridge");






        CookieManager mCookieManager = CookieManager.getInstance();
        mCookieManager.setAcceptCookie(true);
        mCookieManager.setAcceptThirdPartyCookies(webView, true);

//        webView.setWebChromeClient(new Defa);


        //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return true;
            }
        });





        String indexUrl = Constant.MAIN_URL;
        webView.loadUrl(indexUrl);


//        initAction();

        getVersionInfo();


//Activity 队列记录
        activityHolder = ActivityPoolManager.add(indexUrl, this);


        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    final Map<String, Object> params = new HashMap<>();
                    params.put("event", "back");
                    params.put("canGoBack", webView.canGoBack());
//                    System.out.println("返回监听"+keyCode);
//                    System.out.println("返回监听最后"+webView.canGoBack());
                    //按返回键操作并且能回退网页
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
//                        System.out.println("返回监听111");
                        //返回键给前端监听

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:$androidBackClick('" + params + "')");
//                                ActivityPoolManager.exitClient();
                            }
                        });

                        //后退
//                        webView.goBack();
                        return true;
                    }else if(keyCode == KeyEvent.KEYCODE_BACK){
                        //返回键给前端监听
//                        System.out.println("返回监听333");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:$androidBackClick('" + params + "')");
//                                ActivityPoolManager.exitClient();
                            }
                        });

                        return true;
                    }
                }
                return false;
            }
        });

        init();

        SpeechUtils.getInstance(context);
    }



    private void injectJavascript(int keyCode) {
        Log.d("injectJavascript", "keyCode=" + keyCode);
        webView.loadUrl("javascript:remoteController && remoteController(" + keyCode + ")");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            this.injectJavascript(keyCode);
            return false;
        }
        return super.dispatchKeyEvent(event);
    }


    private void getVersionInfo() {
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            final int versionCode = packageInfo.versionCode;
            final int versionName = packageInfo.versionCode;

            Constant.VERSION_NAME=packageInfo.versionName;
            Constant.VERSION_CODE=packageInfo.versionCode+"";




            // 使用 versionCode 进行相关操作
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect print service through interface library
     */
    private void init(){
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
    }



    }
