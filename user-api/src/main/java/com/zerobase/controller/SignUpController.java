package com.zerobase.controller;

import com.zerobase.application.SignUpApplication;
import com.zerobase.domain.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignUpController {
    private final SignUpApplication signUpApplication;

    @PostMapping("/customer")
    public ResponseEntity<String> customerSignUp(
        @RequestBody SignUpForm form
    ){
        return ResponseEntity.ok(signUpApplication.customerSignUp(form));
    }



    @GetMapping("/customer/verify")
    public ResponseEntity<String> verifyCustomer(
        @RequestParam String email, @RequestParam String code
    ){
        signUpApplication.customerVerify(email, code);
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }



    @PostMapping("/seller")
    public ResponseEntity<String> sellerSignUp(
        @RequestBody SignUpForm form
    ){
        return ResponseEntity.ok(signUpApplication.sellerSignUp(form));
    }



    @GetMapping("/seller/verify")
    public ResponseEntity<String> verifySeller(
        @RequestParam String email, @RequestParam String code
    ){
        signUpApplication.sellerVerify(email, code);
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }
}
