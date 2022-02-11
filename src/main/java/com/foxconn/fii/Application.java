package com.foxconn.fii;

import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.repository.DepartmentRepository;
import com.foxconn.fii.main.data.repository.EmployeeRepository;
import com.foxconn.fii.main.data.repository.UserRepository;
import com.foxconn.fii.main.service.UserService;
import com.foxconn.fii.security.config.OAuth2Principal;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@RestController
public class Application extends SpringBootServletInitializer implements CommandLineRunner {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+7:00"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("/greeting")
    public String greeting() {
        return "Welcome to oauth service!\n--- VN FII Team ---";
    }

    @GetMapping("/me")
    public ResponseEntity<Principal> get(final Principal principal) {
        OAuth2Authentication authentication = (OAuth2Authentication) principal;
        String username = userService.getCurrentUsername();
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            log.error("### get user information {} not found", username);
            throw CommonException.of("User %s not found", username);
        }
        return ResponseEntity.ok(new OAuth2Principal(authentication, userOptional.get()));
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        /*
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            if (!StringUtils.isEmpty(user.getLevel()) && Integer.parseInt(user.getLevel()) >= 10) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("ManagerID", user.getUsername());

                HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByManagerID_OU", HttpMethod.POST, entity, String.class);

                if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                    continue;
                }

                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
                    NodeList nList = document.getElementsByTagName("ElistQuery");
                    List<String> ouCodeList = new ArrayList<>();
                    for (int i = 0;i<nList.getLength(); i++) {
                        Node node = nList.item(i);
                        Element eElement = (Element) node;

                        if (eElement.getElementsByTagName("OU_CODE").getLength() > 0) {
                            String ouCode = eElement.getElementsByTagName("OU_CODE").item(0).getTextContent().trim();
                            ouCodeList.add(ouCode);
                        }
                    }

                    if (!ouCodeList.isEmpty()) {
                        user.setLowerOuCode(String.join(",", ouCodeList));
                        userRepository.save(user);
                    }
                } catch (Exception e) {
                    log.error("### load user from hr service error", e);
                }
            }
        }
        */

        /*
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("EmployeeID", user.getUsername());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByUserID", HttpMethod.POST, entity, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                continue;
            }

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
                NodeList nList = document.getElementsByTagName("ElistQuery");
                Node node = nList.item(0);
                Element eElement = (Element) node;
                String name = eElement.getElementsByTagName("USER_NAME").item(0).getTextContent().trim();
                String hiredate = eElement.getElementsByTagName("HIREDATE").item(0).getTextContent().trim().replace("/", "");

                user.setName(name);
                user.setChineseName(name);

                if (eElement.getElementsByTagName("JOB_TITLE").getLength() > 0) {
                    String title = eElement.getElementsByTagName("JOB_TITLE").item(0).getTextContent().trim();
                    user.setTitle(title);
                }

                if (eElement.getElementsByTagName("NOTES_ID").getLength() > 0) {
                    String email = eElement.getElementsByTagName("NOTES_ID").item(0).getTextContent().trim();
                    user.setEmail(email);
                }

                if (eElement.getElementsByTagName("CURRENT_OU_CODE").getLength() > 0) {
                    String ouCode = eElement.getElementsByTagName("CURRENT_OU_CODE").item(0).getTextContent().trim();
                    user.setOuCode(ouCode);
                }

                if (eElement.getElementsByTagName("CURRENT_OU_NAME").getLength() > 0) {
                    String ouName = eElement.getElementsByTagName("CURRENT_OU_NAME").item(0).getTextContent().trim();
                    user.setOuName(ouName);
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

                userRepository.save(user);

            } catch (Exception e) {
                log.error("### load user from hr service error", e);
            }
        }
        */

        /*
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            userService.loadAssistantFromHRService(user);
            userRepository.save(user);
        }
        */

