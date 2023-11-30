package appCore;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;

import appCore.common.BaseAppUtil;

public class HtmlApplication extends Application {
    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();

        //系统配置读取
        initConfigJson();


        initUpdatConfige();


        context = getBaseContext();


    }

    /**
     * 系统配置初始化
     */
    private void initConfigJson() {
        String jsonStr = BaseAppUtil.getJson(Constant.APP_CORE_CONFIG_PATH, this);
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            Constant.BUGLY_KEY = jsonObject.getString("buglyKey");
            Constant.MAIN_URL = jsonObject.getString("mainUrl");
            Constant.SYS_SOCKET_URL = jsonObject.getString("sysSocketUrl");
            Constant.HTTPS_PEM_FILE_NAME = jsonObject.getString("httpsPemFile");
            Constant.IMAGE_HOST = jsonObject.getString("imageHost");
            Constant.IMG_UPLOAD_URL = jsonObject.getString("imageUploadUrl");
            Constant.WX_OPEN_APP_ID = jsonObject.getString("wxOpenAppId");
            Constant.UMENG_APP_KEY = jsonObject.getString("umengAppKey");
            Constant.UMENG_PUSH_APP_SECRET = jsonObject.getString("umengPushAppSecret");
            Constant.APPLICATION_ID = jsonObject.getString("applicationId");
            Constant.WEEX_CATEGORY = "weex.intent.category." + Constant.APPLICATION_ID;
            Constant.OVERDUE_IMG_URL = jsonObject.getString("overImgUrl");
        } catch (Exception e) {

            Toast.makeText(this, "APP配置读取失败", Toast.LENGTH_SHORT);
        }
    }




    public void  initUpdatConfige(){

//        更新库配置
        UpdateConfig updateConfig = new UpdateConfig()
                .setDebug(true)//是否是Debug模式
                .setMethodType(TypeConfig.METHOD_GET)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的方法
                .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_MODEL)//设置获取更新信息的方式
                .setShowNotification(true)//配置更新的过程中是否在通知栏显示进度
                .setNotificationIconRes(R.mipmap.logo)//配置通知栏显示的图标
                .setUiThemeType(TypeConfig.UI_THEME_AUTO)//配置UI的样式，一种有12种样式可供选择
                .setRequestHeaders(null)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的请求头
                .setRequestParams(null)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的请求参数
                .setAutoDownloadBackground(false)//是否需要后台静默下载，如果设置为true，则调用checkUpdate方法之后会直接下载安装，不会弹出更新页面。当你选择UI样式为TypeConfig.UI_THEME_CUSTOM，静默安装失效，您需要在自定义的Activity中自主实现静默下载，使用这种方式的时候建议setShowNotification(false)，这样基本上用户就会对下载无感知了
                .setNeedFileMD5Check(false);//是否需要进行文件的MD5检验，如果开启需要提供文件本身正确的MD5校验码，DEMO中提供了获取文件MD5检验码的工具页面，也提供了加密工具类Md5Utils

        //初始化
        AppUpdateUtils.init(this, updateConfig);

    }

}
