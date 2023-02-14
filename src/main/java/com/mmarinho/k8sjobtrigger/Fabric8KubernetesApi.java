package com.mmarinho.k8sjobtrigger;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This classes uses the Fabric8 kubernetes client library
 */
@Slf4j
@Component("fabric8KubernetesApi")
@ConditionalOnProperty(name = "kubernetes.api", havingValue = "fabric8")
public class Fabric8KubernetesApi implements KubernetesApi {
    @Override
    public void triggerJob(String namespace, String jobName) throws Exception {
        try (final KubernetesClient client = new KubernetesClientBuilder().build()) {
            Job job = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).get();
            log.info("Job {} found", job.getMetadata().getName());

            log.info("Job statuses: active: {}; failed: {}; ready: {}; succeeded: {}",
                job.getStatus().getActive(),
                job.getStatus().getFailed(),
                job.getStatus().getReady(),
                job.getStatus().getSucceeded());

            job.getSpec().getSelector().getMatchLabels().remove(CONTROLLER_UID_KEY);
            job.getSpec().getTemplate().getMetadata().getLabels().remove(CONTROLLER_UID_KEY);

            log.info("Removing job {}", jobName);
            client.batch().v1().jobs()
                .inNamespace(namespace)
                .resource(job).delete();

            log.info("Job {} removed", jobName);

            log.info("Recreating job {}", jobName);
            Job jobCreated = client.batch().v1().jobs()
                .inNamespace(namespace)
                .resource(job)
                .createOrReplace();

            log.info("job {} created", jobCreated.getMetadata().getName());
        }
    }
}
