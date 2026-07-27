package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
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
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Acceptance: the OWNER double-submitting (or a retry) must create exactly one order; the other
 * concurrent attempts get {@code CART_1005}. The submit guard SETNX is modelled by an atomic
 * {@link ConcurrentHashMap#putIfAbsent} that stays held for the whole window.
 */
class GroupCartSubmitConcurrencyTest {

    private static final String SESSION = "session-1";
    private static final String BRANCH = "branch-1";
    private static final String OWNER_DEVICE = "owner-device";
    private static final int THREADS = 10;

    @Test
    void concurrentOwnerSubmitsCreateExactlyOneOrder() throws InterruptedException {
        GroupCartRedisRepository cartRepository = mock(GroupCartRedisRepository.class);
        QrSessionRedisRepository sessionRepository = mock(QrSessionRedisRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        OrderService orderService = mock(OrderService.class);
        GroupCartServiceImpl service = new GroupCartServiceImpl(
                cartRepository, sessionRepository, productRepository, mock(ComboRepository.class),
                mock(ModifierOptionRepository.class), mock(GroupCartSseService.class), orderService);

        QrSessionData session = new QrSessionData(SESSION, "org-1", BRANCH, "table-1", OWNER_DEVICE,
                null, "0900000000", null, QrSessionStatus.OPEN, Instant.now());
        when(sessionRepository.findSession(SESSION)).thenReturn(Optional.of(session));
        when(sessionRepository.getMember(SESSION, OWNER_DEVICE)).thenReturn(Optional.of(
                new QrSessionMember(OWNER_DEVICE, SessionMemberRole.OWNER, null, null, Instant.now(), Instant.now())));
        when(cartRepository.getItems(SESSION)).thenReturn(List.of(
                new GroupCartItem("ci1", "p1", null, 1, null, List.of(), OWNER_DEVICE, Instant.now())));
        when(cartRepository.getLockOwner(SESSION, "ci1")).thenReturn(Optional.empty());
        when(productRepository.findByIdAndBranchId("p1", BRANCH)).thenReturn(Optional.of(Product.builder()
                .id("p1").branchId(BRANCH).categoryId("c").productName("P")
                .price(new BigDecimal("10.00")).status("AVAILABLE").requiresPreparation(true).build()));

        // Atomic submit-guard stand-in; stays held for the window (releaseSubmitGuard is a no-op mock).
        ConcurrentHashMap<String, String> guard = new ConcurrentHashMap<>();
        when(cartRepository.tryAcquireSubmitGuard(eq(SESSION), anyString(), anyLong()))
                .thenAnswer(invocation -> guard.putIfAbsent(SESSION, invocation.getArgument(1)) == null);

        AtomicInteger created = new AtomicInteger();
        when(orderService.create(any(CreateOrderRequestDto.class))).thenAnswer(invocation -> {
            created.incrementAndGet();
            return CreateOrderResponse.builder().orderId("order-1").build();
        });

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
                    auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
                    auth.when(AuthUtils::getDeviceId).thenReturn(OWNER_DEVICE);
                    startGate.await();
                    service.submit();
                } catch (AppException exception) {
                    if (exception.getErrorCode() == ErrorCode.CART_SUBMIT_IN_PROGRESS) {
                        rejected.incrementAndGet();
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown();
        done.await();
        pool.shutdownNow();

        assertEquals(1, created.get(), "exactly one order must be created");
        assertEquals(THREADS - 1, rejected.get(), "all other submits must get CART_1005");
    }
}
