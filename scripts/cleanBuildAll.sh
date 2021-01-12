#!/bin/bash
# Script for cleaning and building Muisti-API project.
# Run this in the "scripts" folder of muisti-api

clear
echo "Console cleared"
echo "Stopping old docker containers..."
for i in `docker ps -qa`; do docker stop $i; done;
echo "Containers stopped"

cd ../api-spec/
echo "Cleaning api-spec folder"
rm -fR src build
echo "Starting clean build for api-spec"
../gradlew clean build
cd ..

cd api-client/
echo "Cleaning api-client folder"
rm -fR src build
echo "Starting clean build for api-client"
../gradlew clean build
cd ..

echo "Starting clean build of whole project"
./gradlew clean build

echo "Build done!"
