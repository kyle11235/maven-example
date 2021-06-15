
curl -X POST \
     -F token=$GITLAB_TOKEN \
     -F ref=master \
     https://gitlabdemo.online/api/v4/projects/135/trigger/pipeline