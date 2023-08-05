package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.Product;
import com.zerobase.cms.order.domain.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartApplication {

    private final CartService cartService;
    private final ProductSearchService productSearchService;


    public Cart addCart(Long customerId, AddProductCartForm form){
        Product product = productSearchService.getByProductId(form.getId());
        if(product == null){
            throw new CustomException(ErrorCode.NOT_FOUND_PRODUCT);
        }

        Cart cart = cartService.getCart(customerId);

        if(cart != null && !addAble(cart, product, form)){
            throw new CustomException(ErrorCode.ITEM_COUNT_NOT_ENOUGH);
        }

        return cartService.addCart(customerId, form);
    }


    //장바구니에 있는 상품의 가격이나 수량이 변경된다면??
    //유저가 알림을 받던지, 장바구니에서 자동으로 빠져야 하지 않을까?
    //메시지를 보고난 다음에는 이미 본 메시지는 스팸이 되기 때문에 삭제한다.

    // CartApplicationTest 를 진행하면서 만난
    // failed to lazily initialize a collection of role could not initialize proxy - no session
    // 라는 에러를 해결하기 위해서 @Transactional 을 추가해주는 방법도 있다.
    // 그러나, 강의에서 등장한 방법을 우선적으로 적용한다.
    //@Transactional
    public Cart getCart(Long customerId){
        Cart cart = refreshCart( cartService.getCart(customerId) );
        cartService.putCart(cart.getCustomerId(), cart);

        Cart returnCart = new Cart();
        returnCart.setCustomerId(customerId);
        returnCart.setProducts(cart.getProducts());
        returnCart.setMessages(cart.getMessages());

        cart.setMessages(new ArrayList<>());

        //메시지가 삭제된 카트를 레디스에 넣는다.
        cartService.putCart(customerId,cart);
        return returnCart;
    }



    /*
     * 새로운 입력으로 들어온 카트를 리프레시 한 후 그것으로 카트를 통째로 덮어쓰면 된다.
     * getCart()내부에 refreshCart()가 있으므로, getCart()를 호출하면 끝난다.
     * */
    public Cart updateCart(Long customerId, Cart cart){
        cartService.putCart(customerId, cart);
        return getCart(customerId);
    }



    public void clearCart(Long customerId){
        cartService.putCart(customerId, null);
    }





    protected Cart refreshCart(Cart cart){
        //1. 상품이나 상품 아이템의 정보, 가격, 수량이 변경됐는지 체크
        //2. 그에 맞는 알림을 제공한다.
        //3. 상품의 수량, 가격을 우리가 임의로 변경한다.

        //장바구니에 있는 프로덕트들 중에서 실제로 찾아낼 수 있는 프로덕트만을 끄집어낸 다음, 그 프로덕트들을 아이디:프로덕트라는
        //키값 쌍 맵으로 만들어둔 것이다. 그러면 장바구니에서 실제로는 존재하지 않는 프로덕트들을 나중에 걸러낼 때 사용될 수 있다.
        Map<Long, Product> productMap = productSearchService.getListByProductIds(
            cart.getProducts().stream().map(Cart.Product::getId).collect(Collectors.toList())
        ).stream().collect(
            Collectors.toMap(Product::getId, product -> product)
        );

        for(int i=0; i < cart.getProducts().size(); i++){

            Cart.Product cartProduct = cart.getProducts().get(i);

            Product p = productMap.get(cartProduct.getId());
            if(p == null){
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품이 삭제되었습니다.");
                continue;
            }

            Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
                .collect(Collectors.toMap( ProductItem::getId, productItem -> productItem ));


            List<String> tmpMessages = new ArrayList<>();
            for(int j=0; j<cartProduct.getItems().size(); j++){
                Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
                ProductItem pi = productItemMap.get(cartProductItem.getId());

                if(pi == null){
                    cartProduct.getItems().remove(cartProductItem);
                    j--;
                    tmpMessages.add(cartProductItem.getName() + " 옵션이 삭제되었습니다.");
                    continue;
                }

                boolean isPriceChanged = false;
                boolean isCountNotEnough = false;

                if(!cartProductItem.getPrice().equals( pi.getPrice() )) {
                    isPriceChanged = true;
                    cartProductItem.setPrice(pi.getPrice());
                }
                if (cartProductItem.getCount() > pi.getCount()) {
                    isCountNotEnough = true;
                    cartProductItem.setCount(pi.getCount());
                }

                //가격 또는 수량이 바뀌었다면 카트 내용물도 바꿔야 한다.
                if(isPriceChanged && isCountNotEnough){
                    //message 1 :
                    tmpMessages.add(cartProductItem.getName() + " 가격변동, 수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
                } else if (isPriceChanged) {
                    //message 2
                    tmpMessages.add(cartProductItem.getName() + " 가격이 변동되었습니다.");
                } else if (isCountNotEnough) {
                    //message 3
                    tmpMessages.add(cartProductItem.getName() + " 수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
                }

            }//inner for

            if(cartProduct.getItems().size() == 0){
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품의 옵션이 모두 삭제되어 구매가 불가능합니다.");
            }
            else if (tmpMessages.size() > 0){
                StringBuilder builder = new StringBuilder();
                builder.append(cartProduct.getName() + "상품의 변동 사항 : ");
                for(String message : tmpMessages){
                    builder.append(message);
                    builder.append(", ");
                }//2nd inner for
                cart.addMessage(builder.toString());
            }
        }//Outer for
        return cart;
    }



    private boolean addAble(Cart cart, Product product, AddProductCartForm form){
        Cart.Product cartProduct = cart.getProducts().stream().filter(p -> p.getId().equals(form.getId()))
            .findFirst()//.orElseThrow( () -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT) );
            .orElse(
                Cart.Product.builder()
                    .id(product.getId())
                    .items(Collections.emptyList())
                    .build()
            );

        Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
            .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

        Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
                .collect( Collectors.toMap(ProductItem::getId, ProductItem::getCount ) );

        return form.getItems().stream().noneMatch(
            formItem -> {
                Integer cartCount = cartItemCountMap.get(formItem.getId());
                if(cartCount == null){
                    cartCount = 0;
                }
                Integer currentCount = currentItemCountMap.get(formItem.getId());
                if(currentCount == null) currentCount = 0;
                return formItem.getCount() + cartCount > currentCount;
            }
        );
    }
}
