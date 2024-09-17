package com.ami.bakhoobiservice.most_recent_applications;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

public class AppLockService extends Service {
    // Write code here to run the service contiuously, and call every 50 to 300 ms getRecentApps(Context context) method to get the current open application
    public String getRecentApps(Context context) {
        String topPackageName = "";
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 30, System.currentTimeMillis() + (10 * 1000));
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
        }
        if (event != null && !TextUtils.isEmpty(event.getPackageName()) && event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            if (AndroidUtils.isRecentActivity(event.getClassName())) {
                return event.getClassName();
            }
            return event.getPackageName();
        } else {
            topPackageName = "";
        }
        return topPackageName;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
