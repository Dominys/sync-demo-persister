package com.sync.demo.syncdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class FbProcessor {

    @KafkaListener(topics = "downloaded-ads", groupId = "downloaded-ads", containerFactory = "kafkaListenerContainerFactory")
    public void process(DownloadResponse message) {
        log.info("Start job {} with ids ", message.getJobId());
        if (Status.MISSED.equals(message.getStatus())) {
            log.warn("Got missed ad with id {}", message.getId());
            return;
        }
        AjaxBooleanResponse<Object> response = WebClient.builder()
                .baseUrl("http://ec2-18-207-166-235.compute-1.amazonaws.com/app-service/services/adgroup/test_saveorupdate")
                .filter(logRequest()).build().post()
                .uri(builder -> builder
                        .queryParam("accountIdInTarget", "359773042")
                        .queryParam("jobId", 123L)
                        .build())
                .header("username", "socialapp")
                .header("password", "s2aryd")
                .body(BodyInserters.fromObject(message.getData()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AjaxBooleanResponse<Object>>() {})
                .block();
        if (response.isSuccess()) {
            log.info("Success {}", response.getData());
        } else {
            log.error("Failed {}", response.getMessage());
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
