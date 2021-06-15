# maven example

- gitlab

        https://gitlabdemo.online/users/sign_in
        https://jfrog.com/blog/gitlab-and-artifactory-on-your-mark-get-set-build/

        - gitlab
        .gitlab-ci.yml
        
        - art maven
        .gitlab-ci.yml -> .jfrog/projects/maven.yaml

        - art jira
        .gitlab-ci.yml -> jira-cli.conf
        commit JIR-1001
        commit JIR-1002

- jenkins

        Jenkinsfile

- local

        setting.xml -> profiles
        pom.xml -> distributionManagement

- test jira

        git revert HEAD~1
        #BUG-1005

        