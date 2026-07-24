package com.restaurant.crm.modules.erp.attendance.mapper;

import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "employeeId", source = "shiftAssignment.employee.id")
    @Mapping(target = "branchId", source = "shiftAssignment.branch.id")
    @Mapping(target = "shiftAssignmentId", source = "shiftAssignment.id")
    @Mapping(target = "workDate", source = "shiftAssignment.workDate")
    @Mapping(target = "scheduledStart", source = "shiftAssignment.startAt")
    @Mapping(target = "scheduledEnd", source = "shiftAssignment.endAt")
    AttendanceResponse toResponse(Attendance attendance);
}
