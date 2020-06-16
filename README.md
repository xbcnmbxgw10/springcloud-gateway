# Quick start

A enhanced enterprise-level project based on springcloud-gateway

## Building

```bash
mvn clean package -DskipTests -Dmaven.test.skip=true -T 2C -U -Pbuild:image -f springcloud-gateway
```

## Deploy on Docker

```bash
docker run -d \
  -e APP_PORT=8080 \
  -e JAVA_OPTS="-Xms4G -Xmx4G -XX:MaxDirectMemorySize=4G"\
  --name=gateway xbcnmbxgw10/springcloud-gateway:latest
```

## Deploy on Kubernetes

```bash
kubectl apply -f https://raw.githubusercontent.com/xbcnmbxgw10/springcloud-gateway/main/kubenetes-repo-example/deployement-all-in-one.yml
```
