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
    private final EmailService emailService;

    public UserServiceImpl(UserDao userDao, PasswordEncoder encoder, EmailService emailService) {
        this.userDao = userDao;
        this.encoder = encoder;
        this.emailService = emailService;
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
        if (!emailService.isVerified(user.getUserEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }
        user.setUserPwd(encoder.encode(user.getUserPwd()));
        user.setEmailVerified(1);
        userDao.insertUser(user);
        emailService.clearCode(user.getUserEmail());
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

    @Override
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    public void updateUser(UserDto user) {
        if (user.getUserPwd() != null && !user.getUserPwd().isBlank()) {
            user.setUserPwd(encoder.encode(user.getUserPwd()));
        } else {
            user.setUserPwd(null);
        }
        userDao.updateUser(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userDao.deleteUser(userId);
    }

    // 회원정보 수정 전 본인 확인용 비밀번호 검증
    // 로그인 응답에서 세션 객체의 userPwd를 null로 지워버리므로, 세션 값 대신 DB에서 새로 조회해 비교한다
    @Override
    public boolean verifyPassword(UserDto user, String rawPassword) {
        if (rawPassword == null) return false;
        UserDto fresh = userDao.selectUserByEmail(user.getUserEmail());
        return fresh != null && encoder.matches(rawPassword, fresh.getUserPwd());
    }
}
