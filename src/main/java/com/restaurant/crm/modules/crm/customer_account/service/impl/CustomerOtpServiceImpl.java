package com.restaurant.crm.modules.crm.customer_account.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.constants.CustomerOtpConstants;
import com.restaurant.crm.modules.crm.customer_account.dto.request.OtpRequestRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.request.OtpVerifyRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.response.OtpRequestResponse;
import com.restaurant.crm.modules.crm.customer_account.dto.response.OtpVerifyResponse;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customer_account.model.OtpCodeEntry;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customer_account.repository.OtpRedisRepository;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.CustomerOtpService;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpSender;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpTicketService;
import com.restaurant.crm.modules.crm.customer_account.utils.PhoneNumberUtils;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import com.restaurant.crm.modules.erp.order.service.interfaces.TableQrTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * OTP request/verify orchestration (uc-c-03, BR-CST-ACC-02). OTP lives in Redis only;
 * branch/table always come from a verified TABLE QR (NFR-07), never from client input.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerOtpServiceImpl implements CustomerOtpService {

    TableQrTokenService tableQrTokenService;
    CustomerRepository customerRepository;
    OtpRedisRepository otpRedisRepository;
    OtpTicketService otpTicketService;
    OtpSender otpSender;

    SecureRandom secureRandom = new SecureRandom();

    @NonFinal
    @Value("${security.otp.signer-key:${security.jwt.signer-key}}")
    String otpSignerKey;

    @Override
    @Transactional(readOnly = true)
    public OtpRequestResponse request(OtpRequestRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        String phone = normalizeAndValidate(request.getCustomerPhone());

        // Read-only LOCKED check — never create a customer here.
        customerRepository.findByPhone(phone)
                .filter(customer -> customer.getStatus() == CustomerStatus.LOCKED)
                .ifPresent(locked -> { throw new AppException(ErrorCode.OTP_CUSTOMER_LOCKED); });

        if (otpRedisRepository.isPhoneLocked(phone)) {
            throw new AppException(ErrorCode.OTP_PHONE_LOCKED);
        }
        if (otpRedisRepository.isResendBlocked(phone)) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_SOON);
        }

        long tableCount = otpRedisRepository.incrementTableCounter(
                payload.branchId(), payload.tableId(), CustomerOtpConstants.TABLE_RATE_TTL_SECONDS);
        if (tableCount > CustomerOtpConstants.TABLE_RATE_LIMIT) {
            throw new AppException(ErrorCode.OTP_TABLE_RATE_LIMIT);
        }

        String code = generateCode();
        otpRedisRepository.saveCode(phone, hmacCode(phone, code),
                payload.branchId(), payload.tableId(), CustomerOtpConstants.CODE_TTL_SECONDS);
        otpRedisRepository.markResend(phone, CustomerOtpConstants.RESEND_TTL_SECONDS);

        // On send failure, keep the stored code (customer may retry sending is out of scope).
        otpSender.send(phone, code);

        Instant now = Instant.now();
        return OtpRequestResponse.builder()
                .maskedPhone(PhoneNumberUtils.mask(phone))
                .expiresAt(now.plusSeconds(CustomerOtpConstants.CODE_TTL_SECONDS))
                .resendAvailableAt(now.plusSeconds(CustomerOtpConstants.RESEND_TTL_SECONDS))
                .attemptsAllowed(CustomerOtpConstants.MAX_ATTEMPTS)
                .build();
    }

    @Override
    public OtpVerifyResponse verify(OtpVerifyRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        String phone = normalizeAndValidate(request.getCustomerPhone());

        OtpCodeEntry entry = otpRedisRepository.findCode(phone)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_EXPIRED));

        if (!payload.branchId().equals(entry.branchId())
                || !payload.tableId().equals(entry.tableId())) {
            throw new AppException(ErrorCode.OTP_CONTEXT_MISMATCH);
        }

        if (!codeMatches(phone, request.getOtpCode(), entry.codeHmac())) {
            long attempts = otpRedisRepository.incrementAttempts(phone);
            if (attempts >= CustomerOtpConstants.MAX_ATTEMPTS) {
                otpRedisRepository.deleteCode(phone);
                otpRedisRepository.lockPhone(phone, CustomerOtpConstants.LOCK_TTL_SECONDS);
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS);
            }
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        otpRedisRepository.deleteCode(phone);
        otpRedisRepository.unlockPhone(phone);

        OtpTicketService.IssuedTicket ticket = otpTicketService.issue(
                phone, payload.organizationId(), payload.branchId(), payload.tableId());
        log.info("OTP verified for {} at branch {} table {}",
                PhoneNumberUtils.mask(phone), payload.branchId(), payload.tableId());

        return OtpVerifyResponse.builder()
                .otpTicket(ticket.token())
                .ticketExpiresAt(ticket.expiresAt())
                .build();
    }

    // ==== helpers ====

    private String normalizeAndValidate(String rawPhone) {
        String phone = PhoneNumberUtils.normalize(rawPhone);
        if (!PhoneNumberUtils.isValid(phone)) {
            throw new AppException(ErrorCode.CUSTOMER_PHONE_INVALID);
        }
        return phone;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CustomerOtpConstants.OTP_LENGTH);
        int value = secureRandom.nextInt(bound);
        return String.format("%0" + CustomerOtpConstants.OTP_LENGTH + "d", value);
    }

    /** HMAC-SHA256 over {@code phone + ":" + code} so each code has its own hash space. */
    private String hmacCode(String phone, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpSignerKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((phone + ":" + code).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new AppException(ErrorCode.OTP_TICKET_GENERATION_FAILED);
        }
    }

    private boolean codeMatches(String phone, String code, String storedHmac) {
        String candidate = hmacCode(phone, code);
        return MessageDigest.isEqual(
                candidate.getBytes(StandardCharsets.UTF_8),
                Optional.ofNullable(storedHmac).orElse("").getBytes(StandardCharsets.UTF_8));
    }
}
