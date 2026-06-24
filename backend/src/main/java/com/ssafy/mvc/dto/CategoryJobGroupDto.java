package com.ssafy.mvc.dto;

import java.util.List;

public class CategoryJobGroupDto {
    private int groupId;
    private String groupCode;
    private String groupName;
    private int sortOrder;
    // 해당 대분류에 속한 직무 목록 (MyBatis collection 매핑)
    private List<JobCategoryDto> jobs;

    public CategoryJobGroupDto() {}

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<JobCategoryDto> getJobs() { return jobs; }
    public void setJobs(List<JobCategoryDto> jobs) { this.jobs = jobs; }
}
