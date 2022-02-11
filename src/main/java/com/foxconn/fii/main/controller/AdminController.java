package com.foxconn.fii.main.controller;

import com.foxconn.fii.common.exception.CommonException;
import com.foxconn.fii.common.response.CommonResponse;
import com.foxconn.fii.common.response.ListResponse;
import com.foxconn.fii.common.utils.BeanUtils;
import com.foxconn.fii.main.data.entity.Role;
import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.entity.UserSystem;
import com.foxconn.fii.main.data.model.CreateUser;
import com.foxconn.fii.main.data.repository.RoleRepository;
import com.foxconn.fii.main.data.repository.UserSystemRepository;
import com.foxconn.fii.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mng")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserSystemRepository userSystemRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

//    @PostMapping("/role")
//    public CommonResponse<Role> createNewRole(@RequestParam String role) {
//        if (userService.isCurrentAdmin()) {
//            return CommonResponse.success(roleRepository.findByRole(role)
//                    .orElseGet(() -> {
//                        Role ins = new Role();
//                        ins.setRole(role);
//                        roleRepository.save(ins);
//                        return ins;
//                    }));
//        }
//        throw CommonException.of("You don't have permission");
//    }

    @GetMapping("/role")
    public ListResponse<Role> getRoleList() {
        if (userService.isCurrentAdmin()) {
            return ListResponse.success(roleRepository.findAll());
        } else {
            String currentUsername = userService.getCurrentUsername();
            User user = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> CommonException.of("User {} not found", currentUsername));
            return ListResponse.success(roleRepository.findBySystemIn(userSystemRepository.findSystemByUser(user)));
        }
    }

    @GetMapping("/user")
    public ListResponse<User> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String system,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Page<User> userPage;
        if (userService.isCurrentAdmin()) {
            userPage = userService.getUserList(username, role, StringUtils.isEmpty(system) ? Collections.emptyList() : Collections.singletonList(system), PageRequest.of(page, size));
        } else {
            String currentUsername = userService.getCurrentUsername();
            User user = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> CommonException.of("User {} not found", currentUsername));

            List<String> systems = userSystemRepository.findSystemByUser(user);
            if (!StringUtils.isEmpty(system)) {
                systems.remove(system);
            }
            userPage = userService.getUserList(username, role, systems, PageRequest.of(page, size));
        }

        return ListResponse.success(userPage);
    }

    @PostMapping("/user")
    public CommonResponse<Boolean> createUser(@RequestBody CreateUser user) {
        if (!userService.isCurrentAdmin()) {
            String currentUsername = userService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> CommonException.of("User {} not found", currentUsername));
            List<String> systems = userSystemRepository.findSystemByUser(currentUser);
            List<Integer> roleList = roleRepository.findBySystemIn(systems).stream().map(Role::getId).collect(Collectors.toList());
            for (Role role : user.getRoles()) {
                if (!roleList.contains(role.getId())) {
                    throw CommonException.of("Can't create user with role {}", role.getId());
                }
            }
        }

        Optional<User> existUser = userService.findByUsername(user.getUsername());
        List<UserSystem> systems = new ArrayList<>();

        if (existUser.isPresent()) {
            for (Role role : user.getRoles()) {
                Optional<Role> tmp = roleRepository.findById(role.getId());
                if (tmp.isPresent()) {
                    if (!existUser.get().getRoles().contains(tmp.get())) {
                        existUser.get().getRoles().add(tmp.get());
                        if (!"oauth-service".equals(tmp.get().getSystem())) {
                            UserSystem userSystem = new UserSystem();
                            userSystem.setUser(existUser.get());
                            userSystem.setSystem(tmp.get().getSystem());
                            systems.add(userSystem);
                        }
                    }
                }
            }
            userService.save(existUser.get());
            userSystemRepository.saveAll(systems);

            return CommonResponse.success(true);
        }

        User newUser = new User();
        BeanUtils.copyPropertiesIgnoreNull(user, newUser);
        newUser.setPassword(encoder.encode(User.DEFAULT_PASSWORD));

        for (Role role : newUser.getRoles()) {
            Optional<Role> tmp = roleRepository.findById(role.getId());
            if (tmp.isPresent()) {
                if (!"oauth-service".equals(tmp.get().getSystem())) {
                    UserSystem userSystem = new UserSystem();
                    userSystem.setUser(newUser);
                    userSystem.setSystem(tmp.get().getSystem());
                    systems.add(userSystem);
                }
            }
        }
        userService.save(newUser);
        userSystemRepository.saveAll(systems);

        return CommonResponse.success(true);
    }

    @PostMapping("/user/{id}/add-role")
    public CommonResponse<Boolean> addUserRole(@PathVariable int id, @RequestParam String role) {
        if (!userService.isCurrentAdmin()) {
            String currentUsername = userService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> CommonException.of("User {} not found", currentUsername));
            List<String> systems = userSystemRepository.findSystemByUser(currentUser);
            List<String> roleList = roleRepository.findBySystemIn(systems).stream().map(Role::getRole).collect(Collectors.toList());
            if (!roleList.contains(role)) {
                throw CommonException.of("Can't add role user with role {}", role);
            }
        }

        User user = userService.findById(id).orElseThrow(() -> CommonException.of("user {} not found", id));
        Role roleDB = roleRepository.findByRole(role).orElseThrow(() -> CommonException.of("role {} do not exist", role));

        if (!user.getRoles().contains(roleDB)) {
            user.getRoles().add(roleDB);
            List<String> systems = userSystemRepository.findSystemByUser(user);
            if (!systems.contains(roleDB.getSystem())) {
                systems.add(roleDB.getSystem());
                if (!"oauth-service".equals(roleDB.getSystem())) {
                    UserSystem userSystem = new UserSystem();
                    userSystem.setUser(user);
                    userSystem.setSystem(roleDB.getSystem());
                    userSystemRepository.save(userSystem);
                }
            }
            userService.save(user);
        }

        return CommonResponse.success(true);
    }

    @PostMapping("/user/{id}/remove-role")
    public CommonResponse<Boolean> removeUserRole(@PathVariable int id, @RequestParam String role) {
        if (!userService.isCurrentAdmin()) {
            String currentUsername = userService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> CommonException.of("User {} not found", currentUsername));
            List<String> systems = userSystemRepository.findSystemByUser(currentUser);
            List<String> roleList = roleRepository.findBySystemIn(systems).stream().map(Role::getRole).collect(Collectors.toList());
            if (!roleList.contains(role)) {
                throw CommonException.of("Can't remove role user with role {}", role);
            }
        }

        User user = userService.findById(id).orElseThrow(() -> CommonException.of("user {} not found", id));
        Role roleDB = roleRepository.findByRole(role).orElseThrow(() -> CommonException.of("role {} do not exist", role));

        if (user.getRoles().contains(roleDB)) {
            user.getRoles().remove(roleDB);
            boolean delete = true;
            for (Role tmp : user.getRoles()) {
                if (roleDB.getSystem().equals(tmp.getSystem())) {
                    delete = false;
                    break;
                }
            }
            if (delete) {
                userSystemRepository.findByUserAndSystem(user, roleDB.getSystem()).ifPresent(us -> userSystemRepository.delete(us));
            }
            userService.save(user);
        }

        return CommonResponse.success(true);
    }

    @PostMapping("/user/{id}/lock")
    public CommonResponse<Boolean> lockUser(@PathVariable int id) {
        User user = userService.findById(id).orElseThrow(() -> CommonException.of("user {} not found", id));

        user.setActive(false);
        userService.save(user);

        return CommonResponse.success(true);
    }

    @PostMapping("/user/{id}/unlock")
    public CommonResponse<Boolean> unlockUser(@PathVariable int id) {
        User user = userService.findById(id).orElseThrow(() -> CommonException.of("user {} not found", id));

        user.setActive(true);
        user.setFailedLoginNumber(0);
        userService.save(user);

        return CommonResponse.success(true);
    }

    @PostMapping("/user/{id}/reset-password")
    public CommonResponse<Boolean> resetPassword(@PathVariable int id) {
        User user = userService.findById(id).orElseThrow(() -> CommonException.of("user {} not found", id));

        user.setPassword(encoder.encode(User.DEFAULT_PASSWORD));
        user.setActive(true);
        user.setFailedLoginNumber(0);
        userService.save(user);

        return CommonResponse.success(true);
    }
}
