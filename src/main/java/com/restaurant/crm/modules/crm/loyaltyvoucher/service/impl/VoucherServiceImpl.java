package com.restaurant.crm.modules.crm.loyaltyvoucher.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.mapper.VoucherMapper;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.VoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.VoucherService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherServiceImpl implements VoucherService {

    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherCreationRequest request) {
        Voucher voucher = voucherMapper.toVoucher(request);
        voucher.setIsActive((short) 1); // default
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(String id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        voucherMapper.updateVoucher(request, voucher);
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherResponse(voucher);
    }

    @Override
    public PagingResponse<VoucherResponse> getActiveVouchersByRestaurant(String restaurantId, int page, int size) {
        int adjustedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Voucher> voucherPage = voucherRepository.findByRestaurantIdAndIsActive(restaurantId, (short) 1, pageable);

        return PagingResponse.<VoucherResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(voucherPage.getTotalPages())
                .totalElement(voucherPage.getTotalElements())
                .data(voucherPage.getContent().stream().map(voucherMapper::toVoucherResponse).toList())
                .build();
    }
}
