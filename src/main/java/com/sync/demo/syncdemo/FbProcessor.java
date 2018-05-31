package com.sync.demo.syncdemo;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class FbProcessor {

    RestTemplate restTemplate = new RestTemplate();
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    private KafkaTemplate<String, DownloadResponse> kafkaTemplate;

    @KafkaListener(topics = "downloaded-ads", groupId = "downloaded-ads", containerFactory = "kafkaListenerContainerFactory")
    public void process(DownloadResponse message) {
        log.info("Start job {} with ids ", message.getJobId());
        if (Status.MISSED.equals(message.getStatus())) {
            log.warn("Got missed ad with id {}", message.getId());
            kafkaTemplate.send("processed-ads", message);
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("username", "socialapp");
        httpHeaders.add("password", "s2aryd");

        ResponseEntity<AjaxBooleanResponse<Object>> responseEntity = restTemplate
                .exchange("http://Integrati-AppLoadB-MLQQY6K55BBK-1934710029.us-east-1.elb.amazonaws.com/app-service/services/adgroup/test_saveorupdate?accountIdInTarget={accountIdInTarget}&jobId={jobId}",
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
        log.info("Processed : {}", counter.incrementAndGet());
        if (responseEntity.getBody().isSuccess()) {
            log.info("Success, took : {} {}", stopWatch.getLastTaskTimeMillis(), responseEntity.getBody().getData());
        } else {
            log.error("Failed, took : {} {}",  stopWatch.getLastTaskTimeMillis(), responseEntity.getBody().getMessage());
        }
        kafkaTemplate.send("processed-ads", message);

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
