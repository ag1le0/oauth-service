package com.foxconn.fii;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Calendar;

@Slf4j
public class MainTest {

    @Test
    public void test() {
        Calendar calendar = Calendar.getInstance();
        String str = String.format("集團越南富弘公司 %d 年 %02d 月外籍員工越南薪資發放明細表", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) == 0 ? 12 : calendar.get(Calendar.MONTH));
        log.info("{}", str);
    }

    @Test
    public void generatePassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode("Foxconn168!!");
        log.info("{}", password);
    }
}
