package com.foxconn.fii.main.config;

import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class MainSchedulerConfig {

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void unlockAccount() {
        log.info("### unlock account START");

        List<User> userList = userRepository.findByLockedIsTrue();
        for (User user : userList) {
            user.setFailedLoginNumber(0);
            user.setLocked(false);
            userRepository.save(user);
        }

        log.info("### unlock account END");
    }

}
