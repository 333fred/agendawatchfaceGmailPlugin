package com.fsilberberg.agendawatchface.agendawatchfacegmailplugin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Fredric on 10/11/14.
 */
public class GmailContentObserver extends ContentObserver {

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    private static String[] accountNames;

    private final Context appContext;

    private int previousUnread = 0;

    public GmailContentObserver(Handler handler, Context context) {
        super(handler);
        appContext = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        // Get the account names if they haven't been gotten before
        updateLabel();
    }

    public void updateLabel() {
        if (accountNames == null) {
            Account[] accounts = AccountManager.get(appContext).getAccountsByType(ACCOUNT_TYPE_GOOGLE);
            accountNames = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                accountNames[i] = accounts[i].name;
            }
        }

        // Get the unread for all accounts
        for (String name : accountNames) {
            Cursor labelCursor = appContext.getContentResolver().query(GmailContract.Labels.getLabelsUri(name), null, null, null, null);
            if (labelCursor != null) {
                final String canonicalInboxName = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX;
                final int nameIndex = labelCursor.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
                final int unreadIndex = labelCursor.getColumnIndexOrThrow(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS);

                int numUnread = 0;

                while (labelCursor.moveToNext()) {
                    if (canonicalInboxName.equals(labelCursor.getString(nameIndex))) {
                        numUnread += labelCursor.getInt(unreadIndex);
                    }
                }

                new GmailProvider().sendData(appContext, numUnread);

                labelCursor.close();

            } else {
                Log.w(GmailContentObserver.class.getName(), "Unknown error when getting the labels for account " + name);
            }
        }
    }
}
