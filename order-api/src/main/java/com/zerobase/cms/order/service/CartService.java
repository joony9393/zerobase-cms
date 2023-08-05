package com.zerobase.cms.order.service;

import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.client.CustomRedisClient;
import com.zerobase.cms.order.domain.redis.Cart.Product;
import com.zerobase.cms.order.domain.redis.Cart.ProductItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private final CustomRedisClient customRedisClient;


    public Cart getCart(Long customerId){
        Cart cart = customRedisClient.get(customerId, Cart.class);
        return cart != null ? cart : new Cart();
    }



    public Cart putCart(Long customerId, Cart cart){
        customRedisClient.put(customerId, cart);
        return cart;
    }



    public Cart addCart(Long customerId, AddProductCartForm form){
        Cart cart = customRedisClient.get(customerId, Cart.class);
        if(cart == null){
            cart = new Cart();
            cart.setCustomerId(customerId);
        }
        //이전에 같은 상품이 있는지?
        Optional<Cart.Product> productOptional = cart.getProducts().stream().filter(
            product -> product.getId().equals(form.getId())
        ).findFirst();

        if(productOptional.isPresent()){
            //같은 상품이 존재하는 경우
            Cart.Product redisProduct = productOptional.get();

            List<Cart.ProductItem> items = form.getItems().stream()
                .map(Cart.ProductItem::from).collect(Collectors.toList());

            Map<Long, Cart.ProductItem> redisItemMap = redisProduct.getItems().stream()
                .collect( Collectors.toMap(it -> it.getId(), it -> it) );

            if(!redisProduct.getName().equals(form.getName())){
                cart.addMessage(redisProduct.getName() + "의 정보가 변경되었습니다. 확인 부탁드립니다.");
            }

            for(Cart.ProductItem item : items){
                Cart.ProductItem redisItem = redisItemMap.get(item.getId());
                if(redisItem == null){
                    redisProduct.getItems().add(item);
                } else {
                    if(redisItem.getPrice().equals(item.getPrice())){
                        cart.addMessage(redisProduct.getName() + item.getName() + "의 가격이 변경되었습니다. 확인 부탁드립니다.");
                    }
                    redisItem.setCount(redisItem.getCount() + item.getCount());
                }
            }
        } else {
            //존지하지 않는 경우
            Cart.Product product = Cart.Product.from(form);
            cart.getProducts().add(product);
        }

        customRedisClient.put(customerId, cart);
        return cart;
    }

}
