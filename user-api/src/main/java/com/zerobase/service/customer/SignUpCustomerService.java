package com.zerobase.service.customer;

import com.zerobase.domain.SignUpForm;
import com.zerobase.domain.model.CustomerEntity;
import com.zerobase.domain.repository.CustomerRepository;
import com.zerobase.exception.CustomException;
import com.zerobase.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpCustomerService {

    private final CustomerRepository customerRepository;

    public CustomerEntity signUp(SignUpForm form){
        return customerRepository.save(
            CustomerEntity.from(form)
        );
    }



    public boolean isEmailExist(String email){
        return customerRepository.findByEmail(email.toLowerCase(Locale.ROOT)).isPresent();
    }



    @Transactional
    public void verifyEmail(String email, String code){
        CustomerEntity customerEntity = customerRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        if(customerEntity.isVerify()){
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        }
        if(!customerEntity.getVerificationCode().equals(code)){
            throw new CustomException(ErrorCode.WRONG_VERIFICATION);
        }
        if(customerEntity.getVerifyExpiredAt().isBefore(LocalDateTime.now())){
            throw new CustomException(ErrorCode.EXPIRED_CODE);
        }
        customerEntity.setVerify(true);
    }



    @Transactional
    public LocalDateTime changeCustomerValidateEmail(Long customerId, String verificationCode){
        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findById(customerId);
        if(optionalCustomerEntity.isPresent()){
            CustomerEntity customerEntity = optionalCustomerEntity.get();
            customerEntity.setVerificationCode(verificationCode);
            customerEntity.setVerifyExpiredAt(LocalDateTime.now().plusDays(1));
            return customerEntity.getVerifyExpiredAt();
        } else {
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        }
    }

}
