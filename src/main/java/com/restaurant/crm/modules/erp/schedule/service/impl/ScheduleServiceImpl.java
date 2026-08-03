package com.restaurant.crm.modules.erp.schedule.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.schedule.constants.WorkScheduleConstants;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleCreationRequest;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleUpdateRequest;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.dto.response.ScheduleEmployeeResponse;
import com.restaurant.crm.modules.erp.schedule.entity.WorkSchedule;
import com.restaurant.crm.modules.erp.schedule.mapper.WorkScheduleMapper;
import com.restaurant.crm.modules.erp.schedule.repository.WorkScheduleRepository;
import com.restaurant.crm.modules.erp.schedule.service.interfaces.ScheduleService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleServiceImpl implements ScheduleService {

    EmployeeRepository employeeRepository;
    WorkScheduleRepository workScheduleRepository;
    WorkScheduleMapper workScheduleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonalScheduleResponse> getPersonalSchedule(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        Employee employee = employeeRepository
                .findByIdAndUserId(AuthUtils.getEmployeeId(), AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }

        return workScheduleMapper.toResponseList(
                workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        employee.getId(),
                        from,
                        to
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalScheduleResponse> getStaffSchedule(String employeeId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        Employee employee = getManagedEmployee(employeeId);

        return workScheduleMapper.toResponseList(
                workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        employee.getId(),
                        from,
                        to
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalScheduleResponse> getManagedSchedules(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        List<WorkSchedule> schedules = AuthUtils.getDataScope() == OrgDataScope.ORGANIZATION
                ? workScheduleRepository.findByBranchOrganizationIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        AuthUtils.getOrganizationId(), from, to)
                : workScheduleRepository.findByBranchIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        AuthUtils.getBranchId(), from, to);
        String currentEmployeeId = AuthUtils.getEmployeeId();
        return workScheduleMapper.toResponseList(schedules.stream()
                .filter(schedule -> !Objects.equals(schedule.getEmployee().getId(), currentEmployeeId))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEmployeeResponse> getManagedEmployees() {
        List<Employee> employees = AuthUtils.getDataScope() == OrgDataScope.ORGANIZATION
                ? employeeRepository.findByBranch_Organization_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
                        AuthUtils.getOrganizationId(), EmployeeStatus.ACTIVE, "MANAGER")
                : employeeRepository.findByBranch_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
                        AuthUtils.getBranchId(), EmployeeStatus.ACTIVE, "MANAGER");
        return employees.stream()
                .map(employee -> ScheduleEmployeeResponse.builder()
                        .id(employee.getId())
                        .name(employee.getUser().getUsername())
                        .branchName(employee.getBranch().getBranchName())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public PersonalScheduleResponse createSchedule(ScheduleCreationRequest request) {
        Employee employee = getManagedEmployee(request.getEmployeeId());
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (workScheduleRepository.existsByEmployeeIdAndWorkDateAndStartTime(
                employee.getId(), request.getWorkDate(), request.getStartTime())) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        WorkSchedule schedule = WorkSchedule.builder()
                .employee(employee)
                .branch(employee.getBranch())
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .note(request.getNote())
                .build();
        return workScheduleMapper.toResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public PersonalScheduleResponse updateSchedule(String scheduleId, ScheduleUpdateRequest request) {
        WorkSchedule schedule = getManagedSchedule(scheduleId);
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (workScheduleRepository.existsByEmployeeIdAndWorkDateAndStartTimeAndIdNot(
                schedule.getEmployee().getId(),
                request.getWorkDate(),
                request.getStartTime(),
                scheduleId
        )) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        schedule.setWorkDate(request.getWorkDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setNote(request.getNote());
        return workScheduleMapper.toResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deleteSchedule(String scheduleId) {
        workScheduleRepository.delete(getManagedSchedule(scheduleId));
    }

    private Employee getManagedEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }
        if (!canManage(employee.getBranch())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return employee;
    }

    private WorkSchedule getManagedSchedule(String scheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!canManage(schedule.getBranch())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return schedule;
    }

    private boolean canManage(OrganizationBranch branch) {
        if (branch == null) {
            return false;
        }
        if (AuthUtils.getDataScope() == OrgDataScope.ORGANIZATION) {
            return branch.getOrganization() != null
                    && Objects.equals(branch.getOrganization().getId(), AuthUtils.getOrganizationId());
        }
        return Objects.equals(branch.getId(), AuthUtils.getBranchId());
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new AppException(ErrorCode.SCHEDULE_TIME_RANGE_INVALID);
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > WorkScheduleConstants.MAX_RANGE_DAYS) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_EXCEEDED);
        }
    }
}

