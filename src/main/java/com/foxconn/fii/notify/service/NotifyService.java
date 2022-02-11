package com.foxconn.fii.notify.service;


import com.foxconn.fii.notify.model.MailMessage;

public interface NotifyService {

    void notifyToMail(MailMessage message, String from, String to);
}
