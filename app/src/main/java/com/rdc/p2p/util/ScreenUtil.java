package com.rdc.p2p.util;

import android.content.Context;
import android.util.DisplayMetrics;

import java.lang.reflect.Field;


public class ScreenUtil {
    /**
     * 根据手机分辨率将dp转为px单位
     */
    public static int dip2px(Context mContext, float dpValue) {
        final float scale = mContext.getResources()
                .getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
