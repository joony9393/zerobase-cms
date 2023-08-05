package com.zerobase.cms.order.service;

import com.zerobase.cms.order.domain.Product;
import com.zerobase.cms.order.domain.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductForm;
import com.zerobase.cms.order.domain.product.UpdateProductForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product addProduct(
        Long sellerId, AddProductForm form
    ){
        return productRepository.save(Product.of(sellerId, form));
    }



    @Transactional
    public Product updateProduct(Long sellerId, UpdateProductForm form){
        Product product = productRepository.findBySellerIdAndId(sellerId, form.getId())
            .orElseThrow( () -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT));
        product.setName(form.getName());
        product.setDescription(form.getDescription());

        for(UpdateProductItemForm itemForm : form.getItems()){
            ProductItem item = product.getProductItems().stream()
                .filter(pi -> pi.getId().equals(itemForm.getId()))
                .findFirst().orElseThrow(
                    () -> new CustomException(ErrorCode.NOT_FOUND_ITEM)
                );
            item.setName(itemForm.getName());
            item.setPrice(itemForm.getPrice());
            item.setCount(itemForm.getCount());
        }
        return product;
    }



    @Transactional
    public void deleteProduct(Long sellerId, Long productId){
        Product product = productRepository.findBySellerIdAndId(sellerId, productId)
            .orElseThrow( () -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT) );

        //Product 엔티티 클래스를 정의할 때, @OneToMany(cascade = CascadeType.ALL)이라고 정의해준 것이 기억날 것이다.
        //프로덕트를 삭제하면, 그거에 딸려있는 프로덕트 아이템들도 전부 삭제하기 위한 것이다.
        productRepository.delete(product);
    }

}























