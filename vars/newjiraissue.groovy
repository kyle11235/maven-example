def call(String jiraSite, String jiraProjectId, String summary, String desc) {    
    withEnv(['JIRA_SITE=' + jiraSite]) {
        def newIssue = [fields: [ project: [id: jiraProjectId],
                                    summary: summary,
                                    description: desc,
                                    issuetype: [id: '10005']]]

        response = jiraNewIssue issue: newIssue

        echo response.successful.toString()
        echo response.data.toString()
    }
    
}
