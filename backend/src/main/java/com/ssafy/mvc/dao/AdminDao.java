package com.ssafy.mvc.dao;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.mvc.dto.AdminDto;

@Mapper
public interface AdminDao {

    AdminDto selectByEmail(String adminEmail);
}
