kubectl delete deploy order
kubectl delete service order
mvn package -B
docker build -t 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/order:latest .
docker push 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/order:latest
