// jenkins shared library
def libraryPath = '/Users/kyle/workspace/microservice'
library identifier: 'local-lib@master', 
        retriever: modernSCM([$class: 'GitSCMSource', remote: "$libraryPath"]), 
        changelog: false

// jira
def JiraUrl = 'https://kyle11235.atlassian.net/browse/'
def JiraSite = 'myjira'
def JiraProjectID = '10001'

// sonarqube
def SonarQubeServer = 'sonarqube'  
def SonarScanner = 'sonarscanner'  
def TestResultPath = 'multi3/target/surefire-reports'  

// mock sonarqube
def sonarqube_scan_response = '/Users/kyle/workspace/maven-example/jenkins/sonarqube/sonarqube_scan_response.json'

// postman auto test script
def collection = '/Users/kyle/workspace/maven-example/jenkins/postman/jfrog_auto_localhost.postman_collection.json'

// show REST API to add property / rtMaven.deployer.addProperty
def artUrl = 'http://182.92.214.141:8081/artifactory'
def appPath = 'libs-snapshot-local/org/jfrog/test/multi3/4.7/multi3-4.7.war'

node('master') {  

	def server
	def buildInfo
	def rtMaven

    def art_apikey
    def passQualityGate
    def requirements

    // init plugin
    server = Artifactory.server('art1')
    rtMaven = Artifactory.newMavenBuild()
    
    buildInfo = Artifactory.newBuildInfo()
    buildInfo.env.capture = true // collect env variables

    rtMaven.tool = 'maven' // Tool name from Jenkins configuration

    // set maven repo
    // 1. resolve -> all set to virtual == settings.xml -> repositories (central + snapshot)
    rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server 

    // 2. deploy == pom.xml -> distributionManagement (central + snapshot)
    rtMaven.deployer releaseRepo: 'libs-snapshot-local', snapshotRepo: 'libs-snapshot-local', server: server

    // init api key
    withCredentials([string(credentialsId: 'art_apikey', variable: 'secret_text')]) {
        art_apikey = "${secret_text}"
    }

	stage('1. Git Clone') {
		git url: 'https://github.com/kyle11235/maven-example.git'

        // update version from 4.7-SNAPSHOT to 4.7
        // check version - https://www.jfrog.com/confluence/display/JFROG/Scripted+Pipeline+Syntax#ScriptedPipelineSyntax-MavenReleaseManagementwithArtifactory
		def descriptor = Artifactory.mavenDescriptor() 
		descriptor.version = '4.7'
		descriptor.pomFile = 'pom.xml'
		
		// 1. not work 
		//descriptor.failOnSnapshot = true
		
		// 2. works
		def snapshots = descriptor.hasSnapshots()
		echo "hasSnapshots=${snapshots}"
		if (snapshots) {
            // option1: throw error
            // error 'snapshots detected, make sure change to release version'
            
            // option2: update version by art plugin
            descriptor.transform()
            
            // option2: update version by sed command
            // sh "sed -i '_bak' 's/-SNAPSHOT//g' pom.xml **/pom.xml" // mac needs '_bak'
        }
	}

    // extract issues
    stage('2. Extract Issues') {
        
        // 1. get issues from git commit messages
        requirements = getRequirementsIds();
        echo "requirements=${requirements}" 

        // 2. get git revisions
        def revisionIds = getRevisionIds();
        echo "revisionIds=${revisionIds}"
        
        // 3. record issues by plugin
        rtMaven.deployer.addProperty("project.issues", requirements).addProperty("project.revisionIds", revisionIds)
        def values = requirements.split(',')
        for (int i = 0; i < values.size(); i++) {
            def issueID = values[i]
            def issueUrl = JiraUrl + issueID
            rtMaven.deployer.addProperty("Issue_" + issueID, issueUrl.toString())
        }
        

        // 3. record issues by curl with api key
        // sh "curl -H \"X-JFrog-Art-Api: ${art_apikey}\" -X PUT \"${artUrl}/api/storage/${appPath}?properties=project.issues=" + requirements + ";project.revisionIds=" + revisionIds + ";IssueUrl=http://example.com/browse/" + requirements + "\" "
    }

    // sonarqube
    // stage('3. Sonar Scan'){  
    //     // def scannerHome = tool SonarScanner
    //     withSonarQubeEnv(SonarQubeServer) {  
    //         sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${JOB_NAME} -Dsonar.sources=. -Dsonar.java.binaries=* -Dsonar.junit.reportPaths=${TestResultPath}"  
    //     }  
    
    //     // Just in case something goes wrong, pipeline will be killed after a timeout
    //     timeout(10) {  
    //             def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
    //             echo "sonarqube quality gate=" + qg.status
    //             // if (qg.status != 'OK') {
    //             //     error "Pipeline aborted due to quality gate failure: ${qg.status}"
    //             // }

    //     }

    //     // withSonarQubeEnv -> https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/
    //     // SONAR_CONFIG_NAME, SONAR_HOST_URL, SONAR_AUTH_TOKEN
    //     withSonarQubeEnv(SonarQubeServer) { 
    //         // authentication (just a username/password credential, not the one configured for sonarqube server)
    //         def surl="${SONAR_HOST_URL}/api/measures/component?component=${JOB_NAME}&metricKeys=alert_status,quality_gate_details,coverage,new_coverage,bugs,new_bugs,reliability_rating,vulnerabilities,new_vulnerabilities,security_rating,sqale_rating,sqale_index,sqale_debt_ratio,new_sqale_debt_ratio,duplicated_lines_density&additionalFields=metrics,periods"
    //         def response = httpRequest authentication: "sonar_username_password", consoleLogResponseBody: true, contentType: 'APPLICATION_JSON', ignoreSslErrors: true, url: surl
            
    //         echo "status=" + response.status  
    //         echo "content=" + response.content  

    //         def json = readJSON text: response.content  
    //         if (json.component.measures) {  
    //             json.component.measures.each{ item ->  
    //             if (item.periods && item.periods[0].value) {  
    //                 name = "qa.code.quality." + item.metric  
    //                 value = item.periods[0].value  
    //                 rtMaven.deployer.addProperty(name, value)  
    //             } else if (item.value) {  
    //                 name = "qa.code.quality." + item.metric  
    //                 value = item.value  
    //                 rtMaven.deployer.addProperty(name, value)  
    //             }  
    //             }  
    //         }  
    //     }  
    // }  

    // mock sonaqube
    stage('3. Sonar Scan'){  

        sh "cp ${sonarqube_scan_response} sonarqube_scan_response.json"  

        def json = readJSON file: "sonarqube_scan_response.json"
        if (json.component.measures) {  
            json.component.measures.each{ item ->  
            if (item.periods && item.periods[0].value) {  
                name = "qa.code.quality." + item.metric  
                value = item.periods[0].value  
                rtMaven.deployer.addProperty(name, value)  
            } else if (item.value) {  
                name = "qa.code.quality." + item.metric  
                value = item.value  
                rtMaven.deployer.addProperty(name, value)  
            }  
            }  
        }  
    }  


    // build, by default the plugin also deploy artifact to maven repo
    stage ('4. Maven Install Deploy') {
        rtMaven.run pom: 'pom.xml', goals: 'clean install', buildInfo: buildInfo

        // publish build info
        server.publishBuildInfo buildInfo
    }
    
    // jira
    stage('5. Link Jira Artifact'){
        echo 'requirements=' + requirements
        if (requirements?.trim()){
            // query artifactory
            commandText = "curl -H \"X-JFrog-Art-Api: ${art_apikey}\" -H \"Content-type:application/json\"  -X POST --data '{\"buildName\": \"${JOB_NAME}\",\"buildNumber\":\"${BUILD_NUMBER}\"}'  ${artUrl}/api/search/buildArtifacts"
            echo commandText
            downloadurl = [ 'bash', '-c', commandText].execute().text

            def values = requirements.split(',')
            for (int i = 0; i < values.size(); i++) {
                def issueID = values[i]
                // add jira comment
                jiracommentadd(JiraSite, issueID, 'build ok' + downloadurl)
            
                // change to in progress
                jiratransition(JiraSite, issueID)
            }
        }
        
    }


    // xray scan build 
    // https://www.jfrog.com/confluence/display/JFROG/CI-CD+Integration+with+Xray
    // https://www.jfrog.com/confluence/display/JFROG/Scripted+Pipeline+Syntax
    stage('6. Xray Scan'){

        // 1. ensure xray's watch's policy's rule's failBuild is checked
        // 2. to not fail a build, set below failBuild to false
		def xrayConfig = [
          'buildName'     : env.JOB_NAME,
          'buildNumber'   : env.BUILD_NUMBER,
          'failBuild'     : false
        ]

        def xrayResults = server.xrayScan xrayConfig

        // if echo Build maven-pipeline is not selected for indexing -> add build into indexing
        echo xrayResults as String

        // by JsonSlurper
        // def jsonSlurper = new JsonSlurper()
    	// def xrayjson = jsonSlurper.parseText(xrayResults.toString())      

        // by readJSON  
        def xrayjson = readJSON text: xrayResults.toString()  
        
    	echo "Xray scan total alerts=" + xrayjson.summary.total_alerts.toString()

        // use artifactory REST API, record as Meta data/Property      
        httpRequest authentication: 'art_username_password',  
        consoleLogResponseBody: true,  
        httpMode: 'PUT',  
        ignoreSslErrors: true,  
        responseHandle: 'NONE',  
        url: "${artUrl}/api/storage/${appPath}?properties=xray.status=scanned;xray.alerts=${xrayjson.summary.total_alerts}"  

        // create jira issue
        def summary = "${JOB_NAME}" + "_" + "${BUILD_NUMBER}" + "_security issue"
        xraycount = "Security:" + xrayjson.summary.total_alerts
        def desc = "issues count = " + xraycount + ", issues details = " + xrayjson.summary.more_details_url
        newjiraissue(JiraSite, JiraProjectID, summary, desc)

	}

    // quality gate
    stage('7. Check Quality Gate') {  
      
        // query artifact by AQL
        def aql = '''items.find({  
             "@build.name": {"$eq" : "''' + buildInfo.name + '''"},  
             "@build.number": {"$eq" : "''' + buildInfo.number + '''"},
             "@qa.code.quality.alert_status": {"$eq" : "OK"},
             "@xray.status": {"$eq" : "scanned"}
             })  
           '''  

        // "@xray.alerts": {"$lte" : "5"} // string so 23 < 5?
        // "@qa.test.api.success": {"$gte" : "''' + '0.5' + '''"}  
        echo aql

        def response = httpRequest authentication: 'art_username_password',  
        consoleLogResponseBody: true,  
        contentType: 'TEXT_PLAIN',  
        httpMode: 'POST',  
        ignoreSslErrors: true,  
        requestBody: aql,  
        url: "${artUrl}/api/search/aql"  
      
        echo "status=" + response.status 
        echo "content=" + response.content
        def props = readJSON text: response.content  
      
        // good or bad
        if (props.range.total > 0) {  
            passQualityGate = true
        } else {
            passQualityGate = false
            // throw error
            // error 'Did not pass the quality gate!!!'  // error = quit
        }
        echo "passQualityGate=" + passQualityGate
    }

    // promote
    stage('8. Promote') {  
        if (passQualityGate) {  
            
            // promotion path = libs-snapshot-local -> libs-stage-local -> libs-release-local -> libs-prod-local
            def promotionConfig = [
                // Optional parameters
                'buildName'          : buildInfo.name,
                'buildNumber'        : buildInfo.number,
                'comment'            : 'this is the promotion comment', // -> Release History tab of build
                'status'             : 'Staged', // Staged, Released, SIT, UAT...
                'sourceRepo'         : 'libs-snapshot-local',
                
                // Mandatory parameters
                'targetRepo'         : 'libs-stage-local',
                
                // Optional parameters
                'includeDependencies': false, // False by default
                'copy'               : true, // Move is the default
                'failFast'           : true // False by default.
            ]

            // promote it
            server.promote promotionConfig
        } 
    }   

    // deploy
    stage('9. Auto Deploy') {
        if (passQualityGate) {  
            // 1. download war file
            def FILENAME = "/opt/tomcat/target/app.war"
            sh "curl -H \"X-JFrog-Art-Api: ${art_apikey}\" -X GET \"${artUrl}/libs-stage-local/org/jfrog/test/multi3/[RELEASE]/multi3-[RELEASE].war\" -o $FILENAME"

            // 2. start tomcat
            sh "/opt/tomcat/start_tomcat.sh"    
        }
    }

    // postman
    stage("10. Auto API Test") {  
        if (passQualityGate) {  
            try {  
                sh "newman run ${collection} -r cli,json --reporter-json-export report.json"  
            } catch(e) {  
                echo e.toString()  
            }  
            
            rest = fileExists 'report.json'  
            if (rest) {  
                def json = readJSON file: "report.json"
                
                testsTotal = json.run.stats.assertions.total
                testsFailed = json.run.stats.assertions.failed
                testsSuccess = ((testsTotal - testsFailed)/testsTotal) * 100
    
                echo "qa.test.api.total=${testsTotal}"
                echo "qa.test.api.failed=${testsFailed}"  
    
                // use artifactory REST API, record as Meta data/Property   
                httpRequest authentication: 'art_username_password',  
                consoleLogResponseBody: true,  
                httpMode: 'PUT',  
                ignoreSslErrors: true,  
                responseHandle: 'NONE',  
                url: "${artUrl}/api/storage/${appPath}?properties=qa.test.api.total=${testsTotal};qa.test.api.failed=${testsFailed};qa.test.api.success=${testsSuccess}&recursive=1"  
            }  
        }
    }

    // notify
    stage('11. Notify Tester') {
        
    }



}


