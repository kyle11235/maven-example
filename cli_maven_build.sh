
BUILD_NAME=cli-maven-build
BUILD_NUMBER=12

jfrog rt mvn clean install --build-name=$BUILD_NAME --build-number=$BUILD_NUMBER
jfrog rt bce $BUILD_NAME $BUILD_NUMBER
jfrog rt bp $BUILD_NAME $BUILD_NUMBER

# build scan
jfrog rt bs $BUILD_NAME $BUILD_NUMBER --fail=false
