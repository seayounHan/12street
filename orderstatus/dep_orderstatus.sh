kubectl delete deploy orderstatus
kubectl delete service orderstatus
mvn package -B
docker build -t 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/orderstatus:latest .
docker push 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/orderstatus:latest
