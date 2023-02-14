package com.mmarinho.k8sjobtrigger;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This classes uses the official kubernetes-java client library
 */
@Slf4j
@Component("officialKubernetesApi")
@ConditionalOnProperty(name = "kubernetes.api", havingValue = "official")
public class OfficialKubernetesApi implements KubernetesApi {

    @Override
    public void triggerJob(String namespace, String jobName) throws Exception{
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        String pretty = "true";
        BatchV1Api batchApi = new BatchV1Api(client);
        V1Job job = batchApi.readNamespacedJob(jobName, namespace, pretty);
        log.info("Job {} found", job.getMetadata().getName());

        log.info("Job statuses: active: {}; failed: {}; ready: {}; succeeded: {}",
            job.getStatus().getActive(),
            job.getStatus().getFailed(),
            job.getStatus().getReady(),
            job.getStatus().getSucceeded());

        log.info("Removing job {}", jobName);
        String propagationPolicy = "Background";
        V1Status status = batchApi.deleteNamespacedJob(jobName, namespace, pretty, null, 0, null, propagationPolicy, null);
        log.info("Job {} removed. Status: {}", jobName, status);

        log.info("Recreating job {}", jobName);
        job.getSpec().getSelector().getMatchLabels().remove(CONTROLLER_UID_KEY);
        job.getSpec().getTemplate().getMetadata().getLabels().remove(CONTROLLER_UID_KEY);
        job.getMetadata().setResourceVersion(null);
        V1Job jobCreated = batchApi.createNamespacedJob(namespace, job, pretty, null, "job-trigger", null);
        log.info("job {} created", jobCreated.getMetadata().getName());
    }
}
