package jp.co.fttx.rakuphotomail.mail;

import java.util.List;

import jp.co.fttx.rakuphotomail.helper.power.TracingPowerManager.TracingWakeLock;

import android.content.Context;

public interface PushReceiver {
    public Context getContext();
    public void syncFolder(Folder folder);
    public void messagesArrived(Folder folder, List<Message> mess);
    public void messagesFlagsChanged(Folder folder, List<Message> mess);
    public void messagesRemoved(Folder folder, List<Message> mess);
    public String getPushState(String folderName);
    public void pushError(String errorMessage, Exception e);
    public void setPushActive(String folderName, boolean enabled);
    public void sleep(TracingWakeLock wakeLock, long millis);
}
