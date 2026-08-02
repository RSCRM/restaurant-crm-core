package com.restaurant.crm.modules.erp.table.dto.request;

import com.restaurant.crm.modules.erp.table.constants.TableSessionConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableSessionCreationRequest {

    @NotBlank
    String tableId;

    @NotBlank
    @Size(max = TableSessionConstants.MAX_GUEST_NAME_LENGTH)
    String guestName;

    @Pattern(regexp = "^\\d{9,15}$")
    String guestPhone;

    @NotNull
    @Min(1)
    Integer partySize;

    @Size(max = TableSessionConstants.MAX_NOTE_LENGTH)
    String note;
}
