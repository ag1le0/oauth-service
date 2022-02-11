package com.foxconn.fii.security.config;

import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.entity.UserDevice;
import com.foxconn.fii.main.data.repository.UserDeviceRepository;
import com.foxconn.fii.main.service.UserService;
import com.foxconn.fii.notify.model.MailMessage;
import com.foxconn.fii.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Aspect
@Configuration
public class Oauth2Aspect {

    @Autowired
    private UserService userService;

    @AfterReturning(value = "execution( * org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint.authorize(..))", returning = "result")
    public void executeAfterReturningAuthorize(Object result) throws Exception {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && result instanceof ModelAndView) {
            log.debug("### redirect parameter lang from cookie");
            HttpServletRequest request = requestAttributes.getRequest();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lang".equalsIgnoreCase(cookie.getName())) {
                        ModelAndView modelAndView = (ModelAndView) result;
                        if (modelAndView.getView() != null && modelAndView.getView() instanceof RedirectView) {
                            RedirectView view = (RedirectView) modelAndView.getView();
                            view.setUrl(view.getUrl() + "&lang=" + cookie.getValue());
                        }
                        break;
                    }
                }
            }
        }
    }

    /*
    @AfterReturning(value = "execution( * org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))", returning = "result")
    public void executeAfterReturningToken(JoinPoint jp, Object result) throws Exception {
        Map<String, String> parameters = (Map<String, String>) jp.getArgs()[1];
        String username = parameters.get("username");
        String grantType = parameters.get("grant_type");
        String mac = parameters.get("mac");
        String otp = parameters.get("otp");
        String version = parameters.get("version");

        *//*
        if ("password".equalsIgnoreCase(grantType) && !"agent".equalsIgnoreCase(version)) {
            userService.checkOTP(username, mac, otp);
        }*//*

        Optional<User> optionalUser = userService.findByUsername(parameters.get("username"));
        if (!optionalUser.isPresent()) {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
        }

        if (optionalUser.get().getPwdExpiredTime() == null || optionalUser.get().getPwdExpiredTime().getTime() - System.currentTimeMillis() < 0) {
            throw new BadCredentialsException("PWD_EXPIRED");
        }
    }
    */
    /*
    @AfterReturning(value = "execution( * org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))", returning = "result")
    public void executeAfterReturningToken(JoinPoint jp, Object result) throws Exception {
        Map<String, String> parameters = (Map<String, String>) jp.getArgs()[1];
        String grantType = parameters.get("grant_type");
        String mac = parameters.get("mac");
        String otp = parameters.get("otp");

        if ("password".equalsIgnoreCase(grantType)) {
            Optional<User> optionalUser = userService.findByUsername(parameters.get("username"));
            if (!optionalUser.isPresent()) {
                throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
            }

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                log.debug("### redirect parameter lang from cookie");
                HttpServletRequest request = requestAttributes.getRequest();
                optionalUser.get().setIpAddress(request.getRemoteAddr());
                userService.save(optionalUser.get());
            }

            if (!"################".equals(mac)) {
                if (StringUtils.isEmpty(mac)) {
                    throw new BadCredentialsException("MAC_IS_NOT_BLANK");
                }

                Optional<UserDevice> optionalUserDevice = userDeviceRepository.findByUserAndMac(optionalUser.get(), mac);
                if (optionalUserDevice.isPresent()) {
                    UserDevice device = optionalUserDevice.get();
                    if (device.isTrust()) {
                        return;
                    }

                    if (!StringUtils.isEmpty(otp)) {
                        if (otp.equals(device.getOtpCode()) &&
                                (device.getOtpExpiredDate() != null && device.getOtpExpiredDate().getTime() - System.currentTimeMillis() > 0)) {
                            device.setTrust(true);
                            userDeviceRepository.save(device);
                        } else {
                            if (!otp.equals(device.getOtpCode())) {
                                throw new BadCredentialsException("OTP_INVALIDED");
                            } else {
                                throw new BadCredentialsException("OTP_EXPIRED");
                            }
                        }
                    } else {
                        Random random = new SecureRandom();
                        String code = String.format("%06d", random.nextInt(1000000));
                        device.setOtpCode(code);

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MINUTE, 5);
                        device.setOtpExpiredDate(calendar.getTime());

                        userDeviceRepository.save(device);

                        MailMessage message = new MailMessage();
                        message.setTitle(String.format("Login to Production Management [%s]", code));
                        message.setBody("Dear user,<br/>This message is automatically sent, please do not reply directly!<br/>Ext: 26152");
                        notifyService.notifyToMail(message, "", optionalUser.get().getEmail());

                        throw new BadCredentialsException("OTP_NOT_YET_CONFIRMED");
                    }
                } else {
                    UserDevice userDevice = new UserDevice();
                    userDevice.setUser(optionalUser.get());
                    userDevice.setMac(mac);

                    Random random = new SecureRandom();
                    String code = String.format("%06d", random.nextInt(1000000));
                    userDevice.setOtpCode(code);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, 5);
                    userDevice.setOtpExpiredDate(calendar.getTime());

                    userDeviceRepository.save(userDevice);

                    MailMessage message = new MailMessage();
                    message.setTitle(String.format("Login to Production Management [%s]", code));
                    message.setBody("Dear user,<br/>This message is automatically sent, please do not reply directly!<br/>Ext: 26152");
                    notifyService.notifyToMail(message, "", optionalUser.get().getEmail());

                    throw new BadCredentialsException("OTP_NOT_YET_CONFIRMED");
                }
            }
        }
    }
    */

    @AfterReturning(value = "execution( * org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint.checkToken(..))", returning = "result")
    public void executeAfterReturningCheckToken(Object result) throws Exception {
        Map<String, Object> response = (Map<String, Object>) result;
        Optional<User> optionalUser = userService.findByUsername((String) response.get("user_name"));
        if (optionalUser.isPresent()) {
            response.put("user", optionalUser.get());
        }
    }
}
