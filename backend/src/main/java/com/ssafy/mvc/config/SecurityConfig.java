package com.ssafy.mvc.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import com.ssafy.mvc.service.CustomUserDetailsService;
import com.ssafy.mvc.service.CustomUserOidcService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.service.AdminUserDetailsService;
import com.ssafy.mvc.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 소셜 로그인 완료(성공/실패) 후 브라우저를 돌려보낼 프론트엔드 주소
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            CustomUserOidcService customUserOidcService,
            OAuth2AuthorizedClientService authorizedClientService,
            ClientRegistrationRepository clientRegistrationRepository,
            UserService userService) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .securityContext(ctx -> ctx.requireExplicitSave(false))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/signup",
                    "/api/users/find-password",
                    "/api/email/send-code",
                    "/api/email/verify-code",
                    "/api/admin/login",
                    "/api/admin/logout",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/job-schedules").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stories", "/api/stories/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices", "/api/notices/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/*", "/api/posts/*/comments").permitAll()

                // 결제 API 전체 인증 필요
                .requestMatchers("/api/payments/**").authenticated()

                // /api/admin/login, /api/admin/logout 을 먼저 허용했으므로
                // 나머지 /api/admin/** 은 ADMIN 권한 필요
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                // access_type=offline을 안 붙이면 구글이 최초 동의 때조차 refresh token을 안 내려줌
                // (회원탈퇴 시 토큰 해지에 필요해서 명시적으로 요청)
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository)))
                // 카카오(일반 OAuth2)는 이메일 스코프 제한으로 보류 중. 재추가 시 여기에
                // .userService(customUserOAuth2Service) 한 줄만 다시 연결하면 됨
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customUserOidcService) // 구글(OIDC)
                )
                .successHandler((request, response, authentication) -> {
                    if (authentication.getPrincipal() instanceof CustomUserDetailsDto principal
                            && authentication instanceof OAuth2AuthenticationToken oauthToken) {
                        // 구글은 최초 동의 시점에만 refresh token을 내려주므로, 받았을 때만 회원탈퇴용으로 저장
                        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                                oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
                        if (client != null && client.getRefreshToken() != null) {
                            userService.saveGoogleRefreshToken(
                                    principal.getUserDto().getUserId(),
                                    client.getRefreshToken().getTokenValue());
                        }
                    }

                    String redirectUrl = frontendUrl + "/#/";
                    if (authentication.getPrincipal() instanceof CustomUserDetailsDto principal
                            && principal.getUserDto().isNewUser()) {
                        redirectUrl = frontendUrl + "/#/?newSocialUser=1";
                    }
                    response.sendRedirect(redirectUrl);
                })
                .failureHandler((request, response, exception) -> {
                    log.warn("소셜 로그인 실패: {}", exception.getMessage());
                    // 프론트가 hash 라우터(createWebHashHistory)라서 쿼리스트링은 #/ 뒤에 와야 인식됨
                    response.sendRedirect(frontendUrl + "/#/?socialLoginError=1");
                })
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
                })
            );

        return http.build();
    }

    // 구글 인증 요청에 access_type=offline을 추가해 refresh token을 받기 위한 리졸버
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("access_type", "offline")));
        return resolver;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://54.116.159.140",
            "https://dadajob.kro.kr",
            "https://www.dadajob.kro.kr"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 일반 사용자 인증 매니저 (users 테이블) */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** 관리자 전용 인증 매니저 (admins 테이블만 조회) */
    @Bean
    @Qualifier("adminAuthManager")
    public AuthenticationManager adminAuthenticationManager(
            AdminUserDetailsService adminUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
