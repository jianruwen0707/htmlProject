package appCore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllowPermissions {
    // 判断是否需要检测，防止不停的弹框
    private boolean permissionCheck = true;

    private static final int PERMISSION_REQUEST_CODE = 90;


    // 检查权限
    public Boolean checkPermissions(final Activity activity, String... permissions) {
        final List<String> needRequestPermissionList = getDeniedPermissions(activity, permissions);
        if (null != needRequestPermissionList
                && needRequestPermissionList.size() > 0) {

            String items[] = {};
            Map map = new HashMap();
            for (int i = 0; i < needRequestPermissionList.size(); i++) {
                System.out.println("应用权限"+needRequestPermissionList.get(i));
                if (needRequestPermissionList.get(i).equals("android.permission.WRITE_EXTERNAL_STORAGE") || needRequestPermissionList.get(i).equals("android.permission.READ_EXTERNAL_STORAGE")) {
                    if (Arrays.binarySearch(items, "【存储权限】用于拜访照片、头像等图像的浏览、上传与存储") == -1) {
                        items = insert(items, "【存储权限】用于拜访照片、头像等图像的浏览、上传与存储");
                    }
                } else if (needRequestPermissionList.get(i).equals("android.permission.BLUETOOTH_CONNECT") || needRequestPermissionList.get(i).equals("android.permission.BLUETOOTH_ADVERTISE") || needRequestPermissionList.get(i).equals("android.permission.BLUETOOTH_SCAN")) {

                    if (Arrays.binarySearch(items, "【蓝牙权限】用于单据打印场景查找、匹配与链接蓝牙设备") == -1) {
                        items = insert(items, "【蓝牙权限】用于单据打印场景查找、匹配与链接蓝牙设备");
                    }
                } else if (needRequestPermissionList.get(i).equals("android.permission.CAMERA")) {
                    if (Arrays.binarySearch(items, "【相机权限】用于进行头像、商品图片、拜访任务的拍照场景") == -1) {
                        items = insert(items, "【相机权限】用于进行头像、商品图片、拜访任务的拍照场景 ");
                    }
                } else if (needRequestPermissionList.get(i).equals("android.permission.ACCESS_COARSE_LOCATION")) {
                    if (Arrays.binarySearch(items, "【位置权限】用于记录客户的送货定位、拜访时的精准签到等场景") == -1) {
                        items = insert(items, "【位置权限】用于记录客户的送货定位、拜访时的精准签到等场景");
                    }

                }
            }

            if (items.length == 0) {
                items = insert(items, "请求授权，以便使用相应功能");
            }
            new AlertDialog.Builder(activity).setTitle("权限申请用途说明")//设置对话框标题
//                        .setMessage("是否需要更换xxx？")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {//添加确定按钮

                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件，点击事件没写，自己添加
                            System.out.println("应用权限请求权限"+needRequestPermissionList.size());
                            ActivityCompat.requestPermissions(activity,
                                    needRequestPermissionList.toArray(
                                            new String[needRequestPermissionList.size()]),
                                    PERMISSION_REQUEST_CODE);
                            return;
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮

                @Override
                public void onClick(DialogInterface dialog, int which) {//响应事件，点击事件没写，自己添加
//                    ToastCompat.makeText(activity, "请开启权限，以便使用相应功能", ToastCompat.LENGTH_LONG).show();
                    return;
                }

            }).show();//在按键响应事件中显示此对话框



//            ActivityCompat.requestPermissions(activity,
//                    needRequestPermissionList.toArray(
//                            new String[needRequestPermissionList.size()]),
//                    PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private static String[] insert(String[] strings, String string) {
        if (strings == null) {
            strings = new String[0];
        }
        if (string.isEmpty()) {

            return null;
        }
        String[] resultString = new String[strings.length + 1];
        for (int i = 0; i < strings.length; i++) {
            resultString[i] = strings[i];
        }
        resultString[strings.length] = string;
        return resultString;
    }


    // 获取权限集中需要申请权限的列表
    private List<String> getDeniedPermissions(Activity activity, String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
    }

//    // 检测是否所有的权限都已经授权
//    private boolean verifyPermissions(int[] grantResults) {
//        for (int result : grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 显示提示信息
//     */
//    private void showNoPermissionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("提示");
//        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。");
//
//        // 拒绝, 退出应用
//        builder.setNegativeButton("取消",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                });
//        builder.setPositiveButton("设置",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        openAppSettings();
//                    }
//                });
//        builder.setCancelable(false);
//        builder.show();
//    }
//
//    //启动应用的设置
//    private void openAppSettings() {
//        Intent intent = new Intent(
//                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (!verifyPermissions(grantResults)) {
//                showNoPermissionDialog();
//                permissionCheck = false;
//            }
//        }
////        printDeviceInfo();
//    }


}
