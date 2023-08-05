package com.zerobase.cms.order.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    private final JwtAuthenticationProvider provider;
    @Value("sandboxb870dfb1fcc240d78758dd09a621e50d.mailgun.org")
    private String mailgunBaseURL;
    @Value("184e889ba53ee349afe0b6e68dd52ea6-c30053db-8b645d31")
    private String mailgunAPIKey;




    public JsonNode sendOrderListEmail(String token, Cart cart, int totalPrice) {
        /*
        메일에 필요한 내용물을 만드는 부분은 메일 전송 메서드 내에서 처리한다.

        내용 예시 : 결국 프로덕트 내의 아이템 별로 1줄씩 내용물을 만들어나가야 한다.
        토탈 가격은 sendOrderListEmail()이 호출되기에 앞서 계산된 숫자를 그대로 받아서 사용한다.

        결제 일시 : 2023-03-20
        상품명 : ~~ / 옵션명 : ~~ / 가격(수량 * 단가) : 20000 원( = 4 개 * 5000 원).
        ..
        총액 : 50000 원.
        */

        HttpResponse<JsonNode> request = null;
        try{
            String mailReceiver = provider.getUserVo(token).getEmail();
            StringBuilder mailContentBuilder = new StringBuilder();

            mailContentBuilder.append("결제 일시 : " + LocalDate.now() + "\n");
            for(Cart.Product cartProduct : cart.getProducts()){
                for(Cart.ProductItem cartProductItem : cartProduct.getItems()){
                    mailContentBuilder.append(
                        "상품명 : " + cartProduct.getName() + " / "
                            + "옵션명 : " + cartProductItem.getName() + " / "
                            + "가격(수량 X 단가) : " + (cartProductItem.getCount())*(cartProductItem.getPrice())
                            + " 원( = " + cartProductItem.getCount() + " 개 * " + cartProductItem.getPrice() + " 원)"
                            + "\n"
                    );
                }
            }
            mailContentBuilder.append("총액 : " + totalPrice + " 원");

            request = Unirest
                .post("https://api.mailgun.net/v3/" + mailgunBaseURL + "/messages")
                .basicAuth("api", mailgunAPIKey)
                .queryString("from", "mailgun@zerobase.com")
                .queryString("to", mailReceiver )
                .queryString("subject", "ZeroBase CMS 주문 결제 내역을 보내드립니다.")
                .queryString("text", mailContentBuilder.toString())
                .asJson();
            log.info("주문결제 내역 메일 전송 완료. 받는 이 아이디 : " + mailReceiver);
        } catch (Exception e){
            log.info("메일 전송 중 예외 발생. 예외 메시지 내용 : " + e.getMessage());
        }
        assert request != null;
        return request.getBody();
    }

}
