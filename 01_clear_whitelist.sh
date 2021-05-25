

FROM=xray-project-maven-whitelist-local

curl -H 'X-JFrog-Art-Api: '"$ART_API_KEY"'' \
-X DELETE "http://182.92.214.141:8082/artifactory/$FROM"
