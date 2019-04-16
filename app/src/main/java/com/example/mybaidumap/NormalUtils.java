package com.example.mybaidumap;

import android.app.Activity;
import android.content.Intent;

public class NormalUtils {
    public static void gotoSettings(Activity activity){
        Intent intent=new Intent(activity,DemoNaviSettingActivity.class);
        activity.startActivity(intent);
    }
    public static  String getTTSAppID(){
        return "16026495";
    }
}
