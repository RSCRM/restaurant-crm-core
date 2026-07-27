package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartAddItemRequest;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartUpdateItemRequest;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartSubmitResponse;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.model.GroupCartItem;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.model.QrSessionMember;
import com.restaurant.crm.modules.erp.order.repository.GroupCartRedisRepository;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupCartServiceImplTest {

    private static final String SESSION = "session-1";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String DEVICE = "device-1";

    @Mock GroupCartRedisRepository groupCartRedisRepository;
    @Mock QrSessionRedisRepository qrSessionRedisRepository;
    @Mock ProductRepository productRepository;
    @Mock ComboRepository comboRepository;
    @Mock ModifierOptionRepository modifierOptionRepository;
    @Mock GroupCartSseService groupCartSseService;
    @Mock OrderService orderService;

    private GroupCartServiceImpl service() {
        return new GroupCartServiceImpl(groupCartRedisRepository, qrSessionRedisRepository,
                productRepository, comboRepository, modifierOptionRepository, groupCartSseService, orderService);
    }

    @Test
    void addItemStoresLineAndBroadcasts() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "AVAILABLE", "10.00")));
            when(groupCartRedisRepository.getItems(SESSION))
                    .thenReturn(List.of(cartItem("ci1", "p1", 2, List.of())));

            GroupCartResponse response = service().addItem(addProduct("p1", 2));

            verify(groupCartRedisRepository).saveItem(eq(SESSION), any(GroupCartItem.class), anyLong());
            verify(groupCartSseService).broadcast(eq(SESSION), eq("CART_UPDATED"), any());
            assertEquals(1, response.getItemCount());
            assertTrue(response.isSubmitAllowed());
        }
    }

    @Test
    void addItemRejectsProductFromAnotherBranch() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> service().addItem(addProduct("p1", 1)));
            assertEquals(ErrorCode.CART_MENU_ITEM_NOT_IN_BRANCH, exception.getErrorCode());
            verify(groupCartRedisRepository, never()).saveItem(any(), any(), anyLong());
        }
    }

    @Test
    void addItemRejectsUnavailableProduct() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "OUT_OF_STOCK", "10.00")));

            AppException exception = assertThrows(AppException.class, () -> service().addItem(addProduct("p1", 1)));
            assertEquals(ErrorCode.CART_ITEM_UNAVAILABLE, exception.getErrorCode());
        }
    }

    @Test
    void addItemRejectsModifierFromAnotherBranch() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "AVAILABLE", "10.00")));
            when(modifierOptionRepository.findByIdAndModifierGroupBranchId("m1", BRANCH))
                    .thenReturn(Optional.empty());
            GroupCartAddItemRequest request = addProduct("p1", 1);
            request.setModifierOptionIds(List.of("m1"));

            AppException exception = assertThrows(AppException.class, () -> service().addItem(request));
            assertEquals(ErrorCode.CART_MODIFIER_INVALID, exception.getErrorCode());
        }
    }

    @Test
    void addingSameProductTwiceCreatesTwoLines() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "AVAILABLE", "10.00")));
            when(groupCartRedisRepository.getItems(SESSION)).thenReturn(List.of());

            GroupCartServiceImpl service = service();
            service.addItem(addProduct("p1", 1));
            service.addItem(addProduct("p1", 1));

            ArgumentCaptor<GroupCartItem> captor = ArgumentCaptor.forClass(GroupCartItem.class);
            verify(groupCartRedisRepository, times(2)).saveItem(eq(SESSION), captor.capture(), anyLong());
            assertEquals(2, captor.getAllValues().stream().map(GroupCartItem::cartItemId).distinct().count());
        }
    }

    @Test
    void getCartComputesSubtotalIncludingModifiers() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "AVAILABLE", "10.00")));
            when(modifierOptionRepository.findByIdAndModifierGroupBranchId("m1", BRANCH))
                    .thenReturn(Optional.of(modifier("m1", "1.50")));
            when(groupCartRedisRepository.getItems(SESSION))
                    .thenReturn(List.of(cartItem("ci1", "p1", 2, List.of("m1"))));

            GroupCartResponse response = service().getCart();

            // 10.00 * 2 + 1.50 = 21.50
            assertEquals(new BigDecimal("21.50"), response.getSubtotal());
            assertEquals(new BigDecimal("21.50"), response.getItems().get(0).getLineTotal());
            assertEquals(new BigDecimal("10.00"), response.getItems().get(0).getUnitPrice());
        }
    }

    @Test
    void getCartRejectsWhenDeviceIsNotAMember() {
        try (MockedStatic<AuthUtils> auth = context()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(QrSessionStatus.OPEN)));
            when(qrSessionRedisRepository.getMember(SESSION, DEVICE)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> service().getCart());
            assertEquals(ErrorCode.CART_MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void operationsRejectMissingSession() {
        try (MockedStatic<AuthUtils> auth = context()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> service().getCart());
            assertEquals(ErrorCode.TQR_SESSION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void operationsRejectLockedForPaymentSession() {
        try (MockedStatic<AuthUtils> auth = context()) {
            when(qrSessionRedisRepository.findSession(SESSION))
                    .thenReturn(Optional.of(session(QrSessionStatus.LOCKED_FOR_PAYMENT)));

            AppException exception = assertThrows(AppException.class, () -> service().getCart());
            assertEquals(ErrorCode.TQR_SESSION_LOCKED_FOR_PAYMENT, exception.getErrorCode());
        }
    }

    @Test
    void updateItemRejectsWhenLockedByAnotherDevice() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.getItem(SESSION, "ci1"))
                    .thenReturn(Optional.of(cartItem("ci1", "p1", 1, List.of())));
            when(groupCartRedisRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.of("device-2"));

            GroupCartUpdateItemRequest request = GroupCartUpdateItemRequest.builder().quantity(3).build();
            AppException exception = assertThrows(AppException.class, () -> service().updateItem("ci1", request));
            assertEquals(ErrorCode.CART_ITEM_LOCKED, exception.getErrorCode());
            verify(groupCartRedisRepository, never()).saveItem(any(), any(), anyLong());
        }
    }

    @Test
    void updateItemRejectsMissingItem() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.getItem(SESSION, "ci9")).thenReturn(Optional.empty());

            GroupCartUpdateItemRequest request = GroupCartUpdateItemRequest.builder().quantity(3).build();
            AppException exception = assertThrows(AppException.class, () -> service().updateItem("ci9", request));
            assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void deleteItemRemovesAndReleasesLock() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.getItem(SESSION, "ci1"))
                    .thenReturn(Optional.of(cartItem("ci1", "p1", 1, List.of())));
            when(groupCartRedisRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.empty());
            when(groupCartRedisRepository.tryLockItem(eq(SESSION), eq("ci1"), eq(DEVICE), anyLong())).thenReturn(true);
            when(groupCartRedisRepository.getItems(SESSION)).thenReturn(List.of());

            service().deleteItem("ci1");

            verify(groupCartRedisRepository).removeItem(SESSION, "ci1");
            verify(groupCartRedisRepository).releaseLock(SESSION, "ci1");
        }
    }

    // ==== submit (M6) ====

    @Test
    void submitByOwnerCreatesOrderBindsAndClearsCart() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(true);
            when(groupCartRedisRepository.getItems(SESSION))
                    .thenReturn(List.of(cartItem("ci1", "p1", 2, List.of())));
            when(groupCartRedisRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.empty());
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "AVAILABLE", "10.00")));
            when(orderService.create(any(CreateOrderRequestDto.class)))
                    .thenReturn(CreateOrderResponse.builder().orderId("order-1").build());

            GroupCartSubmitResponse response = service().submit();

            assertEquals("order-1", response.getOrderId());
            ArgumentCaptor<CreateOrderRequestDto> captor = ArgumentCaptor.forClass(CreateOrderRequestDto.class);
            verify(orderService, times(1)).create(captor.capture());
            assertEquals(OrderType.DINE_IN, captor.getValue().getOrderType());
            assertEquals("0900000000", captor.getValue().getCustomerPhone());
            verify(qrSessionRedisRepository).bindOrder(eq(SESSION), eq("order-1"), anyLong());
            verify(groupCartRedisRepository).clearCart(SESSION);
            verify(groupCartRedisRepository).releaseSubmitGuard(SESSION);
        }
    }

    @Test
    void submitByMemberRejectedWithoutCreatingOrder() {
        try (MockedStatic<AuthUtils> auth = context()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(QrSessionStatus.OPEN)));
            when(qrSessionRedisRepository.getMember(SESSION, DEVICE))
                    .thenReturn(Optional.of(member(DEVICE, SessionMemberRole.MEMBER)));
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(true);

            AppException exception = assertThrows(AppException.class, () -> service().submit());
            assertEquals(ErrorCode.TQR_NOT_SESSION_OWNER, exception.getErrorCode());
            verify(orderService, never()).create(any());
            verify(groupCartRedisRepository).releaseSubmitGuard(SESSION);
        }
    }

    @Test
    void submitRejectsWhenAlreadyInProgress() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(false);

            AppException exception = assertThrows(AppException.class, () -> service().submit());
            assertEquals(ErrorCode.CART_SUBMIT_IN_PROGRESS, exception.getErrorCode());
            verify(orderService, never()).create(any());
            verify(groupCartRedisRepository, never()).releaseSubmitGuard(SESSION);
        }
    }

    @Test
    void submitRejectsEmptyCart() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(true);
            when(groupCartRedisRepository.getItems(SESSION)).thenReturn(List.of());

            AppException exception = assertThrows(AppException.class, () -> service().submit());
            assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
            verify(orderService, never()).create(any());
            verify(groupCartRedisRepository).releaseSubmitGuard(SESSION);
        }
    }

    @Test
    void submitRejectsWhenAnItemLockedByAnotherDevice() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(true);
            when(groupCartRedisRepository.getItems(SESSION))
                    .thenReturn(List.of(cartItem("ci1", "p1", 1, List.of())));
            when(groupCartRedisRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.of("device-2"));

            AppException exception = assertThrows(AppException.class, () -> service().submit());
            assertEquals(ErrorCode.CART_ITEM_LOCKED, exception.getErrorCode());
            verify(orderService, never()).create(any());
        }
    }

    @Test
    void submitRemovesUnavailableItemAndFails() {
        try (MockedStatic<AuthUtils> auth = context()) {
            openSessionWithOwner();
            when(groupCartRedisRepository.tryAcquireSubmitGuard(eq(SESSION), eq(DEVICE), anyLong())).thenReturn(true);
            when(groupCartRedisRepository.getItems(SESSION))
                    .thenReturn(List.of(cartItem("ci1", "p1", 1, List.of())));
            when(groupCartRedisRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.empty());
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "OUT_OF_STOCK", "10.00")));

            AppException exception = assertThrows(AppException.class, () -> service().submit());
            assertEquals(ErrorCode.CART_ITEM_UNAVAILABLE, exception.getErrorCode());
            verify(groupCartRedisRepository).removeItem(SESSION, "ci1");
            verify(orderService, never()).create(any());
            verify(groupCartRedisRepository).releaseSubmitGuard(SESSION);
        }
    }

    // ==== fixtures ====

    private MockedStatic<AuthUtils> context() {
        MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
        auth.when(AuthUtils::getDeviceId).thenReturn(DEVICE);
        return auth;
    }

    private void openSessionWithOwner() {
        when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(QrSessionStatus.OPEN)));
        when(qrSessionRedisRepository.getMember(SESSION, DEVICE))
                .thenReturn(Optional.of(member(DEVICE, SessionMemberRole.OWNER)));
    }

    private QrSessionData session(QrSessionStatus status) {
        return new QrSessionData(SESSION, "org-1", BRANCH, TABLE, DEVICE, null, "0900000000", null, status, Instant.now());
    }

    private QrSessionMember member(String deviceId, SessionMemberRole role) {
        return new QrSessionMember(deviceId, role, null, null, Instant.now(), Instant.now());
    }

    private Product product(String id, String status, String price) {
        return Product.builder().id(id).branchId(BRANCH).categoryId("c1").productName("Product " + id)
                .price(new BigDecimal(price)).status(status).requiresPreparation(true).build();
    }

    private ModifierOption modifier(String id, String price) {
        return ModifierOption.builder().id(id).optionName("Opt " + id)
                .additionalPrice(new BigDecimal(price)).status("AVAILABLE").build();
    }

    private GroupCartItem cartItem(String cartItemId, String productId, int quantity, List<String> modifierIds) {
        return new GroupCartItem(cartItemId, productId, null, quantity, "note", modifierIds, DEVICE, Instant.now());
    }

    private GroupCartAddItemRequest addProduct(String productId, int quantity) {
        return GroupCartAddItemRequest.builder().productId(productId).quantity(quantity).build();
    }
}
