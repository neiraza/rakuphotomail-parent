package jp.co.fttx.rakuphotomail.service;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.remotecontrol.K9RemoteControl;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.Account.FolderMode;
import jp.co.fttx.rakuphotomail.RakuPhotoMail.BACKGROUND_OPS;

import static jp.co.fttx.rakuphotomail.remotecontrol.K9RemoteControl.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

public class RemoteControlService extends CoreService {
    private final static String RESCHEDULE_ACTION = "jp.co.fttx.rakuphotomail.service.RemoteControlService.RESCHEDULE_ACTION";
    private final static String PUSH_RESTART_ACTION = "jp.co.fttx.rakuphotomail.service.RemoteControlService.PUSH_RESTART_ACTION";

    private final static String SET_ACTION = "jp.co.fttx.rakuphotomail.service.RemoteControlService.SET_ACTION";

    public static void set(Context context, Intent i, Integer wakeLockId) {
        //  Intent i = new Intent();
        i.setClass(context, RemoteControlService.class);
        i.setAction(RemoteControlService.SET_ACTION);
        addWakeLockId(i, wakeLockId);
        if (wakeLockId == null) {
            addWakeLock(context, i);
        }
        context.startService(i);
    }

    public static final int REMOTE_CONTROL_SERVICE_WAKE_LOCK_TIMEOUT = 20000;

    @Override
    public void startService(final Intent intent, final int startId) {
        if (RakuPhotoMail.DEBUG)
            Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService started with startId = " + startId);
        final Preferences preferences = Preferences.getPreferences(this);

        if (RESCHEDULE_ACTION.equals(intent.getAction())) {
            if (RakuPhotoMail.DEBUG)
                Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService requesting MailService poll reschedule");
            MailService.actionReschedulePoll(this, null);
        }
        if (PUSH_RESTART_ACTION.equals(intent.getAction())) {
            if (RakuPhotoMail.DEBUG)
                Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService requesting MailService push restart");
            MailService.actionRestartPushers(this, null);
        } else if (RemoteControlService.SET_ACTION.equals(intent.getAction())) {
            if (RakuPhotoMail.DEBUG)
                Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService got request to change settings");
            execute(getApplication(), new Runnable() {
                        public void run() {
                            try {
                                boolean needsReschedule = false;
                                boolean needsPushRestart = false;
                                String uuid = intent.getStringExtra(K9_ACCOUNT_UUID);
                                boolean allAccounts = intent.getBooleanExtra(K9_ALL_ACCOUNTS, false);
                                if (RakuPhotoMail.DEBUG) {
                                    if (allAccounts) {
                                        Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService changing settings for all accounts");
                                    } else {
                                        Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService changing settings for account with UUID " + uuid);
                                    }
                                }
                                Account[] accounts = preferences.getAccounts();
                                for (Account account : accounts) {
                                    //warning: account may not be isAvailable()
                                    if (allAccounts || account.getUuid().equals(uuid)) {

                                        if (RakuPhotoMail.DEBUG)
                                            Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService changing settings for account " + account.getDescription());

                                        String notificationEnabled = intent.getStringExtra(K9_NOTIFICATION_ENABLED);
                                        String ringEnabled = intent.getStringExtra(K9_RING_ENABLED);
                                        String vibrateEnabled = intent.getStringExtra(K9_VIBRATE_ENABLED);
                                        String pushClasses = intent.getStringExtra(K9_PUSH_CLASSES);
                                        String pollClasses = intent.getStringExtra(K9_POLL_CLASSES);
                                        String pollFrequency = intent.getStringExtra(K9_POLL_FREQUENCY);

                                        if (notificationEnabled != null) {
                                            account.setNotifyNewMail(Boolean.parseBoolean(notificationEnabled));
                                        }
                                        if (ringEnabled != null) {
                                            account.getNotificationSetting().setRing(Boolean.parseBoolean(ringEnabled));
                                        }
                                        if (vibrateEnabled != null) {
                                            account.getNotificationSetting().setVibrate(Boolean.parseBoolean(vibrateEnabled));
                                        }
                                        if (pushClasses != null) {
                                            needsPushRestart |= account.setFolderPushMode(FolderMode.valueOf(pushClasses));
                                        }
                                        if (pollClasses != null) {
                                            needsReschedule |= account.setFolderSyncMode(FolderMode.valueOf(pollClasses));
                                        }
                                        if (pollFrequency != null) {
                                            String[] allowedFrequencies = getResources().getStringArray(R.array.account_settings_check_frequency_values);
                                            for (String allowedFrequency : allowedFrequencies) {
                                                if (allowedFrequency.equals(pollFrequency)) {
                                                    Integer newInterval = Integer.parseInt(allowedFrequency);
                                                    needsReschedule |= account.setAutomaticCheckIntervalMinutes(newInterval);
                                                }
                                            }
                                        }
                                        account.save(Preferences.getPreferences(RemoteControlService.this));
                                    }
                                }
                                if (RakuPhotoMail.DEBUG)
                                    Log.i(RakuPhotoMail.LOG_TAG, "RemoteControlService changing global settings");

                                String backgroundOps = intent.getStringExtra(K9_BACKGROUND_OPERATIONS);
                                if (K9RemoteControl.K9_BACKGROUND_OPERATIONS_ALWAYS.equals(backgroundOps)
                                        || K9RemoteControl.K9_BACKGROUND_OPERATIONS_NEVER.equals(backgroundOps)
                                        || K9RemoteControl.K9_BACKGROUND_OPERATIONS_WHEN_CHECKED.equals(backgroundOps)
                                        || K9RemoteControl.K9_BACKGROUND_OPERATIONS_WHEN_CHECKED_AUTO_SYNC.equals(backgroundOps)) {
                                    BACKGROUND_OPS newBackgroundOps = BACKGROUND_OPS.valueOf(backgroundOps);
                                    boolean needsReset = RakuPhotoMail.setBackgroundOps(newBackgroundOps);
                                    needsPushRestart |= needsReset;
                                    needsReschedule |= needsReset;
                                }

                                String theme = intent.getStringExtra(K9_THEME);
                                if (theme != null) {
                                    RakuPhotoMail.setK9Theme(K9RemoteControl.K9_THEME_DARK.equals(theme) ? android.R.style.Theme : android.R.style.Theme_Light);
                                }

                                SharedPreferences sPrefs = preferences.getPreferences();

                                Editor editor = sPrefs.edit();
                                RakuPhotoMail.save(editor);
                                editor.commit();

                                if (needsReschedule) {
                                    Intent i = new Intent();
                                    i.setClassName(getApplication().getPackageName(), "jp.co.fttx.rakuphotomail.service.RemoteControlService");
                                    i.setAction(RESCHEDULE_ACTION);
                                    long nextTime = System.currentTimeMillis() + 10000;
                                    BootReceiver.scheduleIntent(RemoteControlService.this, nextTime, i);
                                }
                                if (needsPushRestart) {
                                    Intent i = new Intent();
                                    i.setClassName(getApplication().getPackageName(), "jp.co.fttx.rakuphotomail.service.RemoteControlService");
                                    i.setAction(PUSH_RESTART_ACTION);
                                    long nextTime = System.currentTimeMillis() + 10000;
                                    BootReceiver.scheduleIntent(RemoteControlService.this, nextTime, i);
                                }
                            } catch (Exception e) {
                                Log.e(RakuPhotoMail.LOG_TAG, "Could not handle K9_SET", e);
                                Toast toast = Toast.makeText(RemoteControlService.this, e.getMessage(), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    }
                    , RemoteControlService.REMOTE_CONTROL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
        }
    }

}
