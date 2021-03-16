
build_number=2
jfrog rt mvn clean install --build-name=maven-cli-build --build-number=$build_number
jfrog rt bce maven-cli-build $build_number
jfrog rt bp maven-cli-build $build_number