package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.PaymentDto;

@Mapper
public interface PaymentDao {
    void insert(PaymentDto dto);
    void updateStatus(@Param("orderId") String orderId,
                      @Param("paymentKey") String paymentKey,
                      @Param("status") String status,
                      @Param("approvedAt") java.time.LocalDateTime approvedAt);
    boolean existsByOrderId(@Param("orderId") String orderId);
    List<PaymentDto> selectByUserId(@Param("userId") Long userId);
}
