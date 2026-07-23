package com.restaurant.crm.modules.erp.service;

import com.restaurant.crm.modules.erp.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.entity.OrderItem;
import com.restaurant.crm.modules.erp.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.mapper.KitchenOrderItemMapper;
import com.restaurant.crm.modules.erp.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.service.impl.KitchenDisplayServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenDisplayServiceImplTest {

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    KitchenOrderItemMapper kitchenOrderItemMapper;

    @InjectMocks
    KitchenDisplayServiceImpl service;

    @Test
    void queriesOnlyPendingAndInProgressForTheGivenBranch() {
        String branchId = "branch-1";
        when(orderItemRepository.findKitchenQueue(eq(branchId), any())).thenReturn(List.of());
        when(kitchenOrderItemMapper.toResponseList(any())).thenReturn(List.of());

        service.getKitchenQueue(branchId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<OrderItemStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(orderItemRepository).findKitchenQueue(eq(branchId), statuses.capture());

        assertThat(statuses.getValue())
                .containsExactlyInAnyOrder(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS);
    }

    @Test
    void returnsTheMapperOutput() {
        OrderItem item = new OrderItem();
        KitchenOrderItemResponse mapped = KitchenOrderItemResponse.builder()
                .orderItemId("oi-1")
                .status(OrderItemStatus.PENDING)
                .build();

        when(orderItemRepository.findKitchenQueue(any(), any())).thenReturn(List.of(item));
        when(kitchenOrderItemMapper.toResponseList(List.of(item))).thenReturn(List.of(mapped));

        List<KitchenOrderItemResponse> result = service.getKitchenQueue("branch-1");

        assertThat(result).containsExactly(mapped);
    }
}
