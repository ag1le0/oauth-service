package com.foxconn.fii.notify.model;

import lombok.Data;

@Data
public class MailMessage {

    private String title;

    private String body;

    private String attach;

    private String fileName;

    public static MailMessage of(String title, String body) {
        return of(title, body, "", "");
    }

    public static MailMessage of(String title, String body, String attach, String fileName) {
        MailMessage result = new MailMessage();
        result.setTitle(title);
        result.setBody(body);
        result.setAttach(attach);
        result.setFileName(fileName);
        return result;
    }
}
