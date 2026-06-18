package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.UserPortfolioDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPortfolioDao {

    void insertPortfolio(UserPortfolioDto dto);

    UserPortfolioDto selectById(Long portfolioId);

    List<UserPortfolioDto> selectByUserId(Long userId);

    void deleteByIdAndUserId(@Param("portfolioId") Long portfolioId, @Param("userId") Long userId);
}
