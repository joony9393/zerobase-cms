package com.zerobase.cms.order.application;

import static org.junit.jupiter.api.Assertions.*;

import com.zerobase.cms.order.domain.Product;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.product.AddProductForm;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CartApplicationTest {
    @Autowired
    private CartApplication cartApplication;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;



    @Test
    void ADD_and_MODIFY_TEST(){
        Long customerId = 100L;

        cartApplication.clearCart(customerId);

        Product p = add_product();

        Product result = productRepository.findWithProductItemsById(p.getId()).get();

        assertNotNull(result);

        //나머지 필드들에 대한 검증
        assertEquals(result.getName(), "나이키 에어포스");
        assertEquals(result.getDescription(), "신발입니다");

        assertEquals(result.getProductItems().size(), 3);
        assertEquals(result.getProductItems().get(0).getName(), "나이키 에어포스0");
        assertEquals(result.getProductItems().get(0).getPrice(), 10000);
        //assertEquals(result.getProductItems().get(0).getCount(), 1);

        Cart cart = cartApplication.addCart(customerId, makeAddForm(result) );

        //데이터 잘 들어갔는지?
        assertEquals(cart.getMessages().size(), 0);

        //카트 수정사항이 잘 반영 되는지? 즉, 가격을 변동시켰는데 메시지가 카트 내에 잘 들어 있는가?
        cart = cartApplication.getCart(customerId);
        assertEquals(cart.getMessages().size(), 1);

        //필드 검증 추가.
        //그렇다면 메시지의 내용은 무엇인가? 가격 변동과 관련된 메시지인가?
        assertEquals(cart.getMessages().get(0), "나이키 에어포스상품의 변동 사항 : 나이키 에어포스0 가격이 변동되었습니다., ");
    }




    AddProductCartForm makeAddForm(Product p){
        AddProductCartForm.ProductItem productItem =
            AddProductCartForm.ProductItem.builder()
                .id(p.getProductItems().get(0).getId())
                .name(p.getProductItems().get(0).getName())
                .count(5)
                .price(20000)
                .build();

        return AddProductCartForm.builder()
                .id(p.getId())
                .sellerId(p.getSellerId())
                .name(p.getName())
                .description(p.getDescription())
                .items(List.of(productItem))
                .build();
    }




    @Test
    Product add_product(){
        Long sellerId = 1L;

        AddProductForm form = makeProductForm("나이키 에어포스", "신발입니다", 3);
        return productService.addProduct(sellerId, form);
    }

    private static AddProductForm makeProductForm(String name, String description, int itemCount){
        List<AddProductItemForm> itemForms = new ArrayList<>();
        for(int i=0; i<itemCount; i++){
            itemForms.add(
                makeProductItemForm(null, name+i)
            );
        }
        return AddProductForm.builder()
            .name(name)
            .description(description)
            .items(itemForms)
            .build();
    }


    private static final AddProductItemForm makeProductItemForm(Long productId, String name){
        return AddProductItemForm.builder()
            .productId(productId)
            .name(name)
            .price((10000))
            .count(10)
            .build();
    }


}