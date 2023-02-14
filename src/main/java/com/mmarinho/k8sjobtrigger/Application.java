package com.mmarinho.k8sjobtrigger;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1/jobs")
@ControllerAdvice
@Slf4j
public class Application {

    @Autowired
    private KubernetesApi client;

	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

    @PutMapping("/{namespace}/{jobName}")
    void triggerJob(@PathVariable String namespace, @PathVariable String jobName) throws Exception {
        client.triggerJob(namespace, jobName);
    }

    @ResponseBody
    @ExceptionHandler(KubernetesClientException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String kubernetesClientException(KubernetesClientException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String kubernetesApiException(ApiException ex) {
        return "Exception when calling BatchV1Api#createNamespacedJob\nStatus code: %s\nReason: %s\nResponse headers: %s\n".formatted(
            ex.getCode(), ex.getResponseBody(), ex.getResponseHeaders());
    }
}
