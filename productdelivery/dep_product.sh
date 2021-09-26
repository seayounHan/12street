kubectl delete deploy productdelivery
kubectl delete service productdelivery
mvn package -B
docker build -t 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/productdelivery:latest .
docker push 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/productdelivery:latest
