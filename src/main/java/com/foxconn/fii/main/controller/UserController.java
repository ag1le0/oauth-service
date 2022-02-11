package com.foxconn.fii.main.controller;

import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.response.CommonResponse;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.model.CreateUser;
import com.foxconn.fii.main.data.model.SignUpUser;
import com.foxconn.fii.main.service.UserService;
import com.foxconn.fii.security.config.OAuth2Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserApiController userApiController;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Principal user(Principal principal) {
        OAuth2Authentication authentication = (OAuth2Authentication) principal;
        String username = userService.getCurrentUsername();
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            log.error("### get user information {} not found", username);
            throw CommonException.of("User %s not found", username);
        }
        return new OAuth2Principal(authentication, userOptional.get());
    }

    @PostMapping("/sign-up")
    public CommonResponse<Boolean> signUp(@Validated @RequestBody SignUpUser signUpUser) {
        return userApiController.signUp(signUpUser);
    }

    @GetMapping("/information")
    public CommonResponse<User> getUserInformation() {
        return userApiController.getUserInformation();
    }

    @PostMapping("/update-information")
    public CommonResponse<Boolean> updateUserInformation(@RequestBody CreateUser createUser) {
        return userApiController.updateUserInformation(createUser);
    }

    @PostMapping("/update-avatar")
    public CommonResponse<Boolean> updateUserAvatar(MultipartFile file) {
        return userApiController.updateUserAvatar(file);
    }

    @PostMapping("/change-password")
    public CommonResponse<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        return userApiController.changePassword(oldPassword, newPassword);
    }

}