        /*
        XSSFWorkbook workbook = new XSSFWorkbook("C:\\Users\\V0946495\\Desktop\\user ESOP.xlsx");
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            String username = (sheet.getRow(i).getCell(0).getCellType() == CellType.STRING) ? sheet.getRow(i).getCell(0).getStringCellValue() : Double.valueOf(sheet.getRow(i).getCell(0).getNumericCellValue()).intValue() + "";
            String email1 = sheet.getRow(i).getCell(1).getStringCellValue();
            String password1 = (sheet.getRow(i).getCell(2).getCellType() == CellType.STRING) ? sheet.getRow(i).getCell(2).getStringCellValue() : Double.valueOf(sheet.getRow(i).getCell(2).getNumericCellValue()).intValue() + "";
            String vnName1 = sheet.getRow(i).getCell(3).getStringCellValue();
            String cnName1 = sheet.getRow(i).getCell(4).getStringCellValue();

            log.info("### {}", username);

            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (!optionalUser.isPresent()) {
                User user = new User();
                user.setUsername(username);
                user.setName(vnName1);
                user.setPassword(passwordEncoder.encode(password1));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("EmployeeID", username);

                HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        "http://10.132.37.98:8006/personal/ElistQuery.asmx/ByUserID", HttpMethod.POST, entity, String.class);

                if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                    continue;
                }

                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(responseEntity.getBody())));
                    NodeList nList = document.getElementsByTagName("ElistQuery");
                    Node node = nList.item(0);
                    Element eElement = (Element) node;
                    if (eElement == null) {
                        if (StringUtils.isEmpty(email1) && !"NULL".equalsIgnoreCase(email1)) {
                            user.setEmail(email1);
                        }
                        user.setActive(true);
                        user.setPwdExpiredTime(new Date());
                        Role defaultRole = new Role();
                        defaultRole.setId(44);
                        defaultRole.setRole("OAUTH_USER");
                        user.setRoles(Collections.singletonList(defaultRole));
                        userRepository.save(user);
                        continue;
                    }

                    if (eElement.getElementsByTagName("USER_NAME").getLength() > 0) {
                        String name = eElement.getElementsByTagName("USER_NAME").item(0).getTextContent().trim();
                        user.setChineseName(name);
                    }

                    if (eElement.getElementsByTagName("JOB_TITLE").getLength() > 0) {
                        String title = eElement.getElementsByTagName("JOB_TITLE").item(0).getTextContent().trim();
                        user.setTitle(title);
                    }

                    if (eElement.getElementsByTagName("NOTES_ID").getLength() > 0) {
                        String email = eElement.getElementsByTagName("NOTES_ID").item(0).getTextContent().trim();
                        user.setEmail(email);
                    } else {
                        if (StringUtils.isEmpty(email1) && !"NULL".equalsIgnoreCase(email1)) {
                            user.setEmail(email1);
                        }
                    }

                    if (eElement.getElementsByTagName("CURRENT_OU_CODE").getLength() > 0) {
                        String ouCode = eElement.getElementsByTagName("CURRENT_OU_CODE").item(0).getTextContent().trim();
                        user.setOuCode(ouCode);
                    }

                    if (eElement.getElementsByTagName("CURRENT_OU_NAME").getLength() > 0) {
                        String ouName = eElement.getElementsByTagName("CURRENT_OU_NAME").item(0).getTextContent().trim();
                        user.setOuName(ouName);
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
                    user.setPwdExpiredTime(new Date());
                    Role defaultRole = new Role();
                    defaultRole.setId(44);
                    defaultRole.setRole("OAUTH_USER");
                    user.setRoles(Collections.singletonList(defaultRole));
                    userRepository.save(user);

                } catch (Exception e) {
                    log.error("### load user from hr service {} error", username, e);
                }
            }
        }
        */

//        List<Employee> employeeList = employeeRepository.findAll();
//        for (Employee employee : employeeList) {
//            Optional<User> userOptional = userRepository.findByUsername(employee.getEmployeeId());
//            if (!userOptional.isPresent()) {
//                User ins = new User();
//                BeanUtils.copyPropertiesIgnoreNull(employee, ins, "id");
//                ins.setUsername(employee.getEmployeeId());
//
//                if (employee.getHireDate() != null) {
//                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//                    ins.setPassword(encoder.encode(df.format(employee.getHireDate())));
//                } else {
//                    log.debug("### default password {}", employee.getEmployeeId());
//                    ins.setPassword(encoder.encode(User.DEFAULT_PASSWORD));
//                }
//
//                ins.setActive(employee.getStatus() != 0);
//                Calendar calendar = Calendar.getInstance();
//                calendar.add(Calendar.DAY_OF_YEAR, 3);
//                ins.setInfoExpiredDate(calendar.getTime());
//                calendar.add(Calendar.YEAR, -1);
//                ins.setPwdExpiredTime(calendar.getTime());
//                Role defaultRole = new Role();
//                defaultRole.setId(44);
//                defaultRole.setRole("OAUTH_USER");
//                ins.setRoles(Collections.singletonList(defaultRole));
//
//                List<Department> department = departmentRepository.findByOuCodeAndLevel(ins.getOuCode(), "1");
//                if (!department.isEmpty()) {
//                    ins.setAssistant(department.get(0).getEmployeeId());
//                }
//
//                userRepository.save(ins);
//            }
//        }

    }
}
