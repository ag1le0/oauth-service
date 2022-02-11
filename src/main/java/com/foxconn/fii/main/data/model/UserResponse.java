package com.foxconn.fii.main.data.model;

import com.foxconn.fii.common.utils.BeanUtils;
import com.foxconn.fii.main.data.entity.User;
import lombok.Data;

@Data
public class UserResponse {

    private String username;

    private String name;

    private String chineseName;

    private String email;

    public static UserResponse of (User user) {
        UserResponse ins = new UserResponse();
        BeanUtils.copyPropertiesIgnoreNull(user, ins);
        return ins;
    }
}
