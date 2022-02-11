package com.foxconn.fii.notify.model;

import lombok.Data;

@Data
public class NotifyMessage {
    private System system;

    private Type type;

    private String source;

    private String from;

    private String toUser;

    private Integer toGroup;

    private String message;

    public static NotifyMessage of(System system, Type type, String source, String from, String toUser, String message) {
        return of(system, type, source, from, toUser, null, message);
    }

    public static NotifyMessage of(System system, Type type, String source, String from, Integer toGroup, String message) {
        return of(system, type, source, from, null, toGroup, message);
    }

    public static NotifyMessage of(System system, Type type, String source, String from, String toUser, Integer toGroup, String message) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setSystem(system);
        notifyMessage.setType(type);
        notifyMessage.setSource(source);
        notifyMessage.setFrom(from);
        notifyMessage.setToUser(toUser);
        notifyMessage.setToGroup(toGroup);
        notifyMessage.setMessage(message);
        return notifyMessage;
    }

    public enum System {
        HALO,
        FII_CHAT,
        CIVET,
        MAIL,
        FII_VN
    }

    public enum Type {
        TEXT,
        NEWS
    }
}
