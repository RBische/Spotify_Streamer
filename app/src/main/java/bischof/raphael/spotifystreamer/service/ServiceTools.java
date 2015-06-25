/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.service;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Code found on stackoverflow post : http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
 * Edited by biche on 24/06/2015.
 */
public class ServiceTools {

    public static boolean isServiceRunning(Context context, String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }
}
