kubectl delete deploy marketing
kubectl delete service marketing
mvn package -B
docker build -t 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/marketing:latest .
docker push 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/marketing:latest
