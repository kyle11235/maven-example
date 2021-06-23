# maven example

- gitlab

        - gitlab
        .gitlab-ci.yml
        
        - art maven
        .gitlab-ci.yml .jfrog/maven.yaml

        - art jira
        .gitlab-ci.yml -> issue-system.yaml

        if issue id = 1
        git commit -m "1 - This is a sample issue"

- jenkins

        Jenkinsfile
        if issue id = BUG-1001
        git commit -m "BUG-1001"

- local

        setting.xml -> profiles
        pom.xml -> distributionManagement



        