#!/bin/bash

sbt clean && sbt assembly

eval $(minikube docker-env)

VERSION=v0.0.1

docker build -t plelukas/super-soft-steam-master:$VERSION master
docker build -t plelukas/super-soft-steam-worker:$VERSION worker

echo "create configmaps"
kubectl apply -f k8s/super-soft-steam-config.yml --validate=false

echo "create cluster"
kubectl apply -f k8s/super-soft-steam-deployment.yml
