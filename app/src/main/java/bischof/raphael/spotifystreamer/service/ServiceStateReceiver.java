/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver able to receive the StreamerService state changes.
 * Created by biche on 25/06/2015.
 */
public class ServiceStateReceiver extends BroadcastReceiver {

    private OnServiceStateChangeListener listener;

    public void setListener(OnServiceStateChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener!=null){
            listener.onServiceStateChange(intent.getBooleanExtra(StreamerService.EXTRA_SERVICE_STARTED,true));
        }
    }

    public interface OnServiceStateChangeListener{
        void onServiceStateChange(boolean started);
    }
}
