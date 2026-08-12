package com.example.bankbff.entity;

import com.example.bankbff.dto.UserInfoDto;

public class UserInfoDtoContext {
    private final UserInfoDto userInfoDto;

    public UserInfoDtoContext(UserInfoDto userInfoDto) {
        this.userInfoDto = userInfoDto;
    }

    public UserInfoDto getUserInfoDto() {
        return userInfoDto;
    }
}

