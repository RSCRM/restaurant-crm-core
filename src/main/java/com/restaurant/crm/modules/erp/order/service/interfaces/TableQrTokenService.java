package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.model.TableQrPayload;

/**
 * Signs and verifies the static TABLE QR token printed on each table (uc-c-02).
 * The QR carries no {@code exp} (it lives on paper) and is revoked by bumping
 * {@code qrVersion}. Generation is called by the Manager module (uc-m-*); this
 * service only owns the crypto contract.
 */
public interface TableQrTokenService {

    /**
     * Signs a TABLE QR token for the given table context (uc-c-02).
     * Secret is derived from {@code organizationId + branchId}, so a token can
     * only be minted by someone holding the QR signer key.
     *
     * @param payload organization, branch, table and qr-version to embed
     * @return serialized compact JWS string to encode into the printed QR
     */
    String generate(TableQrPayload payload);

    /**
     * Verifies a TABLE QR token and returns its trusted payload (uc-c-02).
     * Throws {@code AppException} with a {@code TQR_*} error code on any failure;
     * the returned payload is safe to trust for branch isolation (NFR-07).
     *
     * @param token serialized compact JWS string read from the scanned QR
     * @return verified {@link TableQrPayload}
     */
    TableQrPayload verify(String token);
}
