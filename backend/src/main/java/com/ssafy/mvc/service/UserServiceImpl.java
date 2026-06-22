package com.ssafy.mvc.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.mvc.dao.InterviewPromptDao;
import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dao.InterviewRoomPersonaDao;
import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dao.ReportDao;
import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dao.UserPortfolioDao;
import com.ssafy.mvc.dao.UserResumeDao;
import com.ssafy.mvc.dao.UserSocialDao;
import com.ssafy.mvc.dto.UserDto;
import com.ssafy.mvc.dto.UserPortfolioDto;
import com.ssafy.mvc.dto.UserResumeDto;
import com.ssafy.mvc.dto.UserSocialDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserSocialDao userSocialDao;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    // 회원탈퇴 시 연관 데이터 정리용
    private final InterviewRoomDao interviewRoomDao;
    private final InterviewScenarioDao interviewScenarioDao;
    private final InterviewRoomPersonaDao interviewRoomPersonaDao;
    private final InterviewPromptDao interviewPromptDao;
    private final ReportDao reportDao;
    private final UserResumeDao userResumeDao;
    private final UserPortfolioDao userPortfolioDao;
    private final GcsStorageService gcsStorageService;

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

    // 회원탈퇴: user_resume/user_portfolio/users_social은 실제 삭제(GCS 원본 포함),
    // 면접 관련 7개 테이블(interview_room과 그 자식들)은 deleted_at만 채워 비활성화,
    // 마지막으로 users.user_status를 DELETED로 전환
    // (구글 쪽 토큰 해지는 세션 정보가 필요해 UserController에서 처리)
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        List<Long> roomIds = interviewRoomDao.selectRoomIdsByUserId(userId);
        if (!roomIds.isEmpty()) {
            interviewRoomDao.deactivateByUserId(userId);
            interviewScenarioDao.deactivateByRoomIds(roomIds);
            interviewRoomPersonaDao.deactivateByRoomIds(roomIds);
            interviewPromptDao.deactivateByRoomIds(roomIds);
            reportDao.deactivateReportsByRoomIds(roomIds);
            reportDao.deactivateReportApplicantsByRoomIds(roomIds);
            reportDao.deactivateReportQuestionsByRoomIds(roomIds);
        }

        for (UserResumeDto resume : userResumeDao.selectByUserId(userId)) {
            gcsStorageService.delete(resume.getFileUrl());
        }
        userResumeDao.deleteByUserId(userId);

        for (UserPortfolioDto portfolio : userPortfolioDao.selectByUserId(userId)) {
            gcsStorageService.delete(portfolio.getFileUrl());
        }
        userPortfolioDao.deleteByUserId(userId);

        userSocialDao.deleteByUserId(userId);

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

    // 임시 비밀번호용 글자 집합 (0/O, 1/l/I 등 헷갈리는 문자는 제외)
    private static final String TEMP_PWD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    // 아이디/비밀번호 찾기: 이메일로 가입 여부를 확인하고, 가입돼 있으면 임시 비밀번호를 발급해 DB에 반영 후 메일 발송
    @Override
    public void resetPasswordByEmail(String email) {
        UserDto user = userDao.selectUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("가입되지 않은 회원입니다.");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_PWD_CHARS.charAt(random.nextInt(TEMP_PWD_CHARS.length())));
        }
        String tempPassword = sb.toString();

        user.setUserPwd(encoder.encode(tempPassword));
        userDao.updateUser(user);

        emailService.sendTempPasswordMail(email, tempPassword);
    }

    // 소셜 로그인: 1) 이미 연동된 계정이면 그 계정 그대로 로그인
    //            2) 연동 기록은 없지만 이메일이 같은 기존(이메일/비번) 계정이 있으면 연동만 추가
    //            3) 둘 다 없으면 새 계정을 만들고 연동 등록
    // 이메일/리포트 알림 등 실제 메일 발송 기능들이 user_email에 의존하므로,
    // 소셜 제공자가 이메일을 내려주지 않으면 가입을 막는다 (대체 이메일을 만들지 않음)
    @Override
    public UserDto findOrCreateSocialUser(String provider, String providerAccountId,
                                           String email, String name, String profileImageUrl) {
        UserSocialDto social = userSocialDao.selectByProviderAndAccountId(provider, providerAccountId);
        if (social != null) {
            return userDao.selectUserById(social.getUserId());
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일 제공에 동의해야 가입할 수 있습니다.");
        }

        UserDto user = userDao.selectUserByEmail(email);
        if (user == null) {
            user = new UserDto();
            user.setUserEmail(email);
            user.setUserName(name);
            user.setUserProfileImg(profileImageUrl);
            user.setEmailVerified(1);
            userDao.insertUser(user);
        }

        UserSocialDto newSocial = new UserSocialDto();
        newSocial.setUserId(user.getUserId());
        newSocial.setProvider(provider);
        newSocial.setProviderAccountId(providerAccountId);
        userSocialDao.insertSocial(newSocial);

        return user;
    }
}
