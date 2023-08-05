package com.zerobase.cms.order.config;

import com.zerobase.domain.config.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    //JwtAuthenticationProvider 의 경우 user-api 모듈입장에서 봤을 때 다른 모듈에서 끌고온 모듈이기 때문에
    //user-api 내에서는 빈이 자동으로 생성되지 않는다. 따라서 이를 해결하기 위해서 이러한 컨피규레이션 클래스를 작성한다.

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(){
        return new JwtAuthenticationProvider();
    }

}
