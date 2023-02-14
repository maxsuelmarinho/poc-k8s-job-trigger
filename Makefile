YELLOW := \033[1;33m
NC := \033[0m

K8S_COMMANDS := apply-job app-forward
.PHONY: $(K8S_COMMANDS) k8s-command
$(K8S_COMMANDS): k8s-command

apply-job: ARGS=apply --force -f ./kube/job.yaml

app-forward: ARGS=port-forward -n general svc/app-job-trigger 8080:8080

get:
	@echo "$(YELLOW)------------------"
	@echo "general namespace:"
	@echo "------------------$(NC)"
	@kubectl --kubeconfig=./kubeconfig.yaml get all -o wide -n general
	@echo "$(YELLOW)--------------------"
	@echo "dedicated namespace:"
	@echo "--------------------$(NC)"
	@kubectl --kubeconfig=./kubeconfig.yaml get all -o wide -n dedicated

k8s-command:
	@kubectl --kubeconfig=./kubeconfig.yaml $(ARGS)

docker-build-image:
	@docker build -t registry.local:5000/app-job-trigger:local .

docker-publish-image: docker-build-image
	@docker push registry.local:5000/app-job-trigger:local

apply-app:
	@kubectl --kubeconfig=./kubeconfig.yaml apply --force -f ./kube/app.yaml

release-app: docker-publish-image apply-app

up:
	@docker-compose up -d

down:
	@docker-compose down
	@docker volume rm --force $(shell docker volume ls | awk '{print $2}' | grep "k8s-job-trigger")

bootstrap: up apply-job release-app

trigger-job:
	@curl -i -X PUT localhost:8080/api/v1/jobs/dedicated/simple-job

health-check:
	@echo "$(YELLOW)liveness:$(NC)"
	@curl http://localhost:8080/actuator/health/liveness
	@echo
	@echo "$(YELLOW)readiness:$(NC)"
	@curl http://localhost:8080/actuator/health/readiness

job-logs:
	@kubectl --kubeconfig=./kubeconfig.yaml logs -f $(shell kubectl --kubeconfig=./kubeconfig.yaml get pods --selector=job-name=simple-job --output=jsonpath='{.items[*].metadata.name}' -n dedicated) -n dedicated

app-logs:
	@kubectl --kubeconfig=./kubeconfig.yaml logs -f $(shell kubectl --kubeconfig=./kubeconfig.yaml get pods --selector=app=app-job-trigger --output=jsonpath='{.items[*].metadata.name}' -n general) -n general
