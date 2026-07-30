package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.constants.GroupCartConstants;
import com.restaurant.crm.modules.erp.order.constants.QrSessionConstants;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartAddItemRequest;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartUpdateItemRequest;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartItemModifierResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartSubmitResponse;
import com.restaurant.crm.modules.erp.order.enums.GroupCartEventType;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.model.GroupCartItem;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.model.QrSessionMember;
import com.restaurant.crm.modules.erp.order.repository.GroupCartRedisRepository;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartService;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupCartServiceImpl implements GroupCartService {

    GroupCartRedisRepository groupCartRedisRepository;
    QrSessionRedisRepository qrSessionRedisRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    GroupCartSseService groupCartSseService;
    OrderService orderService;

    @Override
    public GroupCartResponse getCart() {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);
        touchTtls(session);
        return buildCartResponse(session, deviceId);
    }

    @Override
    public GroupCartResponse addItem(GroupCartAddItemRequest request) {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);

        String branchId = session.branchId();
        validateMenuItemAvailable(branchId, request.getProductId(), request.getComboId());
        validateModifiers(branchId, request.getModifierOptionIds());

        GroupCartItem item = new GroupCartItem(
                UUID.randomUUID().toString(),
                request.getProductId(),
                request.getComboId(),
                request.getQuantity(),
                request.getNote(),
                request.getModifierOptionIds() == null ? List.of() : request.getModifierOptionIds(),
                deviceId,
                Instant.now());
        groupCartRedisRepository.saveItem(session.sessionId(), item, QrSessionConstants.SESSION_TTL_SECONDS);
        touchTtls(session);

        GroupCartResponse response = buildCartResponse(session, deviceId);
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_UPDATED.name(), response);
        return response;
    }

    @Override
    public GroupCartResponse updateItem(String cartItemId, GroupCartUpdateItemRequest request) {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);

        GroupCartItem existing = groupCartRedisRepository.getItem(session.sessionId(), cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        boolean acquiredHere = acquireItemLockForOperation(session.sessionId(), cartItemId, deviceId);
        try {
            validateModifiers(session.branchId(), request.getModifierOptionIds());
            GroupCartItem updated = new GroupCartItem(
                    cartItemId,
                    existing.productId(),
                    existing.comboId(),
                    request.getQuantity(),
                    request.getNote(),
                    request.getModifierOptionIds() == null ? existing.modifierOptionIds() : request.getModifierOptionIds(),
                    existing.addedByDeviceId(),
                    existing.addedAt());
            groupCartRedisRepository.saveItem(session.sessionId(), updated, QrSessionConstants.SESSION_TTL_SECONDS);
        } finally {
            if (acquiredHere) {
                groupCartRedisRepository.releaseLock(session.sessionId(), cartItemId);
            }
        }

        touchTtls(session);
        GroupCartResponse response = buildCartResponse(session, deviceId);
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_UPDATED.name(), response);
        return response;
    }

    @Override
    public GroupCartResponse deleteItem(String cartItemId) {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);

        groupCartRedisRepository.getItem(session.sessionId(), cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        acquireItemLockForOperation(session.sessionId(), cartItemId, deviceId);
        groupCartRedisRepository.removeItem(session.sessionId(), cartItemId);
        groupCartRedisRepository.releaseLock(session.sessionId(), cartItemId);

        touchTtls(session);
        GroupCartResponse response = buildCartResponse(session, deviceId);
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_UPDATED.name(), response);
        return response;
    }

    @Override
    public void lockItem(String cartItemId) {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);
        groupCartRedisRepository.getItem(session.sessionId(), cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        boolean acquired = groupCartRedisRepository.tryLockItem(
                session.sessionId(), cartItemId, deviceId, GroupCartConstants.ITEM_LOCK_TTL_SECONDS);
        if (!acquired) {
            String owner = groupCartRedisRepository.getLockOwner(session.sessionId(), cartItemId).orElse(null);
            if (!deviceId.equals(owner)) {
                throw new AppException(ErrorCode.CART_ITEM_LOCKED);
            }
            return; // idempotent: this device already holds the lock
        }
        touchTtls(session);
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_ITEM_LOCKED.name(),
                new CartLockEvent(cartItemId, deviceId));
    }

    @Override
    public void unlockItem(String cartItemId) {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        requireMember(session.sessionId(), deviceId);

        String owner = groupCartRedisRepository.getLockOwner(session.sessionId(), cartItemId).orElse(null);
        if (!deviceId.equals(owner)) {
            throw new AppException(ErrorCode.CART_LOCK_NOT_HELD);
        }
        groupCartRedisRepository.releaseLock(session.sessionId(), cartItemId);
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_ITEM_UNLOCKED.name(),
                new CartLockEvent(cartItemId, deviceId));
    }

    @Override
    public GroupCartSubmitResponse submit() {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();
        QrSessionMember member = requireMember(session.sessionId(), deviceId);

        // (1) duplicate-submission guard — must be first, released in finally.
        if (!groupCartRedisRepository.tryAcquireSubmitGuard(
                session.sessionId(), deviceId, GroupCartConstants.SUBMIT_GUARD_TTL_SECONDS)) {
            throw new AppException(ErrorCode.CART_SUBMIT_IN_PROGRESS);
        }
        try {
            // (3) owner-only — read role from Redis, never trust the token claim.
            if (member.role() != SessionMemberRole.OWNER) {
                throw new AppException(ErrorCode.TQR_NOT_SESSION_OWNER);
            }

            List<GroupCartItem> items = groupCartRedisRepository.getItems(session.sessionId());
            // (4) empty cart.
            if (items.isEmpty()) {
                throw new AppException(ErrorCode.CART_EMPTY);
            }
            // (5) any line locked by another device → someone is mid-edit.
            for (GroupCartItem item : items) {
                String owner = groupCartRedisRepository.getLockOwner(session.sessionId(), item.cartItemId()).orElse(null);
                if (owner != null && !owner.equals(deviceId)) {
                    throw new AppException(ErrorCode.CART_ITEM_LOCKED);
                }
            }
            // (6) re-validate availability; drop unavailable lines, notify, then fail.
            boolean removedAny = false;
            for (GroupCartItem item : items) {
                if (!isStillAvailable(session.branchId(), item)) {
                    groupCartRedisRepository.removeItem(session.sessionId(), item.cartItemId());
                    removedAny = true;
                }
            }
            if (removedAny) {
                touchTtls(session);
                groupCartSseService.broadcast(session.sessionId(),
                        GroupCartEventType.CART_ITEM_REMOVED.name(), buildCartResponse(session, deviceId));
                throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
            }

            // Snapshot totals before the cart is cleared.
            GroupCartResponse snapshot = buildCartResponse(session, deviceId);

            // (7-8) hand off to the existing order pipeline — it prices, merges and broadcasts.
            CreateOrderRequestDto orderRequest = CreateOrderRequestDto.builder()
                    .branchId(session.branchId())
                    .tableId(session.tableId())
                    .orderType(OrderType.DINE_IN)
                    .customerPhone(session.ownerCustomerPhone())
                    .customerName(null)
                    .items(items.stream().map(this::toOrderItem).toList())
                    .build();
            CreateOrderResponse created = orderService.create(orderRequest);
            String orderId = created.getOrderId();

            // (9) two-way link session ↔ order.
            qrSessionRedisRepository.bindOrder(session.sessionId(), orderId, QrSessionConstants.SESSION_TTL_SECONDS);

            // (10) clear cart + all item locks.
            for (GroupCartItem item : items) {
                groupCartRedisRepository.releaseLock(session.sessionId(), item.cartItemId());
            }
            groupCartRedisRepository.clearCart(session.sessionId());
            touchTtls(session);

            GroupCartSubmitResponse response = GroupCartSubmitResponse.builder()
                    .orderId(orderId)
                    .itemCount(snapshot.getItemCount())
                    .subtotal(snapshot.getSubtotal())
                    .build();
            // (11) notify the table.
            groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.CART_SUBMITTED.name(), response);
            return response;
        } finally {
            // (12) always release the submit guard.
            groupCartRedisRepository.releaseSubmitGuard(session.sessionId());
        }
    }

    // ==== shared helpers (also used by locking/submit milestones) ====

    private boolean isStillAvailable(String branchId, GroupCartItem item) {
        if (item.productId() != null && !item.productId().isBlank()) {
            return productRepository.findByIdAndBranchId(item.productId(), branchId)
                    .map(product -> GroupCartConstants.STATUS_AVAILABLE.equals(product.getStatus()))
                    .orElse(false);
        }
        return comboRepository.findByIdAndBranchId(item.comboId(), branchId)
                .map(combo -> GroupCartConstants.STATUS_AVAILABLE.equals(combo.getStatus()))
                .orElse(false);
    }

    private CreateOrderItemRequestDto toOrderItem(GroupCartItem item) {
        List<CreateOrderItemModifierRequestDto> modifiers = item.modifierOptionIds() == null
                ? List.of()
                : item.modifierOptionIds().stream()
                        .map(optionId -> CreateOrderItemModifierRequestDto.builder()
                                .modifierOptionId(optionId)
                                .build())
                        .toList();
        return CreateOrderItemRequestDto.builder()
                .productId(item.productId())
                .comboId(item.comboId())
                .quantity(item.quantity())
                .note(item.note())
                .modifiers(modifiers)
                .build();
    }

    /** Small SSE payload for lock/unlock events. */
    private record CartLockEvent(String cartItemId, String deviceId) {
    }

    /** Small SSE payload for host handover. */
    private record HostChangedEvent(String previousOwnerDeviceId, String newOwnerDeviceId) {
    }


    /** Resolves the OPEN session from the token, or throws (uc-c-05). */
    QrSessionData currentSession() {
        String sessionId = AuthUtils.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
        QrSessionData session = qrSessionRedisRepository.findSession(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.TQR_SESSION_NOT_FOUND));
        if (session.status() == QrSessionStatus.LOCKED_FOR_PAYMENT) {
            throw new AppException(ErrorCode.TQR_SESSION_LOCKED_FOR_PAYMENT);
        }
        if (session.status() != QrSessionStatus.OPEN) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
        return maybeHandoverHost(session);
    }

    /**
     * Lazy host handover (BR-CST-GRP-04): if the OWNER has been idle longer than
     * {@code HOST_IDLE_TIMEOUT_SECONDS}, promote the earliest-joined still-active MEMBER to OWNER,
     * demote the old owner, move {@code ownerDeviceId}, and broadcast {@code SESSION_HOST_CHANGED}.
     * Runs on request (no {@code @Scheduled}); a no-op when the owner is active or no member is active.
     */
    private QrSessionData maybeHandoverHost(QrSessionData session) {
        List<QrSessionMember> members = qrSessionRedisRepository.getMembers(session.sessionId());
        QrSessionMember owner = members.stream()
                .filter(member -> member.role() == SessionMemberRole.OWNER)
                .findFirst()
                .orElse(null);
        if (owner == null || !isIdle(owner)) {
            return session;
        }
        QrSessionMember newOwner = members.stream()
                .filter(member -> member.role() == SessionMemberRole.MEMBER)
                .filter(member -> !isIdle(member))
                .min(java.util.Comparator.comparing(QrSessionMember::joinedAt))
                .orElse(null);
        if (newOwner == null) {
            return session; // nobody active to take over — let the TTL clean up
        }

        Instant now = Instant.now();
        qrSessionRedisRepository.saveMember(session.sessionId(),
                new QrSessionMember(newOwner.deviceId(), SessionMemberRole.OWNER, newOwner.customerId(),
                        newOwner.customerPhone(), newOwner.joinedAt(), now),
                QrSessionConstants.SESSION_TTL_SECONDS);
        qrSessionRedisRepository.saveMember(session.sessionId(),
                new QrSessionMember(owner.deviceId(), SessionMemberRole.MEMBER, owner.customerId(),
                        owner.customerPhone(), owner.joinedAt(), owner.lastSeenAt()),
                QrSessionConstants.SESSION_TTL_SECONDS);
        qrSessionRedisRepository.updateOwnerDeviceId(session.sessionId(), newOwner.deviceId());

        QrSessionData updated = new QrSessionData(session.sessionId(), session.organizationId(),
                session.branchId(), session.tableId(), newOwner.deviceId(), session.ownerCustomerId(),
                session.ownerCustomerPhone(), session.orderId(), session.status(), session.createdAt());
        groupCartSseService.broadcast(session.sessionId(), GroupCartEventType.SESSION_HOST_CHANGED.name(),
                new HostChangedEvent(owner.deviceId(), newOwner.deviceId()));
        return updated;
    }

    private boolean isIdle(QrSessionMember member) {
        return member.lastSeenAt() == null
                || member.lastSeenAt().isBefore(
                        Instant.now().minusSeconds(GroupCartConstants.HOST_IDLE_TIMEOUT_SECONDS));
    }

    QrSessionMember requireMember(String sessionId, String deviceId) {
        return qrSessionRedisRepository.getMember(sessionId, deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_MEMBER_NOT_FOUND));
    }

    private void touchTtls(QrSessionData session) {
        qrSessionRedisRepository.touchTtl(session, QrSessionConstants.SESSION_TTL_SECONDS);
        groupCartRedisRepository.touchCartTtl(session.sessionId(), QrSessionConstants.SESSION_TTL_SECONDS);
    }

    /**
     * Ensures the caller may mutate a line: fails with {@code CART_ITEM_LOCKED} if another device
     * holds the lock; otherwise acquires it transiently (returns true so the caller releases it).
     */
    private boolean acquireItemLockForOperation(String sessionId, String cartItemId, String deviceId) {
        Optional<String> owner = groupCartRedisRepository.getLockOwner(sessionId, cartItemId);
        if (owner.isPresent()) {
            if (!owner.get().equals(deviceId)) {
                throw new AppException(ErrorCode.CART_ITEM_LOCKED);
            }
            return false; // this device already holds an explicit lock — leave it in place
        }
        if (!groupCartRedisRepository.tryLockItem(
                sessionId, cartItemId, deviceId, GroupCartConstants.ITEM_LOCK_TTL_SECONDS)) {
            throw new AppException(ErrorCode.CART_ITEM_LOCKED);
        }
        return true;
    }

    private void validateMenuItemAvailable(String branchId, String productId, String comboId) {
        if (productId != null && !productId.isBlank()) {
            Product product = productRepository.findByIdAndBranchId(productId, branchId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_MENU_ITEM_NOT_IN_BRANCH));
            if (!GroupCartConstants.STATUS_AVAILABLE.equals(product.getStatus())) {
                throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
            }
            return;
        }
        Combo combo = comboRepository.findByIdAndBranchId(comboId, branchId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_MENU_ITEM_NOT_IN_BRANCH));
        if (!GroupCartConstants.STATUS_AVAILABLE.equals(combo.getStatus())) {
            throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
        }
    }

    private void validateModifiers(String branchId, List<String> modifierOptionIds) {
        if (modifierOptionIds == null || modifierOptionIds.isEmpty()) {
            return;
        }
        for (String optionId : modifierOptionIds) {
            ModifierOption option = modifierOptionRepository
                    .findByIdAndModifierGroup_Product_Branch_Id(optionId, branchId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_MODIFIER_INVALID));
            if (!GroupCartConstants.STATUS_AVAILABLE.equals(option.getStatus())) {
                throw new AppException(ErrorCode.CART_MODIFIER_INVALID);
            }
        }
    }

    GroupCartResponse buildCartResponse(QrSessionData session, String deviceId) {
        SessionMemberRole role = requireMember(session.sessionId(), deviceId).role();
        List<GroupCartItem> items = groupCartRedisRepository.getItems(session.sessionId());

        List<GroupCartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (GroupCartItem item : items) {
            GroupCartItemResponse itemResponse = toItemResponse(session.branchId(), session.sessionId(), item);
            itemResponses.add(itemResponse);
            subtotal = subtotal.add(itemResponse.getLineTotal());
        }

        return GroupCartResponse.builder()
                .sessionId(session.sessionId())
                .tableId(session.tableId())
                .role(role)
                .items(itemResponses)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .itemCount(itemResponses.size())
                .submitAllowed(role == SessionMemberRole.OWNER)
                .build();
    }

    private GroupCartItemResponse toItemResponse(String branchId, String sessionId, GroupCartItem item) {
        String name = null;
        BigDecimal unitPrice = BigDecimal.ZERO;
        if (item.productId() != null && !item.productId().isBlank()) {
            Product product = productRepository.findByIdAndBranchId(item.productId(), branchId).orElse(null);
            if (product != null) {
                name = product.getProductName();
                unitPrice = product.getPrice();
            }
        } else if (item.comboId() != null && !item.comboId().isBlank()) {
            Combo combo = comboRepository.findByIdAndBranchId(item.comboId(), branchId).orElse(null);
            if (combo != null) {
                name = combo.getComboName();
                unitPrice = combo.getPrice();
            }
        }

        List<GroupCartItemModifierResponse> modifierResponses = new ArrayList<>();
        BigDecimal modifiersTotal = BigDecimal.ZERO;
        if (item.modifierOptionIds() != null) {
            for (String optionId : item.modifierOptionIds()) {
                ModifierOption option = modifierOptionRepository
                        .findByIdAndModifierGroup_Product_Branch_Id(optionId, branchId)
                        .orElse(null);
                if (option == null) {
                    continue;
                }
                modifiersTotal = modifiersTotal.add(option.getAdditionalPrice());
                modifierResponses.add(GroupCartItemModifierResponse.builder()
                        .modifierOptionId(optionId)
                        .optionName(option.getOptionName())
                        .additionalPrice(option.getAdditionalPrice())
                        .build());
            }
        }

        // Matches OrderServiceImpl: lineTotal = unitPrice * quantity + sum(additionalPrice).
        BigDecimal lineTotal = unitPrice
                .multiply(BigDecimal.valueOf(item.quantity()))
                .add(modifiersTotal)
                .setScale(2, RoundingMode.HALF_UP);

        return GroupCartItemResponse.builder()
                .cartItemId(item.cartItemId())
                .productId(item.productId())
                .comboId(item.comboId())
                .name(name)
                .unitPrice(unitPrice)
                .quantity(item.quantity())
                .lineTotal(lineTotal)
                .note(item.note())
                .modifiers(modifierResponses)
                .addedByDeviceId(item.addedByDeviceId())
                .lockedByDeviceId(groupCartRedisRepository.getLockOwner(sessionId, item.cartItemId()).orElse(null))
                .build();
    }
}
