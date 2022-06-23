cd ..
chmod +x ./mvnw
./mvnw clean package -DskipTests

cd docker

cp -r ../consumers/target/*.jar ./consumers/
cd ./consumers/
docker build -t gitlab-registry.dft.vn/smsgw/smsgw/consumer:latest -t gitlab-registry.dft.vn/smsgw/smsgw/consumer:$VERSION .
docker push gitlab-registry.dft.vn/smsgw/smsgw/consumer:latest
docker push gitlab-registry.dft.vn/smsgw/smsgw/consumer:$VERSION
cd ..

cp -r ../producers/target/*.jar ./producers/
cd ./producers/
docker build -t gitlab-registry.dft.vn/smsgw/smsgw/producer:latest -t gitlab-registry.dft.vn/smsgw/smsgw/producer:$VERSION .
docker push gitlab-registry.dft.vn/smsgw/smsgw/producer:latest
docker push gitlab-registry.dft.vn/smsgw/smsgw/producer:$VERSION
cd ..

cp -r ../dataServices/target/*.jar ./dataServices/
cd ./dataServices/
docker build -t gitlab-registry.dft.vn/smsgw/smsgw/dataservices:latest -t gitlab-registry.dft.vn/smsgw/smsgw/dataservices:$VERSION .
docker push gitlab-registry.dft.vn/smsgw/smsgw/dataservices:latest
docker push gitlab-registry.dft.vn/smsgw/smsgw/dataservices:$VERSION
cd ..

