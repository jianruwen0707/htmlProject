package appCore.ReceiptPrinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import appCore.R;
import appCore.utils.ESCUtil;
import appCore.utils.SunmiPrintHelper;

public class SunmiPrintActivity {
    Context mContext;


    public SunmiPrintActivity(Context context) {
        this.mContext = context;
    }

    private SunmiPrinterService sunmiPrinterService;

    public void printData(String printInfo) throws RemoteException {
        try {
            final JSONArray printList = JSONArray.parseArray(printInfo);
            printList(mContext, printList);

        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public void printList(Context context, JSONArray printList) throws RemoteException {
        sunmiPrinterService = SunmiPrintHelper.getInstance().sunmiPrinterService;
        if (sunmiPrinterService == null) {
            //TODO Service disconnection processing
            return;
        }
        try {
            int paper = sunmiPrinterService.getPrinterPaper();
            sunmiPrinterService.printerInit(null);

            System.out.println("处理打印数据" + printList);
            for (int i = 0; i < printList.size(); i++) {
                JSONObject printContent = (JSONObject) printList.get(i);

                String printType = (String) printContent.get("type");


//                     根据打印类型选择打印方式
                if (printType.equals("text")) {
                    printText(printContent);
                } else if (printType.equals("barcode")) {
//                                条形码
                    printBarcode(printContent);
                } else if (printType.equals("img")) {
//                                二维码
                    printBitmap(printContent);
                }

            }

            sunmiPrinterService.autoOutPaper(null);
            SunmiPrintHelper.getInstance().cutpaper();


        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }


    /**
     * 打印文本
     */
    private void printText(JSONObject printContent) throws RemoteException {
        try {
//        打印位置
            int align = 0;  //0，居左,1 居中 ,2居右
            if (printContent.get("align").equals("left")) {

            } else if (printContent.get("align").equals("right")) {
                align = 2;
            } else if (printContent.get("align").equals("center")) {
                align = 1;
            }


            String printStr = (String) printContent.get("content");
            int font_size = (int) printContent.get("font_size");
//            System.out.println("打印内容" + printStr);
            sunmiPrinterService.setAlignment(align, null);

            if (font_size < 24) {
                sunmiPrinterService.printText(printStr, null);
            } else {
                font_size =font_size+8;
                sunmiPrinterService.printTextWithFont(printStr,
                        null, font_size, null);

            }
            sunmiPrinterService.lineWrap(1, null);


        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    /**
     * 打印一维条码
     */
    private void printBarcode(JSONObject printContent) {
        try {
//        打印位置
            int align = 0;  //0，居左,1 居中 ,2居右
            if (printContent.get("align").equals("left")) {

            } else if (printContent.get("align").equals("right")) {
                align = 2;
            } else if (printContent.get("align").equals("center")) {
                align = 1;
            }

            int width = 2;
            int height = 80;

            if (printContent.containsKey("height")) {
                height = Integer.parseInt(String.valueOf(printContent.get("height")));
            }

            String printStr = (String) printContent.get("data");
            int symbology = 8;
            sunmiPrinterService.printBarCode(printStr, symbology, height, width, 1, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    private void printBitmap(JSONObject printContent) {
        try {
            //        打印位置
            int align = 0;  //0，居左,1 居中 ,2居右
            if (printContent.get("align").equals("left")) {

            } else if (printContent.get("align").equals("right")) {
                align = 2;
            } else if (printContent.get("align").equals("center")) {
                align = 1;
            }

            String printStr = (String) printContent.get("url");


            Bitmap bitmap = getBitmap(printStr);

            if (bitmap == null) {
                return;
            }

            sunmiPrinterService.printBitmap(bitmap, null);
            sunmiPrinterService.lineWrap(1, null);
            sunmiPrinterService.setAlignment(0, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    /**
     * Sample print receipt
     */
    public void printExample(Context context) {
        if (sunmiPrinterService == null) {
            //TODO Service disconnection processing
            return;
        }


        try {
            int paper = sunmiPrinterService.getPrinterPaper();
            sunmiPrinterService.printerInit(null);
            sunmiPrinterService.setAlignment(1, null);
            sunmiPrinterService.printText("测试样张\n", null);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sunmi);
            sunmiPrinterService.printBitmap(bitmap, null);
            sunmiPrinterService.lineWrap(1, null);
            sunmiPrinterService.setAlignment(0, null);
            try {
                sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, 0);
            } catch (RemoteException e) {
                sunmiPrinterService.sendRAWData(new byte[]{0x1B, 0x33, 0x00}, null);
            }
            sunmiPrinterService.printTextWithFont("说明：这是一个自定义的小票样式例子,开发者可以仿照此进行自己的构建\n",
                    null, 12, null);
            if (paper == 1) {
                sunmiPrinterService.printText("--------------------------------\n", null);
            } else {
                sunmiPrinterService.printText("------------------------------------------------\n",
                        null);
            }
            try {
                sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE);
            } catch (RemoteException e) {
                sunmiPrinterService.sendRAWData(ESCUtil.boldOn(), null);
            }
            String txts[] = new String[]{"商品", "价格"};
            int width[] = new int[]{1, 1};
            int align[] = new int[]{0, 2};
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            try {
                sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE);
            } catch (RemoteException e) {
                sunmiPrinterService.sendRAWData(ESCUtil.boldOff(), null);
            }
            if (paper == 1) {
                sunmiPrinterService.printText("--------------------------------\n", null);
            } else {
                sunmiPrinterService.printText("------------------------------------------------\n",
                        null);
            }
            txts[0] = "汉堡";
            txts[1] = "17¥";
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            txts[0] = "可乐";
            txts[1] = "10¥";
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            txts[0] = "薯条";
            txts[1] = "11¥";
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            txts[0] = "炸鸡";
            txts[1] = "11¥";
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            txts[0] = "圣代";
            txts[1] = "10¥";
            sunmiPrinterService.printColumnsString(txts, width, align, null);
            if (paper == 1) {
                sunmiPrinterService.printText("--------------------------------\n", null);
            } else {
                sunmiPrinterService.printText("------------------------------------------------\n",
                        null);
            }
            sunmiPrinterService.printTextWithFont("总计:          59¥\b", null, 40, null);
            sunmiPrinterService.setAlignment(1, null);
            sunmiPrinterService.printQRCode("谢谢惠顾", 10, 0, null);
            sunmiPrinterService.setFontSize(36, null);
            sunmiPrinterService.printText("谢谢惠顾", null);
            sunmiPrinterService.autoOutPaper(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 下载图片方法
     */
    public static Bitmap getBitmap(String url) {

        url = url.replace("http:", "https:");

        Bitmap bm = null;
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;

            int length = http.getContentLength();

            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, length);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {

        }
        return bm;
    }


}
