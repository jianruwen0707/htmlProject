package appCore.UsbModule;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;



import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.utils.PosPrinterDev;

import java.sql.SQLOutput;
import java.util.List;

import appCore.NativeBridge;
import appCore.R;

public class UsbActivity extends Activity {
    public static boolean ISCONNECT = false;

//    public static IMyBinder myBinder;
    public  static  Context context;
    public static String usbadrress = "";

    private ListView lvDevice = null;
    private UsbDeviceAdapter mUsbDeviceAdapter;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_bluetooth_list);
        lvDevice = (ListView) findViewById(R.id.lvDevice);
        initUsb();

    }


    /**
     * 连接usb
     */
    private void connectUSB(final String usbAddress) {

        System.out.println("usb打印机地址" + usbAddress);
        if (usbAddress.equals(null) || usbAddress.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.discon), Toast.LENGTH_SHORT).show();
        } else {
            NativeBridge.myBinder.ConnectUsbPort(getApplicationContext(), usbAddress, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT = true;
                    Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void OnFailed() {
                    ISCONNECT = false;
                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }



    private List<String> usbList;

    private  void  initUsb(){
        usbList = PosPrinterDev.GetUsbPathNames(this);

        System.out.println("usb列表"+usbList);

        if(usbList==null){
            Toast.makeText(getApplicationContext(), "请先连接设备", Toast.LENGTH_SHORT).show();
            return;
        }

        mUsbDeviceAdapter = new UsbDeviceAdapter(this, usbList, usbadrress);
        lvDevice.setAdapter(mUsbDeviceAdapter);
        lvDevice.setOnItemClickListener(mDeviceClickListener);
    }



    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          String usbName = (String) parent.getAdapter().getItem(position);
            connectUSB(usbName);


        }
    };







}
