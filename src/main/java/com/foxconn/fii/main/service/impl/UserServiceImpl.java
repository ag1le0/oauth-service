package com.foxconn.fii.main.service.impl;

import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.utils.BeanUtils;
import com.foxconn.fii.common.utils.CommonUtils;
import com.foxconn.fii.main.data.entity.*;
import com.foxconn.fii.main.data.model.CreateUser;
import com.foxconn.fii.main.data.model.UserContext;
import com.foxconn.fii.main.data.repository.*;
import com.foxconn.fii.main.service.UserService;
import com.foxconn.fii.notify.model.MailMessage;
import com.foxconn.fii.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    Employee employee = employeeRepository.findByEmployeeId(username)
                            .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));

//            userOptional = loadUserFromHRService(username);
//            if (!userOptional.isPresent()) {
//                log.error("### load user {} from hr service not found", username);
//                throw new UsernameNotFoundException(String.format("User %s not found", username));
//            }
//            throw new UsernameNotFoundException(String.format("User %s not found", username));

                    User ins = new User();
                    BeanUtils.copyPropertiesIgnoreNull(employee, ins, "id");
                    ins.setUsername(username);

                    if (employee.getHireDate() != null) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                        ins.setPassword(encoder.encode(df.format(employee.getHireDate())));
                    } else {
                        ins.setPassword(encoder.encode(User.DEFAULT_PASSWORD));
                    }

                    ins.setActive(employee.getStatus() != 0);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 3);
                    ins.setInfoExpiredDate(calendar.getTime());
                    calendar.add(Calendar.YEAR, -1);
                    ins.setPwdExpiredTime(calendar.getTime());
                    Role defaultRole = new Role();
                    defaultRole.setId(44);
                    defaultRole.setRole("OAUTH_USER");
                    ins.setRoles(Collections.singletonList(defaultRole));

                    List<Department> department = departmentRepository.findByOuCodeAndLevel(ins.getOuCode(), "1");
                    if (!department.isEmpty()) {
                        ins.setAssistant(department.get(0).getEmployeeId());
                    }

                    userRepository.save(ins);

                    return ins;
                });

        if (user.getInfoExpiredDate() == null || user.getInfoExpiredDate().getTime() < System.currentTimeMillis()) {
//            updatedInfoUserFromHRService(user);
            employeeRepository.findByEmployeeId(username).ifPresent(employee -> {
                BeanUtils.copyPropertiesIgnoreNull(employee, user, "id");
                user.setActive(employee.getStatus() != 0);

                List<Department> department = departmentRepository.findByOuCodeAndLevel(user.getOuCode(), "1");
                if (!department.isEmpty()) {
                    user.setAssistant(department.get(0).getEmployeeId());
                }

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 3);
                user.setInfoExpiredDate(calendar.getTime());
                userRepository.save(user);
            });
        }

        if (!user.isActive()) {
            log.error("### loadUserByUsername user {} is disabled", username);
            throw new UsernameNotFoundException(String.format("User %s has been disabled", username));
        }

        if (user.isLocked()) {
            log.error("### loadUserByUsername user {} is locked", username);
            throw new UsernameNotFoundException(String.format("User %s has been locked", username));
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRole()));
        }

        UserContext userContext = new UserContext(user.getUsername(), user.getPassword(), authorities);
        userContext.setUser(user);

        return userContext;
    }

    @Override
    public void increaseFailedLoginNumber(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));
        if (user.getFailedLoginNumber() >= 10) {
            user.setLocked(true);
            userRepository.save(user);
        } else {
            userRepository.increasingFailedLoginNumber(username);
        }
    }

    @Override
    public void resetFailedLoginNumber(String username) {
        userRepository.resetFailedLoginNumber(username);
    }

    @Override
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserContext) {
            return ((UserContext) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        } else {
            throw CommonException.of("Security context holder error {}", authentication.getPrincipal());
        }
    }

    @Override
    public User getCurrentUser() {
        String username = getCurrentUsername();
        Optional<User> userOptional = findByUsername(username);
        if (!userOptional.isPresent()) {
            log.error("### get user information {} not found", username);
            throw CommonException.of("User %s not found", username);
        }
        return userOptional.get();
    }

    @Override
    public boolean isCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_OAUTH_ADMIN".equals(authority.getAuthority())) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    @Override
    public void checkOTP(String username, String mac, String otp) {
        if (StringUtils.isEmpty(mac)) {
            throw new BadCredentialsException("MAC_IS_NOT_BLANK");
        }

        if ("################".equals(mac)) {
            return;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        User user = findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Authentication Failed. Username or Password not valid."));

        List<UserDevice> deviceList = userDeviceRepository.findByUser(user);
        if (deviceList.isEmpty()) {
            UserDevice userDevice = new UserDevice();
            userDevice.setUser(user);
            userDevice.setMac(mac);
            userDevice.setOtpCode("");
            userDevice.setOtpExpiredDate(new Date());
            userDevice.setTrust(true);
            userDeviceRepository.save(userDevice);
        } else {
            UserDevice device = userDeviceRepository.findByUserAndMac(user, mac)
                    .orElseGet(() -> {
                        UserDevice ins = new UserDevice();
                        ins.setUser(user);
                        ins.setMac(mac);
                        return ins;
                    });

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
            }

            Random random = new SecureRandom();
            String code = String.format("%06d", random.nextInt(1000000));
            device.setOtpCode(code);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 3);
            device.setOtpExpiredDate(calendar.getTime());

            userDeviceRepository.save(device);

            MailMessage message = new MailMessage();
            message.setTitle(String.format("Login OTP [%s] of user [%s]. It will expired at %s", code, username, df.format(calendar.getTime())));
            message.setBody("Dear user,<br/>This message is automatically sent, please do not reply directly!<br/>Ext: 26152");
            if (!StringUtils.isEmpty(user.getEmail())) {
                notifyService.notifyToMail(message, "", user.getEmail());
            } else {
                if (StringUtils.isEmpty(user.getAssistant())) {
                    throw CommonException.of("Assistant {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", user.getAssistant(), username);
                }

                Employee assistant = employeeRepository.findByEmployeeId(user.getAssistant())
                        .orElseThrow(() -> CommonException.of("Assistant {} of user {} not found", user.getAssistant(), username));

                if (StringUtils.isEmpty(assistant.getEmail())) {
                    throw CommonException.of("Assistant {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", user.getAssistant(), username);
                }
                notifyService.notifyToMail(message, "", assistant.getEmail());
            }

            throw new BadCredentialsException("OTP_NOT_YET_CONFIRMED");
        }
    }

    @Override
    public String getEmail(String username, String grantType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        if ("INDIVIDUAL".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getEmail())) {
//                throw CommonException.of("User {} does not have email. Please try with another way or contact with contact to ext 26152!", username);
                return null;
            }
            return user.getEmail();
        } else if ("ASSISTANT".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAssistant())) {
                throw CommonException.of("User {} does not have assistant. Please try with another way or contact with contact to ext 26152!", username);
            }
            Employee assistant = employeeRepository.findByEmployeeId(user.getAssistant())
                    .orElseThrow(() -> CommonException.of("Assistant {} of user {} not found", user.getAssistant(), username));
            if (StringUtils.isEmpty(assistant.getEmail())) {
                throw CommonException.of("Assistant {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", user.getAssistant(), username);
            }
            return assistant.getEmail();
        } else if ("MANAGER".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAllManagers())) {
                throw CommonException.of("User {} does not have manager. Please try with another way or contact with contact to ext 26152!", username);
            }
            String[] managers = user.getAllManagers().split(";");
            Employee manager = employeeRepository.findByEmployeeId(managers[0])
                    .orElseThrow(() -> CommonException.of("Manager {} of user {} not found", managers[0], username));
            if (StringUtils.isEmpty(manager.getEmail())) {
                throw CommonException.of("Manager {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", managers[0], username);
            }
            return manager.getEmail();
        } else {
            throw CommonException.of("Grant type {} is not supported", grantType);
        }
    }

    @Override
    public void resetPassword(String username, String identityNo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        Employee employee = employeeRepository.findByEmployeeId(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        if (StringUtils.isEmpty(employee.getIdentityNo()) || !employee.getIdentityNo().equalsIgnoreCase(identityNo)) {
            throw CommonException.of("Identity No is not match");
        }

        user.setActive(true);
        user.setPassword(encoder.encode(User.DEFAULT_PASSWORD));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        user.setPwdExpiredTime(calendar.getTime());
        userRepository.save(user);
    }

    @Override
    public void requestForgotPassword(String username, String grantType, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        Random random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1000000));
        user.setOtpCode(code);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 3);
        user.setOtpExpiredDate(calendar.getTime());
        userRepository.save(user);

        MailMessage message = new MailMessage();
        message.setTitle(String.format("Change password OTP [%s]", code));
        message.setBody("Dear user,<br/>This message is automatically sent, please do not reply directly!<br/>Ext: 26152");
        if ("INDIVIDUAL".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getEmail())) {
                if (StringUtils.isEmpty(email) || !CommonUtils.checkEmail(email)) {
                    throw CommonException.of("User {} does not have email. Please try with another way or contact with contact to ext 26152!", username);
                }
                user.setEmail(email);
                userRepository.save(user);
            }
            notifyService.notifyToMail(message, "", user.getEmail());
        } else if ("ASSISTANT".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAssistant())) {
                throw CommonException.of("User {} does not have assistant. Please try with another way or contact with contact to ext 26152!", username);
            }
            Employee assistant = employeeRepository.findByEmployeeId(user.getAssistant())
                    .orElseThrow(() -> CommonException.of("Assistant {} of user {} not found", user.getAssistant(), username));
            if (StringUtils.isEmpty(assistant.getEmail())) {
                throw CommonException.of("Assistant {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", user.getAssistant(), username);
            }
            notifyService.notifyToMail(message, "", assistant.getEmail());
        } else if ("MANAGER".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAllManagers())) {
                throw CommonException.of("User {} does not have manager. Please try with another way or contact with contact to ext 26152!", username);
            }
            String[] managers = user.getAllManagers().split(";");
            Employee manager = employeeRepository.findByEmployeeId(managers[0])
                    .orElseThrow(() -> CommonException.of("Manager {} of user {} not found", managers[0], username));
            if (StringUtils.isEmpty(manager.getEmail())) {
                throw CommonException.of("Manager {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", managers[0], username);
            }
            notifyService.notifyToMail(message, "", manager.getEmail());
        } else {
            throw CommonException.of("Grant type {} is not supported", grantType);
        }
    }

    @Override
    public boolean checkForgotPasswordOTP(String username, String otp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        return otp.equals(user.getOtpCode()) && (user.getOtpExpiredDate() != null && user.getOtpExpiredDate().getTime() - System.currentTimeMillis() > 0);
    }

    @Override
    public void changeForgotPassword(String username, String password, String otp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        if (!otp.equals(user.getOtpCode()) ||
                (user.getOtpExpiredDate() == null || user.getOtpExpiredDate().getTime() - System.currentTimeMillis() <= 0)) {
            throw CommonException.of("OTP code is invalid or expired");
        }

        user.setPassword(encoder.encode(password));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 3);
        user.setPwdExpiredTime(calendar.getTime());
        userRepository.save(user);
    }

    @Override
    public void requestUnlockAccount(String username, String grantType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        if(!user.isLocked()) {
            throw CommonException.of("User {} is not locked", username);
        }

        Random random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1000000));
        user.setOtpCode(code);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 3);
        user.setOtpExpiredDate(calendar.getTime());
        userRepository.save(user);

        MailMessage message = new MailMessage();
        message.setTitle(String.format("Unlock account OTP [%s]", code));
        message.setBody("Dear user,<br/>This message is automatically sent, please do not reply directly!<br/>Ext: 26152");
        if ("INDIVIDUAL".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getEmail())) {
                throw CommonException.of("User {} does not have email. Please try with another way or contact with contact to ext 26152!", username);
            }
            notifyService.notifyToMail(message, "", user.getEmail());
        } else if ("ASSISTANT".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAssistant())) {
                throw CommonException.of("User {} does not have assistant. Please try with another way or contact with contact to ext 26152!", username);
            }
            Employee assistant = employeeRepository.findByEmployeeId(user.getAssistant())
                    .orElseThrow(() -> CommonException.of("Assistant {} of user {} not found", user.getAssistant(), username));
            if (StringUtils.isEmpty(assistant.getEmail())) {
                throw CommonException.of("Assistant {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", user.getAssistant(), username);
            }
            notifyService.notifyToMail(message, "", assistant.getEmail());
        } else if ("MANAGER".equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(user.getAllManagers())) {
                throw CommonException.of("User {} does not have manager. Please try with another way or contact with contact to ext 26152!", username);
            }
            String[] managers = user.getAllManagers().split(";");
            Employee manager = employeeRepository.findByEmployeeId(managers[0])
                    .orElseThrow(() -> CommonException.of("Manager {} of user {} not found", managers[0], username));
            if (StringUtils.isEmpty(manager.getEmail())) {
                throw CommonException.of("Manager {} of user {} does not have email. Please try with another way or contact with contact to ext 26152!", managers[0], username);
            }
            notifyService.notifyToMail(message, "", manager.getEmail());
        } else {
            throw CommonException.of("Grant type {} is not supported", grantType);
        }
    }

    @Override
    public void unlockAccount(String username, String otp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        if (!otp.equals(user.getOtpCode()) ||
                (user.getOtpExpiredDate() == null || user.getOtpExpiredDate().getTime() - System.currentTimeMillis() <= 0)) {
            throw CommonException.of("OTP code is invalid or expired");
        }

        user.setLocked(false);
        userRepository.save(user);
    }

    @Override
    @Deprecated
    public Optional<User> loadUserFromHRService(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("EmployeeID", username);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByUserID", HttpMethod.POST, entity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return Optional.empty();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
            NodeList nList = document.getElementsByTagName("ElistQuery");

            if (nList.getLength() == 0) {
                return Optional.empty();
            }

            Node node = nList.item(0);
            Element eElement = (Element) node;
            String name = eElement.getElementsByTagName("USER_NAME").item(0).getTextContent().trim();
            String hiredate = eElement.getElementsByTagName("HIREDATE").item(0).getTextContent().trim().replace("/", "");

            User user = new User();
            user.setUsername(username);
            user.setPassword(encoder.encode(hiredate));
            user.setName(name);
            user.setChineseName(name);

            if (eElement.getElementsByTagName("JOB_TITLE").getLength() > 0) {
                String title = eElement.getElementsByTagName("JOB_TITLE").item(0).getTextContent().trim();
                user.setTitle(title);
            }

            if (eElement.getElementsByTagName("CURRENT_OU_CODE").getLength() > 0) {
                String ouCode = eElement.getElementsByTagName("CURRENT_OU_CODE").item(0).getTextContent().trim();
                user.setOuCode(ouCode);
            }

            if (eElement.getElementsByTagName("CURRENT_OU_NAME").getLength() > 0) {
                String ouName = eElement.getElementsByTagName("CURRENT_OU_NAME").item(0).getTextContent().trim();
                user.setOuName(ouName);
            }

            if (eElement.getElementsByTagName("NOTES_ID").getLength() > 0) {
                String email = eElement.getElementsByTagName("NOTES_ID").item(0).getTextContent().trim();
                user.setEmail(email);
            }

            if (eElement.getElementsByTagName("ALL_MANAGERS").getLength() > 0) {
                String allManagers = eElement.getElementsByTagName("ALL_MANAGERS").item(0).getTextContent().trim();
                user.setAllManagers(allManagers);
            }

            if (eElement.getElementsByTagName("SITE_ALL_MANAGERS").getLength() > 0) {
                String siteAllManagers = eElement.getElementsByTagName("SITE_ALL_MANAGERS").item(0).getTextContent().trim();
                user.setSiteAllManagers(siteAllManagers);
            }

            if (eElement.getElementsByTagName("BU_ALL_MANAGERS").getLength() > 0) {
                String buAllManagers = eElement.getElementsByTagName("BU_ALL_MANAGERS").item(0).getTextContent().trim();
                user.setBuAllManagers(buAllManagers);
            }

            if (eElement.getElementsByTagName("UPPER_OU_CODE").getLength() > 0) {
                String upperOuCode = eElement.getElementsByTagName("UPPER_OU_CODE").item(0).getTextContent().trim();
                user.setUpperOuCode(upperOuCode);

            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            if (eElement.getElementsByTagName("HIREDATE").getLength() > 0) {
                String hireDate = eElement.getElementsByTagName("HIREDATE").item(0).getTextContent().trim();
                try {
                    user.setHireDate(df.parse(hireDate));
                } catch (Exception ee) {
                    log.error("convert date error");
                }
            }

            if (eElement.getElementsByTagName("LEAVEDAY").getLength() > 0) {
                String leaveDate = eElement.getElementsByTagName("LEAVEDAY").item(0).getTextContent().trim();
                try {
                    user.setLeaveDate(df.parse(leaveDate));
                } catch (Exception ee) {
                    log.error("convert date error");
                }
            }

            if (eElement.getElementsByTagName("USER_LEVEL").getLength() > 0) {
                String userLevel = eElement.getElementsByTagName("USER_LEVEL").item(0).getTextContent().trim();
                user.setLevel(userLevel);
            }

            if (eElement.getElementsByTagName("CARD_ID").getLength() > 0) {
                String cardId = eElement.getElementsByTagName("CARD_ID").item(0).getTextContent().trim();
                user.setCardId(cardId);
            }

            user.setActive(true);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 10);
            user.setInfoExpiredDate(calendar.getTime());
            calendar.add(Calendar.YEAR, -1);
            user.setPwdExpiredTime(calendar.getTime());
            Role defaultRole = new Role();
            defaultRole.setId(44);
            defaultRole.setRole("OAUTH_USER");
            user.setRoles(Collections.singletonList(defaultRole));

            loadAssistantFromHRService(user);

            userRepository.save(user);

            return Optional.of(user);
        } catch (Exception e) {
            log.error("### load user from hr service error", e);
            return Optional.empty();
        }
    }

    @Deprecated
    private void updatedInfoUserFromHRService(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("EmployeeID", user.getUsername());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByUserID", HttpMethod.POST, entity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
            NodeList nList = document.getElementsByTagName("ElistQuery");

            if (nList.getLength() == 0) {
                return;
            }

            Node node = nList.item(0);
            Element eElement = (Element) node;

            if (eElement.getElementsByTagName("JOB_TITLE").getLength() > 0) {
                String title = eElement.getElementsByTagName("JOB_TITLE").item(0).getTextContent().trim();
                user.setTitle(title);
            }

            if (eElement.getElementsByTagName("CURRENT_OU_CODE").getLength() > 0) {
                String ouCode = eElement.getElementsByTagName("CURRENT_OU_CODE").item(0).getTextContent().trim();
                user.setOuCode(ouCode);
            }

            if (eElement.getElementsByTagName("CURRENT_OU_NAME").getLength() > 0) {
                String ouName = eElement.getElementsByTagName("CURRENT_OU_NAME").item(0).getTextContent().trim();
                user.setOuName(ouName);
            }

            if (eElement.getElementsByTagName("NOTES_ID").getLength() > 0) {
                String email = eElement.getElementsByTagName("NOTES_ID").item(0).getTextContent().trim();
                user.setEmail(email);
            }

            if (eElement.getElementsByTagName("ALL_MANAGERS").getLength() > 0) {
                String allManagers = eElement.getElementsByTagName("ALL_MANAGERS").item(0).getTextContent().trim();
                user.setAllManagers(allManagers);
            }

            if (eElement.getElementsByTagName("SITE_ALL_MANAGERS").getLength() > 0) {
                String siteAllManagers = eElement.getElementsByTagName("SITE_ALL_MANAGERS").item(0).getTextContent().trim();
                user.setSiteAllManagers(siteAllManagers);
            }

            if (eElement.getElementsByTagName("BU_ALL_MANAGERS").getLength() > 0) {
                String buAllManagers = eElement.getElementsByTagName("BU_ALL_MANAGERS").item(0).getTextContent().trim();
                user.setBuAllManagers(buAllManagers);
            }

            if (eElement.getElementsByTagName("UPPER_OU_CODE").getLength() > 0) {
                String upperOuCode = eElement.getElementsByTagName("UPPER_OU_CODE").item(0).getTextContent().trim();
                user.setUpperOuCode(upperOuCode);

            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            if (eElement.getElementsByTagName("HIREDATE").getLength() > 0) {
                String hireDate = eElement.getElementsByTagName("HIREDATE").item(0).getTextContent().trim();
                try {
                    user.setHireDate(df.parse(hireDate));
                } catch (Exception ee) {
                    log.error("convert date error");
                }
            }

            if (eElement.getElementsByTagName("LEAVEDAY").getLength() > 0) {
                String leaveDate = eElement.getElementsByTagName("LEAVEDAY").item(0).getTextContent().trim();
                try {
                    user.setLeaveDate(df.parse(leaveDate));
                } catch (Exception ee) {
                    log.error("convert date error");
                }
            }

            if (eElement.getElementsByTagName("USER_LEVEL").getLength() > 0) {
                String userLevel = eElement.getElementsByTagName("USER_LEVEL").item(0).getTextContent().trim();
                user.setLevel(userLevel);
            }

            if (eElement.getElementsByTagName("CARD_ID").getLength() > 0) {
                String cardId = eElement.getElementsByTagName("CARD_ID").item(0).getTextContent().trim();
                user.setCardId(cardId);
            }

            if (user.getLeaveDate() != null && user.getLeaveDate().getTime() < System.currentTimeMillis()) {
                user.setActive(false);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 10);
            user.setInfoExpiredDate(calendar.getTime());

            loadAssistantFromHRService(user);

            userRepository.save(user);
        } catch (Exception e) {
            log.error("### load user from hr service error", e);
        }
    }

    @Deprecated
    public void loadAssistantFromHRService(User user) {
        if (!StringUtils.isEmpty(user.getOuCode())) {
            String assist = loadAssistantFromHRService(user.getOuCode());
            if (!StringUtils.isEmpty(assist)) {
                user.setAssistant(assist);
                return;
            }

            if (StringUtils.isEmpty(user.getUpperOuCode())) {
                return;
            }
            String[] ouCodeUppers = user.getUpperOuCode().split(">");
            for (int i= ouCodeUppers.length -1; i >=0; i--) {
                assist = loadAssistantFromHRService(ouCodeUppers[i]);
                if (!StringUtils.isEmpty(assist)) {
                    user.setAssistant(assist);
                    break;
                }
            }
        }
    }

    @Deprecated
    private String loadAssistantFromHRService(String ouCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("CompanyCode", "VN");
        map.add("OUCode", ouCode);
        map.add("Level", "100");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByOUCode_Assistant", HttpMethod.POST, entity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
            NodeList nList = document.getElementsByTagName("ElistQuery");

            if (nList.getLength() == 0) {
                return null;
            }

            Node node = nList.item(0);
            Element eElement = (Element) node;
            return eElement.getElementsByTagName("USER_ID").item(0).getTextContent().trim();
        } catch (Exception e) {
            log.error("### load assistant from hr service error", e);
        }

        return null;
    }

    @Override
    public Boolean updateInformation(String username, CreateUser user) {
        User existUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("### updateInformation user {} not found", username);
                    return new UsernameNotFoundException(String.format("User %s not found", username));
                });

        BeanUtils.copyPropertiesIgnoreNull(user, existUser, "id", "roles");

        userRepository.save(existUser);

        return true;
    }

    @Override
    public Boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            log.error("### changePassword user {} not found", username);
            throw CommonException.of("User {} not found", username);
        }

        if (!encoder.matches(oldPassword, userOptional.get().getPassword())) {
            log.error("### changePassword user {} invalid password", username);
            throw CommonException.of("{} invalid password", username);
        }

        userOptional.get().setPassword(encoder.encode(newPassword));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 3);
        userOptional.get().setPwdExpiredTime(calendar.getTime());
        userRepository.save(userOptional.get());

        return true;
    }

    @Override
    public Page<User> getUserList(String username, String role, List<String> systems, Pageable pageable) {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(role) && systems.isEmpty()) {
            return userRepository.findAll(pageable);
        } else if (!StringUtils.isEmpty(username)) {
            Optional<User> userOption = userRepository.findByUsername(username);
            if (userOption.isPresent()) {
                return new PageImpl<>(Collections.singletonList(userOption.get()));
            }
            return Page.empty();
        } else if (StringUtils.isEmpty(role)) {
            List<String> roleList = roleRepository.findBySystemIn(systems).stream().map(Role::getRole).collect(Collectors.toList());
            return userRepository.findByRoleIn(roleList, pageable);
        } else {
            return userRepository.findByRole(role, pageable);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }
}
