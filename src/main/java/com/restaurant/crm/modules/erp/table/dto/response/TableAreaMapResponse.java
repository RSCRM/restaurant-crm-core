package com.restaurant.crm.modules.erp.table.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableAreaMapResponse {
    String id;
    String areaName;
    String description;
    Integer displayOrder;
    List<TableStatusResponse> tables;
}

