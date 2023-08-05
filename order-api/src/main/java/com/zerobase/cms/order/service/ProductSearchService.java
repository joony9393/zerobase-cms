package com.zerobase.cms.order.service;

import com.zerobase.cms.order.domain.Product;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;


    public List<Product> searchByName(String name){
        return productRepository.searchByName(name);
    }



    @Transactional(readOnly = true)
    public Product getByProductId(Long productId){
        return productRepository.findWithProductItemsById(productId)
            .orElseThrow( () -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT));
    }



    @Transactional(readOnly = true)
    public List<Product> getListByProductIds(List<Long> productIds){
        return productRepository.findAllByIdIn(productIds);
    }

}





















