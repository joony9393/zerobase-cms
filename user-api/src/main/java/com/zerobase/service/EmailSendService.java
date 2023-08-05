package com.zerobase.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.zerobase.client.SendMailForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    @Value("${mailgun.base.URL}")
    private String mailgunBaseURL;

    @Value("${mailgun.api.KEY}")
    private String mailgunAPIKey;




    public JsonNode sendEmail(SendMailForm sendMailForm) {
        HttpResponse<JsonNode> request = null;
        try{
            request = Unirest
                .post("https://api.mailgun.net/v3/" + mailgunBaseURL + "/messages")
                .basicAuth("api", mailgunAPIKey)
                .queryString("from", sendMailForm.getFrom())
                .queryString("to", sendMailForm.getTo())
                .queryString("subject", sendMailForm.getSubject())
                .queryString("text", sendMailForm.getText())
                .asJson();
            log.info("메일 전송 완료. 제목 : " + sendMailForm.getSubject());
        } catch (Exception e){
            log.info("메일 전송 중 예외 발생. 예외 메시지 내용 : " + e.getMessage());
        }
        assert request != null;
        return request.getBody();
    }

}
