package com.zerobase.controller;

import com.zerobase.domain.common.UserVo;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import com.zerobase.domain.customer.ChangeBalanceForm;
import com.zerobase.domain.customer.CustomerDto;
import com.zerobase.domain.model.CustomerEntity;
import com.zerobase.exception.CustomException;
import com.zerobase.exception.ErrorCode;
import com.zerobase.service.customer.CustomerBalanceService;
import com.zerobase.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final JwtAuthenticationProvider provider;
    private final CustomerService customerService;
    private final CustomerBalanceService customerBalanceSErvice;


    @GetMapping("/getInfo")
    public ResponseEntity<CustomerDto> getInfo(
        @RequestHeader(name = "X-AUTH-TOKEN") String token
    ){
        UserVo vo = provider.getUserVo(token);
        CustomerEntity customerEntity = customerService.findByIdAndEmail(vo.getId(), vo.getEmail()).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );
        return ResponseEntity.ok(CustomerDto.from(customerEntity));
    }



    @PostMapping("/balance")
    public ResponseEntity<Integer> changeBalance(
        @RequestHeader(name = "X-AUTH-TOKEN") String token, @RequestBody ChangeBalanceForm form
    ){
        UserVo vo = provider.getUserVo(token);

        return ResponseEntity.ok(customerBalanceSErvice.changeBalance(vo.getId(), form).getCurrentMoney());
    }

}















