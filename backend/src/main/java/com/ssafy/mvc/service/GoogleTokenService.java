package com.ssafy.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

// 회원탈퇴 시 구글 소셜 로그인으로 들어온 세션이면 구글 쪽 토큰도 함께 해지
// (이메일/비번 로그인 세션은 해지할 구글 토큰이 없으므로 조용히 무시)
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestClient restClient = RestClient.create();

    public void revokeIfLinked(String principalName) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", principalName);
        if (client == null) return;

        String token = client.getAccessToken().getTokenValue();
        try {
            restClient.post()
                    .uri("https://oauth2.googleapis.com/revoke?token={token}", token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("구글 토큰 해지 실패: {}", e.getMessage());
        }
    }
}
