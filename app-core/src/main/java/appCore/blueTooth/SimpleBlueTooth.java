package appCore.blueTooth;

/**
 * 基本蓝牙服务
 */
public class SimpleBlueTooth {

    String address;

    String name;

    public SimpleBlueTooth(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
