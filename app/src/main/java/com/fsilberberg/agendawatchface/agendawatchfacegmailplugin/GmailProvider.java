package com.fsilberberg.agendawatchface.agendawatchfacegmailplugin;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.janbo.agendawatchface.api.AgendaItem;
import de.janbo.agendawatchface.api.AgendaWatchfacePlugin;
import de.janbo.agendawatchface.api.LineOverflowBehavior;
import de.janbo.agendawatchface.api.TimeDisplayType;

/**
 * Created by 333fr_000 on 10/14/14.
 */
public class GmailProvider extends AgendaWatchfacePlugin {
    private static GmailContentObserver observer;

    @Override
    public String getPluginId() {
        return "com.fsilberberg.agendawatchface.agendawatchfacegmailplugin";
    }

    @Override
    public String getPluginDisplayName() {
        return "Unread Inbox";
    }

    @Override
    public void onRefreshRequest(Context context) {
        ContentResolver cr = context.getContentResolver();

        if (observer != null) {
            cr.unregisterContentObserver(observer);
        }
        observer = new GmailContentObserver(new Handler(), context);
        cr.registerContentObserver(Uri.parse("content://gmail-ls"), false, observer);

        observer.updateLabel();
    }

    public void sendData(Context context, int unread) {
        List<AgendaItem> gmailItemList = new ArrayList<>();
        AgendaItem gmailItem = new AgendaItem(getPluginId());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        gmailItem.startTime = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        gmailItem.endTime = cal.getTime();

        gmailItem.line1.timeDisplay = TimeDisplayType.NONE;
        gmailItem.line1.textBold = false;
        gmailItem.line1.overflow = LineOverflowBehavior.OVERFLOW_IF_NECESSARY;
        gmailItem.line1.text = String.format("Inbox: %d unread", unread);
        gmailItem.priority = 100;
        gmailItemList.add(gmailItem);
        publishData(context, gmailItemList, false);
    }

    @Override
    public void onShowSettingsRequest(Context context) {
        // Start our settings activity
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
