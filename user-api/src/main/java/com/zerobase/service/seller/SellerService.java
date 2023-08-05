package com.zerobase.service.seller;

import com.zerobase.domain.SignUpForm;
import com.zerobase.domain.model.CustomerEntity;
import com.zerobase.domain.model.SellerEntity;
import com.zerobase.domain.repository.SellerRepository;
import com.zerobase.exception.CustomException;
import com.zerobase.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;


    public Optional<SellerEntity> findByIdAndEmail(
        Long id, String email
    ){
        return sellerRepository.findByIdAndEmail(id, email);
    }



    public Optional<SellerEntity> findValidSeller(String email, String password){
        return sellerRepository.findByEmailAndPasswordAndVerifyIsTrue(email, password);
    }



    public SellerEntity signUp(SignUpForm form){
        return sellerRepository.save(SellerEntity.from(form));
    }



    public boolean isEmailExist(String email){
        return sellerRepository.findByEmail(email).isPresent();
    }



    @Transactional
    public void verifyEmail(String email, String code){
        SellerEntity sellerEntity = sellerRepository.findByEmail(email).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );
        if(sellerEntity.isVerify()){
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        }
        if(!sellerEntity.getVerificationCode().equals(code)){
            throw new CustomException(ErrorCode.WRONG_VERIFICATION);
        }
        if(sellerEntity.getVerifyExpiredAt().isBefore(LocalDateTime.now())){
            throw new CustomException(ErrorCode.EXPIRED_CODE);
        }
        sellerEntity.setVerify(true);
    }



    @Transactional
    public LocalDateTime changeSellerValidateEmail(Long sellerId, String verificationCode){
        Optional<SellerEntity> optionalSellerEntity = sellerRepository.findById(sellerId);
        if(optionalSellerEntity.isPresent()){
            SellerEntity sellerEntity = optionalSellerEntity.get();
            sellerEntity.setVerificationCode(verificationCode);
            sellerEntity.setVerifyExpiredAt(LocalDateTime.now().plusDays(1));
            return sellerEntity.getVerifyExpiredAt();
        } else {
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        }
    }

}


















