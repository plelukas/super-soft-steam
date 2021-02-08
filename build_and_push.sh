#!/bin/bash

sbt clean && sbt assembly

VERSION=v0.0.1

docker build -t plelukas/super-soft-steam-master:$VERSION master
docker build -t plelukas/super-soft-steam-worker:$VERSION worker

docker push plelukas/super-soft-steam-master:$VERSION
docker push plelukas/super-soft-steam-worker:$VERSION
