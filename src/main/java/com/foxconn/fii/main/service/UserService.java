package com.foxconn.fii.main.service;

import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.model.CreateUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    Optional<User> loadUserFromHRService(String username);

    void loadAssistantFromHRService(User user);

    void increaseFailedLoginNumber(String username);

    void resetFailedLoginNumber(String username);

    String getCurrentUsername();

    User getCurrentUser();

    boolean isCurrentAdmin();

    void checkOTP(String username, String mac, String otp);

    String getEmail(String username, String grantType);

    void resetPassword(String username, String identityNo);

    void requestForgotPassword(String username, String grantType, String email);

    boolean checkForgotPasswordOTP(String username, String otp);

    void changeForgotPassword(String username, String password, String otp);

    void requestUnlockAccount(String username, String grantType);

    void unlockAccount(String username, String otp);

    Boolean updateInformation(String username, CreateUser user);

    Boolean changePassword(String username, String oldPassword, String newPassword);


    Page<User> getUserList(String username, String role, List<String> systems, Pageable pageable);

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    void save(User user);
}
