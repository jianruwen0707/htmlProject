package appCore.blueTooth.holinBle;

public interface PrinterCallback {
    void onState(int var1);

    void onError(int var1);

    void onEvent(PrinterEvent var1);
}