@NonCPS
def getRequirementsIds() {
    def ids = "";
    
    // jenkins changeSets - https://ci.jenkins.io/pipeline-syntax/globals

    final changeSets = currentBuild.changeSets
    echo 'changeSets size=' + changeSets.size().toString()
    final changeSetIterator = changeSets.iterator()
    while (changeSetIterator.hasNext()) {
        final changeSet = changeSetIterator.next();
        def logEntryIterator = changeSet.iterator();
        while (logEntryIterator.hasNext()) {
            final logEntry = logEntryIterator.next()
            
            // git commit -m "#BUG-1 comment, #BUG-2 comment, #BUG3 comment, #2 comment"
            def patten = ~/#[\w-\d|\d]+/;
            
            def matcher = (logEntry.getMsg() =~ patten);
            def count = matcher.getCount();
            for (int i = 0; i < count; i++) {
                ids += matcher[i].replace('#', '') + ","
            }
        }
    }
    return ids;
}
@NonCPS
def getRevisionIds() {
    def ids = "";
    final changeSets = currentBuild.changeSets
    final changeSetIterator = changeSets.iterator()
    while (changeSetIterator.hasNext()) {
        final changeSet = changeSetIterator.next();
        def logEntryIterator = changeSet.iterator();
        while (logEntryIterator.hasNext()) {
            final logEntry = logEntryIterator.next()
            ids += logEntry.getCommitId() + "," // not getRevision, should be getCommitId
        }
    }
    return ids
}


@NonCPS
def printlog() {
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                echo "  ${file.editType.name} ${file.path}"
            }
        }
    }
    return ""
}