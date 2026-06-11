package com.ssafy.mvc.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userDao.selectAllUsers();
    }

    @Override
    public void signup(UserDto user) {
        if (userDao.existsByEmail(user.getUserEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        user.setUserPwd(encoder.encode(user.getUserPwd()));
        userDao.insertUser(user);
    }

    @Override
    public UserDto userLogin(String email, String password) {
        UserDto user = userDao.selectUserByEmail(email);
        if (user == null) {
            return null;
        }
        if (!encoder.matches(password, user.getUserPwd())) {
            return null;
        }
        user.setUserPwd(null);
        return user;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserDto user = userDao.selectUserByEmail(email);
        if (user != null) user.setUserPwd(null);
        return user;
    }
}
