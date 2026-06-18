package com.ssafy.mvc.controller;

import com.ssafy.mvc.dao.PaymentDao;
import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    // 요금제 정의: planName → { amount, tickets }
    private static final Map<String, int[]> PLANS = Map.of(
        "10회권",  new int[]{5_000,  10},
        "20회권",  new int[]{10_000, 21},
        "30회권",  new int[]{15_000, 32},
        "50회권",  new int[]{25_000, 55}
    );

    private final PaymentDao paymentDao;
    private final UserDao userDao;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public PaymentController(PaymentDao paymentDao, UserDao userDao) {
        this.paymentDao = paymentDao;
        this.userDao = userDao;
    }

    // ① 결제 준비: 프론트에서 결제창 열기 전 주문 ID 예약
    @PostMapping("/ready")
    public ResponseEntity<?> ready(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        String planName = (String) body.get("planName");
        int[] plan = PLANS.get(planName);
        if (plan == null)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 요금제입니다."));

        String orderId = "order-" + userDetails.getUserDto().getUserId() + "-" + System.currentTimeMillis();

        PaymentDto dto = new PaymentDto();
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setOrderId(orderId);
        dto.setPlanName(planName);
        dto.setAmount(plan[0]);
        dto.setTickets(plan[1]);
        paymentDao.insert(dto);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "orderId", orderId,
            "amount",  plan[0],
            "tickets", plan[1]
        ));
    }

    // ② 결제 검증: 토스에서 승인 후 프론트가 paymentKey·orderId·amount 전달
    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        String paymentKey = (String) body.get("paymentKey");
        String orderId    = (String) body.get("orderId");
        int    amount     = (Integer) body.get("amount");

        // 1. orderId가 DB에 존재하는지 + 금액 일치 여부는 Toss 응답으로 확인
        if (!paymentDao.existsByOrderId(orderId))
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 주문입니다."));

        // 2. 토스페이먼츠 결제 승인 API 호출
        try {
            String auth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
            String requestBody = String.format(
                "{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%d}",
                paymentKey, orderId, amount
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                paymentDao.updateStatus(orderId, paymentKey, "FAILED", null);
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "결제 승인에 실패했습니다."));
            }

            // 3. 승인 성공 → DB 업데이트 + 티켓 지급
            paymentDao.updateStatus(orderId, paymentKey, "DONE", LocalDateTime.now());

            // payments 테이블에서 티켓 수 조회 후 지급
            List<PaymentDto> history = paymentDao.selectByUserId(userDetails.getUserDto().getUserId());
            int tickets = history.stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .mapToInt(PaymentDto::getTickets)
                .findFirst()
                .orElse(0);

            userDao.addTickets(userDetails.getUserDto().getUserId(), tickets);
            int newCount = userDao.getTicketCount(userDetails.getUserDto().getUserId());

            return ResponseEntity.ok(Map.of(
                "success",     true,
                "message",     "결제가 완료됐습니다.",
                "tickets",     tickets,
                "ticketCount", newCount
            ));

        } catch (Exception e) {
            paymentDao.updateStatus(orderId, paymentKey, "FAILED", null);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "결제 처리 중 오류가 발생했습니다."));
        }
    }

    // ③ 결제 내역 조회
    @GetMapping("/history")
    public ResponseEntity<?> history(@AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        List<PaymentDto> list = paymentDao.selectByUserId(userDetails.getUserDto().getUserId());
        return ResponseEntity.ok(Map.of("success", true, "data", list));
    }
}
