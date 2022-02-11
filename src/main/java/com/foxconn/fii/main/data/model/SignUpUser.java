package com.foxconn.fii.main.data.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignUpUser {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
