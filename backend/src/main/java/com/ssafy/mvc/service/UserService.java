package com.ssafy.mvc.service;

import java.util.List;

import com.ssafy.mvc.dto.UserDto;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto userLogin(String email, String password);

    void signup(UserDto user);

    UserDto getUserByEmail(String email);
}