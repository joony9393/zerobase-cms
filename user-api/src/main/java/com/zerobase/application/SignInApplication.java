package com.zerobase.application;

import com.zerobase.domain.SignInForm;
import com.zerobase.domain.common.UserType;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import com.zerobase.domain.model.CustomerEntity;
import com.zerobase.domain.model.SellerEntity;
import com.zerobase.exception.CustomException;
import com.zerobase.exception.ErrorCode;
import com.zerobase.service.customer.CustomerService;
import com.zerobase.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignInApplication {
    private final CustomerService customerService;
    private final SellerService sellerService;
    private final JwtAuthenticationProvider provider;

    public String customerLoginToken(SignInForm form){
        //1. 로그인 가능 여부
        CustomerEntity customerEntity = customerService.findValidCustomer(form.getEmail(), form.getPassword())
            .orElseThrow( () -> new CustomException(ErrorCode.LOGIN_CHECK_FAIL));

        //2. 토큰 발행
        //3. 토큰 리스펀스한다.
        return provider.createToken(customerEntity.getEmail(), customerEntity.getId(), UserType.CUSTOMER);
    }


    public String sellerLoginToken(SignInForm form){
        SellerEntity sellerEntity = sellerService.findValidSeller(form.getEmail(), form.getPassword())
            .orElseThrow( () -> new CustomException(ErrorCode.LOGIN_CHECK_FAIL));
        return provider.createToken(sellerEntity.getEmail(), sellerEntity.getId(), UserType.SELLER);
    }
}
