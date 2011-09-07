package jp.co.fttx.rakuphotomail.controller;

import jp.co.fttx.rakuphotomail.mail.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
