package com.restaurant.crm.modules.erp.table.service.interfaces;

import com.restaurant.crm.modules.erp.table.dto.request.TableSessionCreationRequest;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionTransferRequest;
import com.restaurant.crm.modules.erp.table.dto.response.TableSessionResponse;

public interface TableSessionService {
    TableSessionResponse create(TableSessionCreationRequest request);

    TableSessionResponse transfer(String sessionId, TableSessionTransferRequest request);

    TableSessionResponse close(String sessionId);
}
