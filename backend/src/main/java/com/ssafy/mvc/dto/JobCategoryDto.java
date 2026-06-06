package com.ssafy.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCategoryDto {
    private int jobId;
    private String jobCode;
    private String jobName;
    private int sortOrder;
}
