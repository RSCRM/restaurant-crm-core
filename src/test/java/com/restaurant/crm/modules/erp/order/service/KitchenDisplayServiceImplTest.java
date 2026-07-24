package com.restaurant.crm.modules.erp.order.service;

import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.service.impl.KitchenDisplayServiceImpl;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KitchenDisplayServiceImplTest {

    private static final String BRANCH_ID = "branch-1";

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    ComboRepository comboRepository;

    @Mock
    RestaurantTableRepository restaurantTableRepository;

    @InjectMocks
    KitchenDisplayServiceImpl service;

    MockedStatic<AuthUtils> authUtils;

    @BeforeEach
    void stubCurrentBranch() {
        authUtils = mockStatic(AuthUtils.class);
        authUtils.when(AuthUtils::getBranchId).thenReturn(BRANCH_ID);
    }

    @AfterEach
    void releaseStaticMock() {
        authUtils.close();
    }

    @Test
    void queriesOnlyPendingAndInProgressForTheCallersBranch() {
        when(orderItemRepository.findKitchenQueue(any(), any())).thenReturn(List.of());

        service.getKitchenQueue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<OrderItemStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(orderItemRepository).findKitchenQueue(eq(BRANCH_ID), statuses.capture());

        assertThat(statuses.getValue())
                .containsExactlyInAnyOrder(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS);
    }

    @Test
    void returnsEmptyWithoutNameLookupsWhenQueueIsEmpty() {
        when(orderItemRepository.findKitchenQueue(any(), any())).thenReturn(List.of());

        assertThat(service.getKitchenQueue()).isEmpty();

        verifyNoInteractions(productRepository, comboRepository, restaurantTableRepository);
    }

    @Test
    void mapsProductNameAndTableNumberAndPreservesRepositoryOrder() {
        OrderItem older = orderItem("oi-1", "prod-1", null, "table-1", 2, OrderItemStatus.PENDING, true);
        OrderItem newer = orderItem("oi-2", "prod-1", null, "table-1", 1, OrderItemStatus.IN_PROGRESS, false);
        when(orderItemRepository.findKitchenQueue(any(), any())).thenReturn(List.of(older, newer));
        when(productRepository.findAllById(any())).thenReturn(List.of(product("prod-1", "Phở bò")));
        when(restaurantTableRepository.findAllById(any())).thenReturn(List.of(table("table-1", "A5")));

        List<KitchenOrderItemResponse> result = service.getKitchenQueue();

        // The repository applies the FIFO ordering; the service must not reshuffle it.
        assertThat(result).extracting(KitchenOrderItemResponse::getOrderItemId)
                .containsExactly("oi-1", "oi-2");

        KitchenOrderItemResponse first = result.getFirst();
        assertThat(first.getItemName()).isEqualTo("Phở bò");
        assertThat(first.getTableNumber()).isEqualTo("A5");
        assertThat(first.getQuantity()).isEqualTo(2);
        assertThat(first.getStatus()).isEqualTo(OrderItemStatus.PENDING);
        assertThat(first.isPriorityFlag()).isTrue();
        assertThat(first.getCreatedAt()).isNotNull();
    }

    @Test
    void usesComboNameWhenTheItemIsACombo() {
        OrderItem item = orderItem("oi-3", null, "combo-1", "table-2", 1, OrderItemStatus.PENDING, false);
        when(orderItemRepository.findKitchenQueue(any(), any())).thenReturn(List.of(item));
        when(comboRepository.findAllById(any())).thenReturn(List.of(combo("combo-1", "Combo gia đình")));
        when(restaurantTableRepository.findAllById(any())).thenReturn(List.of(table("table-2", "B1")));

        List<KitchenOrderItemResponse> result = service.getKitchenQueue();

        assertThat(result).singleElement()
                .satisfies(response -> {
                    assertThat(response.getItemName()).isEqualTo("Combo gia đình");
                    assertThat(response.getTableNumber()).isEqualTo("B1");
                });
        verifyNoInteractions(productRepository);
    }

    private OrderItem orderItem(
            String id,
            String productId,
            String comboId,
            String tableId,
            int quantity,
            OrderItemStatus status,
            boolean priorityFlag
    ) {
        return OrderItem.builder()
                .id(id)
                .createdAt(Instant.now())
                .order(Order.builder().branchId(BRANCH_ID).tableId(tableId).build())
                .productId(productId)
                .comboId(comboId)
                .quantity(quantity)
                .status(status)
                .priorityFlag(priorityFlag)
                .build();
    }

    private Product product(String id, String name) {
        return Product.builder().id(id).productName(name).build();
    }

    private Combo combo(String id, String name) {
        return Combo.builder().id(id).comboName(name).build();
    }

    private RestaurantTable table(String id, String number) {
        return RestaurantTable.builder().id(id).tableNumber(number).build();
    }
}
