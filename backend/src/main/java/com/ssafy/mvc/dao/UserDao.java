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

    void updateUser(UserDto user);

    void deleteUser(@Param("userId") Long userId);

    void addTickets(@Param("userId") Long userId, @Param("tickets") int tickets);

    void useTicket(@Param("userId") Long userId);

    int getTicketCount(@Param("userId") Long userId);
}
