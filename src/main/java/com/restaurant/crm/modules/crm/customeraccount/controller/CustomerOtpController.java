package com.restaurant.crm.modules.crm.customeraccount.controller;


import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.crm.customeraccount.dto.request.CheckPhoneRequest;
import com.restaurant.crm.modules.crm.customeraccount.dto.request.CustomerIdentifyRequest;
import com.restaurant.crm.modules.crm.customeraccount.dto.response.CheckPhoneResponse;
import com.restaurant.crm.modules.crm.customeraccount.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.CustomerOtpService;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("crmCustomerAccountOtpController")
@RequestMapping("/api/v1/public/customer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerOtpController {
//    CustomerOtpService customerOtpService;
//    CustomerRepository customerRepository;
//
//
//    @PostMapping("/check-phone")
//    public ResponseEntity<ApiResponse<CheckPhoneResponse>> checkPhone (@RequestBody @Valid CheckPhoneRequest request){
//        var customerOtp = customerRepository.findByPhone(request.getPhone());
//        CheckPhoneResponse responseData;
//        if (customerOtp.isPresent()){
//            responseData = CheckPhoneResponse.builder()
//                    .phone(customerOtp.get().getPhone())
//                    .build();
//
//        } else {
//            responseData = CheckPhoneResponse.builder()
//                    .phone(null)
//                    .build();
//        }
//        return ResponseEntity.ok(ApiResponse.<CheckPhoneResponse>builder()
//                .success(true)
//                .data(responseData)
//                .build());
//    }
//
    CustomerRepository customerRepository;
    CustomerService customerService;
    CustomerOtpService customerOtpService;

    @PostMapping("/check-phone")
    public ResponseEntity<ApiResponse<CheckPhoneResponse>> checkPhone(@RequestBody @Valid
                                                                          CheckPhoneRequest request){
        boolean isExist = customerRepository.existsByPhone(request.getPhone());
        var responseData = CheckPhoneResponse.builder()
                .phone(request.getPhone())
                .exists(isExist)
                .build();

        return ResponseEntity.ok(ApiResponse.<CheckPhoneResponse>builder()
                .success(true)
                .data(responseData)
                .build());
    }

    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<Object>> requestOtp(
            @RequestParam String phone,
            @RequestParam String branchId,
            @RequestParam String tableId
    ) {
        var result = customerOtpService.request(phone, branchId, tableId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(result)
                .build());

    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<CustomerResponse>> verifyOtp (
            @RequestParam String phone,
            @RequestParam String branchId,
            @RequestParam String tabledId,
            @RequestParam String otpCode,
            @RequestParam String restaurantId
    ){
//        verify OTP -> fail -> throw E & stop here
        customerOtpService.verify(phone, branchId, tabledId, otpCode);

//        verify OTP -> true -> save user and create point wallet
        CustomerIdentifyRequest identifyRequest = CustomerIdentifyRequest.builder()
                .phone(phone)
                .restaurantId(restaurantId)
                .build();

//        save customer wallet -> db
        CustomerResponse customerResponse = customerService.identifyAndInitializeWallet(identifyRequest);

//        throw data -> client
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .data(customerResponse)
                .build());
    }

}
