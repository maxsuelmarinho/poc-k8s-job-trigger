package com.mmarinho.k8sjobtrigger;

public interface KubernetesApi {
    String CONTROLLER_UID_KEY = "controller-uid";

    void triggerJob(String namespace, String jobName) throws Exception;
}
