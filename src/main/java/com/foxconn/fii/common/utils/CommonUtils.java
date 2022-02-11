package com.foxconn.fii.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonUtils {

    public boolean checkEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public int calculatePasswordStrength(String password) {
        int score = 0;

        if (password.length() < 8) {
            return 0;
        } else if (password.length() >= 10) {
            score += 2;
        } else {
            score += 1;
        }

        if (password.matches("(?=.*[0-9].*[0-9]).*")) {
            score += 2;
        } else if (password.matches("(?=.*[0-9]).*")) {
            score += 1;
        }

        if (password.matches("(?=.*[a-z].*[a-z]).*")) {
            score += 2;
        } else if (password.matches("(?=.*[a-z]).*")) {
            score += 1;
        }

        if (password.matches("(?=.*[A-Z].*[A-Z]).*")) {
            score += 2;
        } else if (password.matches("(?=.*[A-Z]).*")) {
            score += 1;
        }

        if (password.matches("(?=.*[~!@#$%^&*()_-].*[~!@#$%^&*()_-]).*")) {
            score += 2;
        } else if (password.matches("(?=.*[~!@#$%^&*()_-]).*")) {
            score += 1;
        }

        return score;
    }
}
