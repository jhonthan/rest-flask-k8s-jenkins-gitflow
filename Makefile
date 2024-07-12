.DEFAULT_GOAL := create

create:
	@kind create cluster --config config.yaml

pre:
	@kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.3/config/manifests/metallb-native.yaml
	@kubectl wait --namespace metallb-system \
		--for=condition=ready pod\
		--selector=app=metallb \
		--timeout=90s
	@kubectl apply -f manifests/

helm:
	@helmfile apply

up: create pre helm

destroy:
	@kind delete clusters kind

passwd:
	@echo "Jenkins:"
	@kubectl get secret -n jenkins jenkins -ojson | jq -r '.data."jenkins-admin-password"' | base64 -d
	@echo ""

	@echo ""
	@echo "Gitea: "
	@echo "r8sA8CPHD9!bt6d"

	@echo ""
	@echo "Harbor: "
	@echo "Harbor12345"

	@echo ""
	@echo "Sonarqube: "
	@echo "r8sA8CPHD9!bt6d"

	@echo ""
	@echo "ArgoCD: "
	@kubectl get secret -n argo argocd-initial-admin-secret -ojson | jq -r '.data.password' | base64 -d
	@echo ""

	@echo ""
	@echo "Gitea-Jenkins: "
	@echo "UMZ^8%BNpUCjX9"