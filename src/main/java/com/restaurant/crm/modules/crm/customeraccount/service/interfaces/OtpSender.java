package com.restaurant.crm.modules.crm.customeraccount.service.interfaces;


public interface OtpSender {


    void send(String phone, String code);
}
