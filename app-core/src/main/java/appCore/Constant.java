package appCore;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public abstract class Constant {

    //HTTPS 业务请求开关
    public static Boolean HTTPS_STATUS = false;

    //蓝牙打印机配对pin
    public static final String DEFAULT_BLUE_BOND_PIN_0000 = "0000";
    public static final String DEFAULT_BLUE_BOND_PIN_1234 = "1234";

    //APP 配置
    public static final String APP_CORE_CONFIG_PATH = "app-core.json";

    //https请求证书
    public static String HTTPS_PEM_FILE_NAME = "";

    //正式服首页
    public static String MAIN_URL = "";

    //系统socket地址
    public static String SYS_SOCKET_URL = "";

    //图片访问首页
    public static String IMAGE_HOST = "";

    //图片上传地址
    public static String IMG_UPLOAD_URL = "";

    //过期图片处理
    public static String OVERDUE_IMG_URL = "";


    //微信分享
    public static String WX_OPEN_APP_ID = "";

    //BUGLY
    public static String BUGLY_KEY = "";

    //UMeng
    public static String UMENG_APP_KEY = "";
    public static String UMENG_PUSH_APP_SECRET = "";

    //包名
    public static String APPLICATION_ID = "";

    //weex页面跳转过滤
    public static String WEEX_CATEGORY = "";

    public static String getSaveImgPath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "Pictures" + File.separator + "files";
    }


    //版本code
    public static String VERSION_CODE = "";

    //版本code
    public static String VERSION_NAME = "";





}