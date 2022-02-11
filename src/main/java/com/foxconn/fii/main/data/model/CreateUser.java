package com.foxconn.fii.main.data.model;

import com.foxconn.fii.main.data.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class CreateUser {

    private String username;

    private String name;

    private String chineseName;

    private String email;

    private String bu;

    private String cft;

    private String factory;

    private String department;

    private String title;

    private String level;

    private List<Role> roles;
}
