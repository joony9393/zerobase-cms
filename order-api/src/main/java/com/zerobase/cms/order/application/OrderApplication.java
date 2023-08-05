package com.zerobase.cms.order.application;

import com.zerobase.cms.order.client.UserClient;
import com.zerobase.cms.order.client.user.ChangeBalanceForm;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.domain.ProductItem;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.EmailSendService;
import com.zerobase.cms.order.service.ProductItemService;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplication {
    private final CartApplication cartApplication;
    private final UserClient userClient;
    private final ProductItemService productItemService;
    private final EmailSendService emailSendService;

    //결제를 위해 필요한 것.
    //1. 물건들이 주문 가능한가?
    //2. 가격 변동이 있었나?
    //3. 고객의 돈이 충분한가?
    //4. 결제 품 재고 관리
    //5. 주문 내역 이메일 발송처리.

    @Transactional
    public void order(String token, Cart cart){
        // 기존 카트를 버리는가, 선택주문으로 처리하는가?
        // 일단 기존 카트를 버리는 것으로 진행.
        Cart orderCart = cartApplication.refreshCart(cart);
        //Cart orderCart = cartApplication.getCart(customerId);

        if(orderCart.getMessages().size() > 0){
            //문제가 있다. 카트를 덮어쓰기 하는 과정에서 정보 불일치의 문제가 발생한 것이다.
            throw new CustomException(ErrorCode.ORDER_FAIL_CHEKCK_CART);
        }

        CustomerDto customerDto = userClient.getCustomerInfo(token).getBody();

        if(customerDto.getBalance() < getTotalPrice(cart)){
            throw new CustomException(ErrorCode.ORDER_FAIL_NO_MONEY);
        }

        int totalPrice = getTotalPrice(cart);

        //롤백에 대해서 생각해야 함.
        userClient.changeBalance(
            token,
            ChangeBalanceForm.builder()
                .from("USER")
                .message("Order")
                .money( (-1)*totalPrice )
                .build()
            );

        //주문 결제 내역 이메일 전송.
        emailSendService.sendOrderListEmail(token, cart, totalPrice);

        for(Cart.Product product : orderCart.getProducts()){
            for(Cart.ProductItem cartItem : product.getItems()){
                ProductItem productItem = productItemService.getProductItem(cartItem.getId());
                productItem.setCount(productItem.getCount() - cartItem.getCount() );
                productItemService.saveProductItem(productItem);
            }//inner for
        }//Outer for
    }


    private Integer getTotalPrice(Cart cart){
        return cart.getProducts().stream().flatMapToInt(
            product -> product.getItems().stream().flatMapToInt(
                productItem -> IntStream.of(productItem.getPrice()* productItem.getCount())
            )
        ).sum();
    }

}
























