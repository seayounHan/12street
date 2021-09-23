kubectl delete deploy gateway
kubectl delete service gateway
mvn package -B
docker build -t 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/gateway:latest .
docker push 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/gateway:latest
kubectl create deploy gateway --image=879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/gateway:latest
kubectl expose deployment gateway --type=LoadBalancer --port=8080
