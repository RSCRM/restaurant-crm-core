package com.restaurant.crm.modules.erp.schedule.mapper;

import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.entity.WorkSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.user.username")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    PersonalScheduleResponse toResponse(WorkSchedule workSchedule);

    List<PersonalScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);
}

