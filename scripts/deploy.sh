#!/bin/bash

cd ..
echo "Deploying to Helios"
ssh -p 2222 s409203@se.ifmo.ru "rm -rf /Web/lab3/wildfly-preview-26.1.3.Final/standalone/deployments/lab3-beta-0.4.war"
scp -P 2222 ../target/lab3-beta-0.4.war s409203@se.ifmo.ru:Web/lab3/wildfly-preview-26.1.3.Final/standalone/deployments