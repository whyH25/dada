package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.UserDto;

@Mapper
public interface UserDao {

    List<UserDto> selectAllUsers();

    UserDto selectUserByEmail(@Param("userEmail") String email);

    boolean existsByEmail(@Param("userEmail") String email);

    void insertUser(UserDto user);
}
