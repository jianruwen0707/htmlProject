package appCore.autoUpdateApk;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.cretin.www.cretinautoupdatelibrary.interfaces.AppDownloadListener;
import com.cretin.www.cretinautoupdatelibrary.interfaces.AppUpdateInfoListener;
import com.cretin.www.cretinautoupdatelibrary.model.DownloadInfo;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;

import java.util.HashMap;

import appCore.autoUpdateApk.model.ListModel;

public class AutoUpdateApp {
    private static Boolean forceUpdate;
    private static String versionName;
    private static int versionCode;
    private static String updateLog;
    private static String updateUrl;


    public static void autoUpdateApp(JSONObject updateInfo) {
        forceUpdate = Boolean.parseBoolean((String) updateInfo.get("APP_VERSION_RECORD.FORCE_UPDATE"));//是否强制更新 1 普通更新  ， 0 强制更新
        versionName = (String) updateInfo.get("APP_VERSION_RECORD.VERSION_NAME");//更新版本名称
        versionCode = Integer.parseInt((String) updateInfo.get("APP_VERSION_RECORD.VERSION_CODE"));//更新code
        updateLog = (String) updateInfo.get("UPDATE_LOG_LIST"); //更新日志
        updateUrl = (String) updateInfo.get("URL");  //下载链接

//        forceUpdate = false;
//        versionName = "dd";
//        versionCode = 3;
//        updateLog = "13";
//        updateUrl = "https://aikuaixiao-temp.oss-cn-shanghai.aliyuncs.com/tmp/shouyin/shouyin_3.0_orign_2023-11-22_20-36-32_legu_aligned_signed.apk";


        ListModel listModel = new ListModel();
        listModel.setForceUpdate(forceUpdate);
        listModel.setUiTypeValue(302);
        listModel.setCheckFileMD5(false);
        listModel.setSourceTypeVaule(TypeConfig.DATA_SOURCE_TYPE_MODEL);


        DownloadInfo info = new DownloadInfo().setApkUrl(updateUrl)
                .setProdVersionCode(versionCode)
                .setProdVersionName(versionName)
                .setForceUpdateFlag(listModel.isForceUpdate() ? 1 : 0)  // 1 普通更新  ， 0 强制更新
                .setUpdateLog(updateLog);


        AppUpdateUtils.getInstance().getUpdateConfig().setUiThemeType(listModel.getUiTypeValue());
        //打开文件MD5校验
        AppUpdateUtils.getInstance().getUpdateConfig().setNeedFileMD5Check(false);
        AppUpdateUtils.getInstance().getUpdateConfig().setDataSourceType(listModel.getSourceTypeVaule());
        //开启或者关闭后台静默下载功能
        AppUpdateUtils.getInstance().getUpdateConfig().setAutoDownloadBackground(false);
        if (listModel.isAutoUpdateBackground()) {
            //开启静默下载的时候推荐关闭通知栏进度提示
            AppUpdateUtils.getInstance().getUpdateConfig().setShowNotification(false);
        } else {
            AppUpdateUtils.getInstance().getUpdateConfig().setShowNotification(true);
        }


        //因为这里打开了MD5的校验 我在这里添加一个MD5检验监听监听
        AppUpdateUtils.getInstance()
                .addAppUpdateInfoListener(new AppUpdateInfoListener() {
                    @Override
                    public void isLatestVersion(boolean isLatest) {
                        Log.e("HHHHHHHHHHHHHHH", "isLatest:" + isLatest);
                    }
                })
                .addAppDownloadListener(new AppDownloadListener() {
                    @Override
                    public void downloading(int progress) {
                        Log.e("HHHHHHHHHHHHHHH", "progress:" + progress);
                    }

                    @Override
                    public void downloadFail(String msg) {
                        Log.e("HHHHHHHHHHHHHHH", "msg:" + msg);
                    }

                    @Override
                    public void downloadComplete(String path) {
                        Log.e("HHHHHHHHHHHHHHH", "path:" + path);
                    }

                    @Override
                    public void downloadStart() {
                        Log.e("HHHHHHHHHHHHHHH", "start");
                    }

                    @Override
                    public void reDownload() {

                    }

                    @Override
                    public void pause() {

                    }
                })
                .checkUpdate(info);
    }
}
