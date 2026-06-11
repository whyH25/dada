package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.JobScheduleDto;

@Mapper
public interface JobScheduleDao {

    List<JobScheduleDto> selectAll();

    JobScheduleDto selectById(@Param("scheduleId") Long scheduleId);

    void insert(JobScheduleDto schedule);

    void update(JobScheduleDto schedule);

    void delete(@Param("scheduleId") Long scheduleId);
}
