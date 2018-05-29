package com.sync.demo.syncdemo;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class FbProcessor {

    RestTemplate restTemplate = new RestTemplate();

    @KafkaListener(topics = "downloaded-ads", groupId = "downloaded-ads", containerFactory = "kafkaListenerContainerFactory")
    public void process(DownloadResponse message) {
        log.info("Start job {} with ids ", message.getJobId());
        if (Status.MISSED.equals(message.getStatus())) {
            log.warn("Got missed ad with id {}", message.getId());
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("username", "socialapp");
        httpHeaders.add("password", "s2aryd");

        ResponseEntity<AjaxBooleanResponse<Object>> responseEntity = restTemplate
                .exchange("http://Integrati-AppLoadB-7OP5J773Z3YL-812415291.us-east-1.elb.amazonaws.com/app-service/services/adgroup/test_saveorupdate?accountIdInTarget={accountIdInTarget}&jobId={jobId}",
                        HttpMethod.POST, new HttpEntity<>(message.getData(), httpHeaders),
                        new ParameterizedTypeReference<AjaxBooleanResponse<Object>>() {},
                        ImmutableMap.of("accountIdInTarget", "359773042", "jobId", 123L));

//        AjaxBooleanResponse<Object> response = WebClient.builder()
//                .baseUrl("http://Integrati-AppLoadB-7OP5J773Z3YL-812415291.us-east-1.elb.amazonaws.com/app-service/services/adgroup/test_saveorupdate")
//                .filter(logRequest()).build().post()
//                .uri(builder -> builder
//                        .queryParam("accountIdInTarget", "359773042")
//                        .queryParam("jobId", 123L)
//                        .build())
//                .header("username", "socialapp")
//                .header("password", "s2aryd")
//                .body(BodyInserters.fromObject(message.getData()))
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<AjaxBooleanResponse<Object>>() {})
//                .block();
        stopWatch.stop();
        if (responseEntity.getBody().isSuccess()) {
            log.info("Success, took : {} {}", stopWatch.getLastTaskTimeMillis(), responseEntity.getBody().getData());
        } else {
            log.error("Failed, took : {} {}",  stopWatch.getLastTaskTimeMillis(), responseEntity.getBody().getMessage());
        }

    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
//            clientRequest.headers()
//                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }


}
