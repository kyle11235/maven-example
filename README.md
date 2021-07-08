# maven example

- prepare art repo

        - local
        libs-snapshot-local
        libs-release-local

        - virtual
        libs-snapshot
        libs-release

        - remote repo example

                - maven cental remote
                https://repo1.maven.org/maven2/

                - ali spring
                https://maven.aliyun.com/repository/spring

- maven local build

        - guide
        
                https://www.kdocs.cn/l/sQpfp1M74?f=501

        - install
        
                install maven & set maven home in /etc/profile

                install jdk & set java home in /etc/profile
                wget --no-cookies --no-check-certificate -H "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.tar.gz"

        - config

                resolve -> setting.xml -> profiles
                deploy -> pom.xml -> distributionManagement
                git commit -m "#BUG-1 comment, #BUG-2 comment, #BUG3 comment, #2 comment"

        - test

                mvn clean install
                mvn deploy

- jenkins maven project demo

        - guide
        
                https://www.jfrog.com/confluence/display/JFROG/Maven+Builds

        - test
        
                create maven project
                git = https://github.com/kyle11235/maven-example.git

                1. Build environment -> Resolve Artifacts from Artifactory
                Resolution releases repository = libs-release (virtual)
                Resolution snapshots repository = libs-snapshot (virtual)

                2. build
                path = pom.xml
                goal = clean install

                3. add Post build Actions -> deploy artifacts to artifactory
                Target releases repository = libs-release-local
                Target snapshot repository = libs-snapshot-local

- jenkins freestyle maven3 demo

        - guide
        
                https://www.jfrog.com/confluence/display/JFROG/Maven+Builds

        - test
        
                create freestyle maven project
                git = https://github.com/kyle11235/maven-example.git

                1. Build Environment -> enable Maven3-Artifactory Integration
                        
                        -> enable Resolve artifacts from Artifactory
                        Resolution releases repository = libs-release (virtual)
                        Resolution snapshots repository = libs-snapshot (virtual)
                
                        -> enable Capture and publish build info
                        Target releases repository = libs-release-local
                        Target snapshots repository = libs-snapshot-local

                2. add build step 'invoke art maven3'
                path = pom.xml
                goal = clean install

- jenkins basic maven pipeline demo

        - install maven

                https://archive.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip
                /u02/app/apache-maven-3.6.0
                sudo vi /etc/profile
                ...

        - config

                - global tools -> maven
                maven=/u02/app/apache-maven-3.6.0

                - system -> artifactory server
                art1=http://localhost:8081/artifactory

                ./jenkins/maven.groovy

- jenkins demo pipeline

        - jira

                - install jira pipeline plugin

                        api_token = jira -> personal account -> security -> create api token
                        jenkins plugin -> jira pipeline step
                        jenkins global setting -> jira sites -> add
                        url=https://kyle11235.atlassian.net
                        Basic
                        kylezhang@xxx.com/api_token

                - configure

                        - find jira issue type ID
                        1. In Jira, navigate to Project Settings > Issue Types
                        2. On that page, hover your mouse cursor over the Issue Type in the left menu

                - test
                
                        build
                        commit with comment #BUG-001, get commit id1
                        commit with comment #BUG-002, get commit id2
                        build
                        check properties of multi-3.7-xxx.pom / multi3-3.7-xxx.war
                        
                                - project.issues = BUG-001,BUG-002
                                - project.revisionIds = commit id1, commit id2
                                - JiraUrl = http:/jira.example.com/browse/HAP-121

                                - error
                                changeSet is empty

                                - fix
                                People often take changeSets for what they aren't. changeSet is the list of files that was modified between this build and the previous one. If the previous one failed and was re-triggered, changeSet would be empty.

                                - error
                                Scripts not permitted to use staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods

                                - fix
                                http://localhost:9001/scriptApproval/ -> approve the method signature

                                - error
                                Scripts not permitted to use method hudson.plugins.git.GitChangeSet getRevision

                                - fix, change to getCommitId
                                https://javadoc.jenkins.io/hudson/scm/ChangeLogSet.Entry.html#Entry--


        - sonarqube

                - guide
                
                        https://www.kdocs.cn/l/s7I9NbN6w?f=501

                - sonar scanner guide

                        https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/
                
                - install sonarqube server

                        docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest

                                - error on mac M1
                                no matching manifest for linux/arm64/v8

                                - fix
                                install on centos

                        http://x.x.x.x:9000/
                        default admin/admin
                        
                        configure jenkins webhook in sonarqube
                        http://x.x.x.x:8080/sonarqube-webhook (no / at the end)


                - install jenkins sonarqube plugin (name used in pipeline.groovy)

                        - add token for sonarqube server (server config cannot use username/password)
                        choose secret text
                        get token from sonarqube server -> my account -> generate token, ID=sonartoken

                        - add credential username/password, will be used for API call
                        choose username/password = admin/xxx, ID=sonar_username_password

                        - add sonarqube server, name=sonarqube
                        choose sonartoken

                        - add sonarqube scanner
                        check auto install or install on jenkins server, name=sonarscanner

                - install other jenkins plugins

                        add jdk, JAVA_HOME=/opt/jdk1.8.0_141/, name=java
                        add maven, MAVEN_HOME=/opt/apache-maven-3.6.0/
                        add http request plugin
                        use readJson in pipeline (pipeline-utility-steps)

                - test

                        - check
                        qa.code.quality.coverage
                        qa.code.quality.test_failures
                        qa.code.quality.skipped_tests
                        qa.code.quality.tests

                        - error
                        sonarqube webhook -> Response: Server Unreachable (sonar qube in docker?)
                        
                        - fix
                        remove '/' at the end of webhook url

        - postman

                - guide
                
                        https://www.kdocs.cn/l/sEDyIlbHh?f=501

                - install
                
                        login jenkins server, npm install -g newman
                
                - test
                
                        update password in jfrog_auto.postman_collection.json
                        upload json to jenkins server

        - ansible

                - guide
                
                        https://www.kdocs.cn/l/ssFuDIiGK?f=501

                - install

                        - install node1
                        yum install -y python
                        setenforce 0

                        - install ansible host
                        yum install epel-release -y
                        yum install ansible
                        
                        setenforce 0
                        or
                        vim /etc/selinux/config
                        SELINUX=disabled
                        sudo reboot

                        - copy from ansible_host -> node1(python)
                        ssh-keygen (all default enter)
                        ssh-copy-id -i root@39.101.199.34

                        vi /etc/ansible/hosts (ansible_host=node1 ip)
                        [testservers]
                        node1 ansible_host=39.101.199.34 ansible_ssh_user=root

                        ansible -m ping 'testservers'
                
                - test
                
                        mkdir -p /tmp/data/ansible_test
                        vi /tmp/data/ansible_test/test.txt
                        ##SSH_CONNECTION##

                        mkdir /tmp/data/ansible_playbook
                        vi /tmp/data/ansible_playbook/replace.yaml
                        check replace.yaml

                        cd /tmp/data/ansible_playbook/
                        ansible-playbook replace.yaml

- gitlab ci

        - gitlab
        .gitlab-ci.yml
        
        - art maven
        .gitlab-ci.yml .jfrog/maven.yaml

        - art jira
        .gitlab-ci.yml -> issue-system.yaml


        