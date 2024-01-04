package appCore.ReceiptPrinter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos58;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.StringUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import appCore.HtmlApplication;
import appCore.NativeBridge;
import appCore.R;
import appCore.UsbModule.UsbActivity;

public class PrintActivity extends Activity {
    Context mContext;



    List<byte[]> list = new ArrayList<>();

    public PrintActivity(Context context) {
      this.mContext =context;
    }



    /**
     * 加载走纸命令
     */
//    public  byte[] cut = new byte[] { 0x1D, 0x56, 0x42, 0x00 };// 切纸并且走纸
    public byte[] cut = new byte[]{29, 86, 1};
    public void printData(String printInfo) {

//        Toast.makeText(HtmlApplication.context,getString(R.string.connect_first), Toast.LENGTH_SHORT).show();

        final JSONArray printList = JSONArray.parseArray(printInfo);

//        初始化打印数据
        if (list != null) {
            list = new ArrayList<>();
        }


//        判读usb是否连接成功
        if (!UsbActivity.ISCONNECT) {
//            Toast.makeText(mContext,getString(R.string.connect_first), Toast.LENGTH_SHORT).show();
            System.out.println("打印机连接失败");
            return;
        }



        new Thread(new Runnable() {
            public void run() {
                NativeBridge.myBinder.WriteSendData(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                        System.out.println("打印机搜索");
//                        Toast.makeText(getApplicationContext(),getString(R.string.con_success), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void OnFailed() {

//                Toast.makeText(getApplicationContext(),getString(R.string.con_failed), Toast.LENGTH_SHORT).show();
                    }
                }, new ProcessData() {
                    @Override
                    public List<byte[]> processDataBeforeSend() {
                        System.out.println("处理打印数据" + printList);

//                        List<byte[]> list = new ArrayList<>();
                        for (int i = 0; i < printList.size(); i++) {
                            JSONObject printContent = (JSONObject) printList.get(i);

                            String printType = (String) printContent.get("type");


//                     根据打印类型选择打印方式
                            if (printType.equals("text")) {
                                printText(printContent, list);
                            } else if (printType.equals("barcode")) {
//                                条形码
                                printBarcode(printContent, list);
                            } else if (printType.equals("img")) {
//                                二维码
                                printBitmap(printContent, list);
                            }

                        }

                        list.add(cut);


                        return list;
                    }
                });
            }
        }).start();


    }


    /**
     * 打印文本
     */
    private void printText(JSONObject printContent, List<byte[]> list) {
//        打印位置
        int align = 0;  //0，居左,1 居中 ,2居右
        if (printContent.get("align").equals("left")) {

        } else if (printContent.get("align").equals("right")) {
            align = 2;
        } else if (printContent.get("align").equals("center")) {
            align = 1;
        }


        String printStr = (String) printContent.get("content");
        System.out.println("打印内容" + printStr);


        int font_size = (int) printContent.get("font_size");


        list.add(DataForSendToPrinterPos58.initializePrinter());
        list.add(DataForSendToPrinterPos58.selectCharacterSize(font_size));//字体放大一倍
        list.add(DataForSendToPrinterPos58.selectAlignment(align)); //0，居左,1 居中 ,2居右
        list.add(StringUtils.strTobytes(printStr));
        list.add(DataForSendToPrinterPos58.printAndFeedLine());


    }

    /**
     * 打印一维条码
     */
    private void printBarcode(JSONObject printContent, List<byte[]> list) {
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

//        if (printContent.containsKey("width")) {
//            width = Integer.parseInt(String.valueOf(printContent.get("width")));
//        }

        String printStr = (String) printContent.get("data");
        System.out.println("打印内容" + printStr);


        //初始化打印机，清除缓存
        list.add(DataForSendToPrinterPos58.initializePrinter());
        //选择对齐方式
        list.add(DataForSendToPrinterPos58.selectAlignment(align));
        //选择HRI文字文字
        list.add(DataForSendToPrinterPos58.selectHRICharacterPrintPosition(02));
        //设置条码宽度
        list.add(DataForSendToPrinterPos58.setBarcodeWidth(width));
        //设置高度
        list.add(DataForSendToPrinterPos58.setBarcodeHeight(height));
        //条码的类型和内容，73是code128的类型，请参考说明手册每种类型的规则
        list.add(DataForSendToPrinterPos58.printBarcode(73, 10, printStr));
        //打印指令
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
    }

    /**
     * 打印二维条码
     */
    private void printqr(JSONObject printContent, List<byte[]> list) {

//        打印位置
        int align = 0;  //0，居左,1 居中 ,2居右
        if (printContent.get("align").equals("left")) {

        } else if (printContent.get("align").equals("right")) {
            align = 2;
        } else if (printContent.get("align").equals("center")) {
            align = 1;
        }


        String printStr = (String) printContent.get("url");
        System.out.println("打印内容" + printStr);

        //初始化打印机，清除缓存
        list.add(DataForSendToPrinterPos58.initializePrinter());
        //选择对齐方式
        list.add(DataForSendToPrinterPos58.selectAlignment(align));
        list.add(DataForSendToPrinterPos80.printQRcode(3, 48, printStr));
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
    }

    private void printBitmap(JSONObject printContent, final List<byte[]> list) {

        //        打印位置
        int align = 0;  //0，居左,1 居中 ,2居右
        if (printContent.get("align").equals("left")) {

        } else if (printContent.get("align").equals("right")) {
            align = 2;
        } else if (printContent.get("align").equals("center")) {
            align = 1;
        }

        String printStr = (String) printContent.get("url");
        System.out.println("打印内容" + printStr);


        Bitmap bitmap = getBitmap(printStr);

        if(bitmap ==null){
            return;
        }


        list.add(DataForSendToPrinterPos80.initializePrinter());
        List<Bitmap> blist = new ArrayList<>();
        blist = BitmapProcess.cutBitmap(50, bitmap);
        for (int i = 0; i < blist.size(); i++) {
            list.add(DataForSendToPrinterPos80.printRasterBmp(0, blist.get(i), BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Center, 384));
        }
        list.add(DataForSendToPrinterPos80.printAndFeedLine());




    }


    /**
     * 下载图片方法
     */
    public static Bitmap getBitmap(String url) {

        url = url.replace("http:","https:");



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


