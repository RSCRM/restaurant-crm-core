package com.restaurant.crm.modules.crm.customeraccount.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerOtpConstants;
import com.restaurant.crm.modules.crm.customeraccount.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customeraccount.model.OtpCodeEntry;
import com.restaurant.crm.modules.crm.customeraccount.model.OtpRequestResult;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customeraccount.repository.OtpRedisRepository;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.CustomerOtpService;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpSender;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpTicketService;
import com.restaurant.crm.modules.crm.customeraccount.utils.PhoneNumberUtils;

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
 * OTP request/verify orchestration (uc-c-03, BR-CST-ACC-02). QR-agnostic: branch/table arrive
 * as trusted parameters, so this class imports nothing from the erp domain. OTP lives in Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerOtpServiceImpl implements CustomerOtpService {

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
    public OtpRequestResult request(String customerPhone, String branchId, String tableId) {
        String phone = normalizeAndValidate(customerPhone);

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
                branchId, tableId, CustomerOtpConstants.TABLE_RATE_TTL_SECONDS);
        if (tableCount > CustomerOtpConstants.TABLE_RATE_LIMIT) {
            throw new AppException(ErrorCode.OTP_TABLE_RATE_LIMIT);
        }

        String code = generateCode();
        otpRedisRepository.saveCode(phone, hmacCode(phone, code),
                branchId, tableId, CustomerOtpConstants.CODE_TTL_SECONDS);

        // Send BEFORE marking the resend cooldown: a failed send must not burn the customer's
        // 60s cooldown (and the stored code is kept so they can retry).
        otpSender.send(phone, code);
        otpRedisRepository.markResend(phone, CustomerOtpConstants.RESEND_TTL_SECONDS);

        Instant now = Instant.now();
        return new OtpRequestResult(
                PhoneNumberUtils.mask(phone),
                now.plusSeconds(CustomerOtpConstants.CODE_TTL_SECONDS),
                now.plusSeconds(CustomerOtpConstants.RESEND_TTL_SECONDS));
    }

    @Override
    public String verify(String customerPhone, String branchId, String tableId, String otpCode) {
        String phone = normalizeAndValidate(customerPhone);

        OtpCodeEntry entry = otpRedisRepository.findCode(phone)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_EXPIRED));

        if (!branchId.equals(entry.branchId()) || !tableId.equals(entry.tableId())) {
            throw new AppException(ErrorCode.OTP_CONTEXT_MISMATCH);
        }

        if (!codeMatches(phone, otpCode, entry.codeHmac())) {
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

        OtpTicketService.IssuedTicket ticket = otpTicketService.issue(phone, branchId, tableId);
        log.info("OTP verified for {} at branch {} table {}",
                PhoneNumberUtils.mask(phone), branchId, tableId);
        return ticket.token();
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
