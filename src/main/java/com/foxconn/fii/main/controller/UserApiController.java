package com.foxconn.fii.main.controller;

import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.response.CommonResponse;
import com.foxconn.fii.common.utils.CommonUtils;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.model.CreateUser;
import com.foxconn.fii.main.data.model.SignUpUser;
import com.foxconn.fii.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Value("${path.data}")
    private String dataPath;

    @PostMapping("/sign-up")
    public CommonResponse<Boolean> signUp(@Validated @RequestBody SignUpUser signUpUser) {
        userService.findByUsername(signUpUser.getUsername())
                .ifPresent(user -> {
                    throw CommonException.of("username {} exist", signUpUser.getUsername());
                });

        User user = new User();
        user.setUsername(signUpUser.getUsername());
        user.setPassword(encoder.encode(signUpUser.getPassword()));
        userService.save(user);

        return CommonResponse.success(true);
    }

    @PostMapping("/reset-password")
    public CommonResponse<Boolean> resetPassword(
            @RequestParam String username,
            @RequestParam String identityNo) {
        userService.resetPassword(username, identityNo);
        return CommonResponse.success(true);
    }

    @GetMapping("/get-email")
    public CommonResponse<String> getForgotPasswordEmail(
            @RequestParam String username,
            @RequestParam(defaultValue = "INDIVIDUAL") String grantType) {
        String email = userService.getEmail(username, grantType);
        return CommonResponse.success(email);
    }

    @GetMapping("/forgot-password")
    public CommonResponse<Boolean> requestForgotPassword(
            @RequestParam String username,
            @RequestParam(defaultValue = "INDIVIDUAL") String grantType,
            @RequestParam(required = false) String email) {
        userService.requestForgotPassword(username, grantType, email);
        return CommonResponse.success(true);
    }

    @GetMapping("/forgot-password/check-otp")
    public CommonResponse<Boolean> forgotPasswordCheckOTP(
            @RequestParam String username,
            @RequestParam String otp) {
        boolean valid = userService.checkForgotPasswordOTP(username, otp);
        return CommonResponse.success(valid);
    }

    @PostMapping("/forgot-password")
    public CommonResponse<Boolean> changeForgotPassword(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String otp) {

        if (CommonUtils.calculatePasswordStrength(password) < 6) {
            throw CommonException.of("New password is weak. Password should contain at least one lowercase, uppercase, number and special character");
        }

        userService.changeForgotPassword(username, password, otp);
        return CommonResponse.success(true);
    }

    @GetMapping("/unlock-account")
    public CommonResponse<Boolean> requestUnlockAccount(
            @RequestParam String username,
            @RequestParam(defaultValue = "INDIVIDUAL") String grantType,
            @RequestParam(required = false) String email) {
        userService.requestUnlockAccount(username, grantType);
        return CommonResponse.success(true);
    }

    @PostMapping("/unlock-account")
    public CommonResponse<Boolean> unlockAccount(
            @RequestParam String username,
            @RequestParam String otp) {
        userService.unlockAccount(username, otp);
        return CommonResponse.success(true);
    }

    @GetMapping("/information")
    public CommonResponse<User> getUserInformation() {
        String username = userService.getCurrentUsername();
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            log.error("### get user information {} not found", username);
            throw CommonException.of("User %s not found", username);
        }

        return CommonResponse.success(userOptional.get());
    }

    @PostMapping("/update-information")
    public CommonResponse<Boolean> updateUserInformation(@RequestBody CreateUser createUser) {
        String username = userService.getCurrentUsername();
        Boolean result = userService.updateInformation(username, createUser);
        return CommonResponse.success(result);
    }

    @PostMapping("/update-avatar")
    public CommonResponse<Boolean> updateUserAvatar(MultipartFile file) {
        String username = userService.getCurrentUsername();

        Optional<User> optionalUser = userService.findByUsername(username);
        if (!optionalUser.isPresent()) {
            log.error("### get user information {} not found", username);
            throw CommonException.of("User %s not found", username);
        }

        String subFolder = "image/";
        String fileName = System.currentTimeMillis() + '-' + new Random().nextInt(100) + ".jpg";
        File localFile = new File(dataPath + subFolder + fileName);

        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            log.error("### upload error", e);
        }

        optionalUser.get().setAvatar("/assets/" + fileName);
        userService.save(optionalUser.get());

        return CommonResponse.success(true);
    }

    @PostMapping("/change-password")
    public CommonResponse<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        if (newPassword.equals(oldPassword)) {
            throw CommonException.of("New password and old password are same");
        }

        if (CommonUtils.calculatePasswordStrength(newPassword) < 6) {
            throw CommonException.of("New password is weak. Password should contain at least one lowercase, uppercase, number and special character");
        }

        String username = userService.getCurrentUsername();
        Boolean result = userService.changePassword(username, oldPassword, newPassword);
        return CommonResponse.success(result);
    }
}
