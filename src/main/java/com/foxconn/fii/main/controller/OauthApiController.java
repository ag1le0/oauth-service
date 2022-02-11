package com.foxconn.fii.main.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.response.CommonResponse;
import com.foxconn.fii.common.utils.CommonUtils;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.entity.UserRecognize;
import com.foxconn.fii.main.data.model.RecognizeResponse;
import com.foxconn.fii.main.data.model.UserResponse;
import com.foxconn.fii.main.data.repository.UserRecognizeRepository;
import com.foxconn.fii.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/oauth")
public class OauthApiController {

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    @Autowired
    private TokenEnhancer accessTokenEnhancer;

    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserRecognizeRepository userRecognizeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${path.data}")
    private String dataPath;

    @Value("${security.recognize.url}")
    private String recognizeUrl;

    @PostMapping(value = "/recognize")
    public OAuth2AccessToken recognize(
            @RequestPart MultipartFile image,
            @RequestParam(defaultValue = "") String mac,
            @RequestParam(defaultValue = "") String otp,
            @RequestParam(defaultValue = "") String version) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        log.debug("### recognize START");
        Resource file;
        try {
            file = createTempFile(image.getBytes());
        } catch (Exception e) {
            log.error("### recognize error", e);
            throw new CommonException("Image is not recognized");
        }
        log.debug("### recognize upload image END");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("image", file);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<RecognizeResponse> responseEntity;

        responseEntity = restTemplate.exchange(recognizeUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<RecognizeResponse>() {
        });
        log.debug("### recognize IT server END");

        if (responseEntity.getBody() == null || responseEntity.getBody().getPerson() == null) {
            throw new CommonException("response of recognize invalid");
        }
        String tag = responseEntity.getBody().getPerson().getTag();

        Map<String, Object> tagObject;
        try {
            tagObject = objectMapper.readValue(tag, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("### recognize error", e);
            throw new CommonException("Image is not recognized");
        }

        String username = (String) tagObject.getOrDefault("name", "");

        ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId("ws-system");
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("grant_type", "password");
        requestParams.put("username", username);
        OAuth2Request oAuth2Request = new OAuth2Request(requestParams, authenticatedClient.getClientId(), authenticatedClient.getAuthorities(), true, authenticatedClient.getScope(), authenticatedClient.getResourceIds(), null, null, null);

        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
        authentication.setDetails(requestParams);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        defaultTokenServices.setTokenEnhancer(accessTokenEnhancer);

        userService.findByUsername(username).ifPresent(
                user -> {
                    UserRecognize recognize = new UserRecognize();
                    recognize.setUser(user);
                    recognize.setRecognizeImageUrl("/ws-data/image/recognize/" + file.getFilename());
                    userRecognizeRepository.save(recognize);
                }
        );

//        if (!"agent".equalsIgnoreCase(version)) {
//            userService.checkOTP(username, mac, otp);
//        }

        log.debug("### recognize END");
        return defaultTokenServices.createAccessToken(oAuth2Authentication);
    }

    @PostMapping(value = "/recognize-information")
    public CommonResponse<UserResponse> recognize(
            @RequestPart MultipartFile image) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        log.debug("### recognize START");
        Resource file;
        try {
            file = createTempFile(image.getBytes());
        } catch (Exception e) {
            log.error("### recognize error", e);
            throw new CommonException("Image is not recognized");
        }
        log.debug("### recognize upload image END");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("image", file);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<RecognizeResponse> responseEntity;

        responseEntity = restTemplate.exchange(recognizeUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<RecognizeResponse>() {
        });
        log.debug("### recognize IT server END");

        if (responseEntity.getBody() == null || responseEntity.getBody().getPerson() == null) {
            throw new CommonException("response of recognize invalid");
        }
        String tag = responseEntity.getBody().getPerson().getTag();

        Map<String, Object> tagObject;
        try {
            tagObject = objectMapper.readValue(tag, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("### recognize error", e);
            throw new CommonException("Image is not recognized");
        }

        String username = (String) tagObject.getOrDefault("name", "");

        User user = userService.findByUsername(username)
                .orElseThrow(() -> CommonException.of("User {} not found", username));

        return CommonResponse.success(UserResponse.of(user));
    }


    @PostMapping(value = "/user/information")
    public CommonResponse<UserDetails> getUserInformation(String username) {
        return CommonResponse.success(userService.loadUserByUsername(username));
    }

    @PostMapping("/user/change-password")
    public CommonResponse<Boolean> changePassword(
            @RequestParam String username,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        if (StringUtils.isEmpty(username)) {
            throw CommonException.of("Username is not blank");
        }

        if (newPassword.equals(oldPassword)) {
            throw CommonException.of("New password and old password are same");
        }

        if (CommonUtils.calculatePasswordStrength(newPassword) < 6) {
            throw CommonException.of("New password is weak. Password should contain at least one lowercase, uppercase, number and special character");
        }

        Boolean result = userService.changePassword(username, oldPassword, newPassword);
        return CommonResponse.success(result);
    }

    private Resource createTempFile(byte[] bytes) throws IOException {
        String filename = UUID.randomUUID().toString().replace("-", "");

        Path recognizeDict = Paths.get(dataPath, "/recognize");
        if (!Files.exists(recognizeDict)) {
            Files.createDirectory(recognizeDict);
        }
        Path tempFile = Paths.get(dataPath, "/recognize", "/" + filename);
        Files.write(tempFile, bytes);
        return new FileSystemResource(tempFile.toFile());
    }
}
