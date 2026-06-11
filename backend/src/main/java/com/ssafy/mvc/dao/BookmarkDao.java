package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookmarkDao {

    List<Long> selectScheduleIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserAndSchedule(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);

    void insert(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);

    void delete(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);
}
