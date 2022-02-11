package com.foxconn.fii.main.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.utils.CommonUtils;
import com.foxconn.fii.common.utils.CookieUtils;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
//import oauth.CivetOAuth;
//import oauth.civetOAuth.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
public class MvcController {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestCache requestCache;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        model.addAttribute("error", error);

        String redirectUrl = "/introduce";
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String[] redirectUri = savedRequest.getParameterValues("redirect_uri");
            if (redirectUri != null && redirectUri.length > 0) {
                redirectUrl = redirectUri[0].replace("/login", "/handle-login-success");
            }
        }
        response.addCookie(new Cookie("redirect_uri", redirectUrl));

        model.addAttribute("loginIcivetUrl", "http://civetinterface.foxconn.com/Open/oauth/?to_code=login," + redirectUrl);

        String loginSsoUrl = request.getRequestURL().toString().replace("/login", "/login-with-sso");
        model.addAttribute("loginSsoUrl", "https://lh-account.cnsbg.efoxconn.com/oauth2/v3/auth?scope=profile&state=&client_id=e7caf4ff63702298bb1316fe18913937&redirect_uri=" + loginSsoUrl);

        return "login";
    }

    @GetMapping("/logout")
    public void logout(
            @RequestParam(required = false) String redirectUrl,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("### logout {}", authentication.getPrincipal());

        new SecurityContextLogoutHandler().logout(request, null, null);
        try {
            if (StringUtils.isEmpty(redirectUrl)) {
                response.sendRedirect(request.getHeader("referer"));
            } else {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            log.error("### logout error", e);
        }
    }
//
//    @GetMapping("/login-with-icivet")
//    public void loginWithIcivet(
//            @RequestParam (required = false) String redirectUrl,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//        try {
//            String appid = "sslGW9wN1Mw1";
//            CivetOAuth oAuth = new CivetOAuth(appid);
//            UserInfo userInfo = oAuth.FastGetUserInfo(request);
//
//            if (userInfo.getCivetno() != null) {
//                UserDetails userDetails = userService.loadUserByUsername(userInfo.getCivetno());
//                SecurityContext context = SecurityContextHolder.createEmptyContext();
//                Authentication authentication =  new UsernamePasswordAuthenticationToken(userInfo.getCivetno(), null, userDetails.getAuthorities());
//                context.setAuthentication(authentication);
//                SecurityContextHolder.setContext(context);
//            }
//
//            if (StringUtils.isEmpty(redirectUrl)) {
//                response.sendRedirect(request.getHeader("referer"));
//            } else {
//                response.sendRedirect(redirectUrl);
//            }
//
//        } catch (Exception e) {
//            log.error("### logout error", e);
//        }
//    }

    @RequestMapping("/login-with-sso")
    public void loginWithSSo(
            HttpServletRequest request,
            HttpServletResponse response,
            String redirectUrl,
            String code) {
        try {
            String clientId = "e7caf4ff63702298bb1316fe18913937";
            String clientSecret = "lv0eldz8b1jyby0jizy2mdacema06ou0iptmmlamvdgzui9loi7xfo1f24mcnvai";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("grant_type", "authorization_code");
            map.add("code", code);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> getAccessTokenResponseEntity = restTemplate.exchange("https://lh-account.cnsbg.efoxconn.com/oauth2/v3/token", HttpMethod.POST, entity, String.class);

            Map<String, Object> getAccessTokenResponseMap = objectMapper.readValue(getAccessTokenResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});

            if (getAccessTokenResponseMap.containsKey("access_token")) {
                String accessToken = (String) getAccessTokenResponseMap.get("access_token");

                headers.add("Authorization", "Bearer " + accessToken);
                HttpEntity<String> profileEntity = new HttpEntity<>("", headers);

                ResponseEntity<String> getProfileResponseEntity = restTemplate.exchange("https://lh-account.cnsbg.efoxconn.com/oauth2/v3/profile", HttpMethod.GET, profileEntity, String.class);

                Map<String, Object> getProfileResponseMap = objectMapper.readValue(getProfileResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});

                if (getProfileResponseMap.containsKey("username")) {
                    UserDetails userDetails = userService.loadUserByUsername((String) getProfileResponseMap.get("username"));
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    Authentication authentication =  new UsernamePasswordAuthenticationToken(getProfileResponseMap.get("username"), null, userDetails.getAuthorities());
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
            }

            if (StringUtils.isEmpty(redirectUrl)) {
                Cookie cookie = CookieUtils.getCookie(request, "redirect_uri");
                if (cookie != null) {
                    response.sendRedirect(cookie.getValue());
                } else {
                    response.sendRedirect("/introduce");
                }
            } else {
                response.sendRedirect(redirectUrl);
            }

        } catch (Exception e) {
            log.error("### login with sso error", e);
        }
    }

    @GetMapping("/change-password")
    public String getChangePasswordPage(Model model) {
        return "change-password";
    }

    @GetMapping("/register")
    public String createRegister(Model model) {
        return "register";
    }

    @GetMapping("/user-manager")
    public String manageUser(Model model) {
        return "user-manager";
    }

    @GetMapping(value = {"/user-information", "/"})
    public String infoUser(Model model) {
        return "infoUser";
    }
}
