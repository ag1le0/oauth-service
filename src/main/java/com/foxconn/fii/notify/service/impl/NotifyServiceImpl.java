package com.foxconn.fii.notify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxconn.fii.notify.model.MailMessage;
import com.foxconn.fii.notify.model.NotifyMessage;
import com.foxconn.fii.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public void notifyToMail(MailMessage data, String from, String to) {
        try {
            String json = mapper.writeValueAsString(data);
            String message = mapper.writeValueAsString(NotifyMessage.of(
                    NotifyMessage.System.MAIL,
                    NotifyMessage.Type.TEXT,
                    "OAUTH",
                    from,
                    to,
                    json));

            amqpTemplate.convertAndSend("notify", "", message);
        } catch (Exception e) {
            log.error("### send to mail error", e);
        }
    }

}
