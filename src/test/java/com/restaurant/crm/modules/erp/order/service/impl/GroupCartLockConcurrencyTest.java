package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * BR-CST-GRP-03 / acceptance: N devices locking the same cart line at the same instant must yield
 * exactly one winner; everyone else gets {@code CART_1003}. The atomic SETNX is modelled by a
 * {@link ConcurrentHashMap#putIfAbsent} stand-in (real atomicity, not a Mockito "always true").
 */
class GroupCartLockConcurrencyTest {

    private static final String SESSION = "session-1";
    private static final String BRANCH = "branch-1";
    private static final String CART_ITEM = "ci1";
    private static final int THREADS = 10;

    @Test
    void concurrentLocksOnSameItemElectExactlyOneHolder() throws InterruptedException {
        GroupCartRedisRepository cartRepository = mock(GroupCartRedisRepository.class);
        QrSessionRedisRepository sessionRepository = mock(QrSessionRedisRepository.class);
        GroupCartServiceImpl service = new GroupCartServiceImpl(
                cartRepository, sessionRepository, mock(ProductRepository.class), mock(ComboRepository.class),
                mock(ModifierOptionRepository.class), mock(GroupCartSseService.class), mock(OrderService.class));

        QrSessionData session = new QrSessionData(SESSION, "org-1", BRANCH, "table-1", "owner",
                null, "0900000000", null, QrSessionStatus.OPEN, Instant.now());
        when(sessionRepository.findSession(SESSION)).thenReturn(Optional.of(session));
        when(sessionRepository.getMember(eq(SESSION), anyString())).thenAnswer(invocation ->
                Optional.of(new QrSessionMember(invocation.getArgument(1), SessionMemberRole.MEMBER,
                        null, null, Instant.now(), Instant.now())));
        when(cartRepository.getItem(SESSION, CART_ITEM)).thenReturn(Optional.of(
                new GroupCartItem(CART_ITEM, "p1", null, 1, null, List.of(), "owner", Instant.now())));

        // Atomic SETNX stand-in.
        ConcurrentHashMap<String, String> locks = new ConcurrentHashMap<>();
        when(cartRepository.tryLockItem(eq(SESSION), eq(CART_ITEM), anyString(), anyLong()))
                .thenAnswer(invocation -> locks.putIfAbsent(CART_ITEM, invocation.getArgument(2)) == null);
        when(cartRepository.getLockOwner(SESSION, CART_ITEM))
                .thenAnswer(invocation -> Optional.ofNullable(locks.get(CART_ITEM)));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger acquired = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            String deviceId = "device-" + i;
            pool.submit(() -> {
                try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
                    auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
                    auth.when(AuthUtils::getDeviceId).thenReturn(deviceId);
                    startGate.await();
                    service.lockItem(CART_ITEM);
                    acquired.incrementAndGet();
                } catch (AppException exception) {
                    if (exception.getErrorCode() == ErrorCode.CART_ITEM_LOCKED) {
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

        assertEquals(1, acquired.get(), "exactly one device must hold the lock");
        assertEquals(THREADS - 1, rejected.get(), "all other devices must get CART_1003");
    }
}
