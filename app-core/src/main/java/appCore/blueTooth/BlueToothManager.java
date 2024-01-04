package appCore.blueTooth;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

import appCore.blueTooth.holinBle.BleUnit;

/**
 * 蓝牙设备管理
 */
public class BlueToothManager {

    // 基本蓝牙信息
    private static Map<String, SimpleBlueTooth> bluetoothDeviceMap = new HashMap<>();
    // 蓝牙服务
    private static Map<String, BleUnit> bluetoothDeviceServiceMap = new HashMap<>();

    public static SimpleBlueTooth getDeviceByType(String type) {

        System.out.println(JSON.toJSONString(bluetoothDeviceMap));
        return bluetoothDeviceMap.get(type);
    }

    public static BleUnit getDeviceServiceByType(String type) {
        return bluetoothDeviceServiceMap.get(type);
    }

    public static void addByType(String type, SimpleBlueTooth bluetoothDevice, BleUnit bleUnit) {
        bluetoothDeviceMap.put(type, bluetoothDevice);
        bleUnit.connect(bluetoothDevice.getAddress());
        bluetoothDeviceServiceMap.put(type, bleUnit);
    }

    public static void removeByType(String type) {
        bluetoothDeviceMap.remove(type);
        bluetoothDeviceServiceMap.remove(type);
    }

    public static void close(String type) {

        System.out.println(JSON.toJSONString(bluetoothDeviceMap));
        bluetoothDeviceServiceMap.get(type).getService().stop();
        removeByType(type);
    }

    public static void closeAll() {
        for (Map.Entry<String, BleUnit> k : bluetoothDeviceServiceMap.entrySet()) {
            try {
                k.getValue().getService().stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bluetoothDeviceMap.clear();
        bluetoothDeviceServiceMap.clear();
    }

}
