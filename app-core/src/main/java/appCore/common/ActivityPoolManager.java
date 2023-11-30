package appCore.common;

import android.app.Activity;
import android.content.Intent;



import java.util.ArrayList;
import java.util.List;

import appCore.MainActivity;

/**
 * Activity 队列池
 */
public class ActivityPoolManager {

    public static List<ActivityHolder> activityHolderList = new ArrayList<>();

    public static class ActivityHolder {
        Activity activity;
        String url;

        public ActivityHolder(String url, Activity activity) {
            this.activity = activity;
            this.url = url;
        }

        public Activity getActivity() {
            return activity;
        }

        public String getUrl() {
            return url;
        }
    }

    public static void destory(ActivityHolder activityHolder){
        activityHolderList.remove(activityHolder);
    }

    public static void exitClient() {
        clearActivity();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity.activityContext.startActivity(startMain);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static ActivityHolder add(String url, Activity activity) {
        if (activity.getClass() == MainActivity.class) {
            activityHolderList.clear();
            clear(activity);
        }
        url = filterUrlParam(url);
        ActivityHolder activityHolder = new ActivityHolder(url, activity);
        activityHolderList.add(activityHolder);
        return activityHolder;
    }

    public static void clearActivity() {
        // 关闭所有Activity
        for (int i = 0; i < activityHolderList.size(); i++) {
            if (activityHolderList.get(i).getActivity().getClass() != MainActivity.class) {
                if (null != activityHolderList.get(i) && null != activityHolderList.get(i).getActivity()) {
                    activityHolderList.get(i).getActivity().finish();
                    System.out.println(" kill :"+activityHolderList.get(i).url);
                    activityHolderList.remove(i);
                }
            }
        }
    }

    public static void clear(Activity activity) {
        // 关闭所有Activity
        for (int i = 0; i < activityHolderList.size(); i++) {
            if (null != activityHolderList.get(i) && activityHolderList.get(i).getActivity() != activity) {
                activityHolderList.get(i).getActivity().finish();
                System.out.println(" kill :"+activityHolderList.get(i).url);
                activityHolderList.remove(i);
            }
        }

    }

    public static void clearOthers() {
        int aim = -2;
        for (int i = 0; i < activityHolderList.size(); i++) {
            if (activityHolderList.get(i).getActivity().getClass() == MainActivity.class) {
                aim = i;
            }
        }
        for (int i = 0; i < activityHolderList.size(); i++) {
            if (i != aim) {
                activityHolderList.get(i).getActivity().finish();
                System.out.println(" kill :"+activityHolderList.get(i).url);
                activityHolderList.remove(i);
                i--;
                aim--;
            }
        }
    }

    private static String filterUrlParam(String url) {
        return url.split("\\?")[0];
    }

    /**
     * 关闭最新的几个页面 除了当前页
     *
     * @param size
     */
    public static void clearActivityBySizeLeftOne(int size) {
        // 关闭所有Activity
        for (int d = 1; d <= size; d++) {
            int i = activityHolderList.size() - 1;
            if (activityHolderList.get(i).getActivity().getClass() != MainActivity.class) {
                if (i >= 0) {
                    if (null != activityHolderList.get(i)) {
                        activityHolderList.get(i).getActivity().finish();
                        System.out.println(" kill :"+activityHolderList.get(i).url);
                        activityHolderList.remove(i);
                    }
                }
            }
        }
    }

    /**
     * 关闭最新的几个页面 包括当前页
     *
     * @param size
     */
    public static void clearActivityBySize(int size) {
        // 关闭所有Activity
        for (int d = 1; d <= size; d++) {
            int i = activityHolderList.size() - 1;
            if (activityHolderList.get(i).getActivity().getClass() != MainActivity.class) {
                if (i >= 0) {
                    if (null != activityHolderList.get(i)) {
                        activityHolderList.get(i).getActivity().finish();
                        System.out.println(" kill :"+activityHolderList.get(i).url);
                        activityHolderList.remove(i);
                    }
                }
            }
        }
    }

    /**
     * 关闭最新的几个页面 除了当前页
     *
     * @param size
     */
    public static void clearActivityBySizeLeft(int size) {
        // 关闭所有Activity
        for (int d = 1; d <= size; d++) {
            int i = activityHolderList.size() - 2;
            if(activityHolderList.get(i) !=null){
                if (activityHolderList.get(i).getActivity().getClass() != MainActivity.class) {
                    if (i >= 0) {
                        if (null != activityHolderList.get(i)) {
                            activityHolderList.get(i).getActivity().finish();
                            System.out.println(" kill :"+activityHolderList.get(i).url);
                            activityHolderList.remove(i);
                        }
                    }
                }
            }

        }
    }


    /**
     * 一直关闭直到到了目标url的页面
     *
     * @param url
     */
    public static void clearActivityByUrl(String url) {
        url = filterUrlParam(url);
        int aimI = -1;
        System.out.println(" aimUrl = "+url);
        //找到最早的目标url
        for (int i = 0; i < activityHolderList.size(); i++) {
            System.out.println(" current aimI = "+aimI +" | "+activityHolderList.get(i).getUrl());
            if (activityHolderList.get(i).getUrl().equals(url) && aimI < 0) { //未找到继续找
                aimI = i;
                continue;//只需要第一条
            } else if (aimI >= 0) { //找到之后 后面的直接清掉
                activityHolderList.get(i).getActivity().finish();
                System.out.println(" kill :"+activityHolderList.get(i).url);
                activityHolderList.remove(i);
                i--;
            }
        }
    }

}