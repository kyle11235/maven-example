def call(String jiraSite, String issueID, String message) {    
    withEnv(['JIRA_SITE=' + jiraSite]) {
        def comment = [ body: message ]
        jiraAddComment idOrKey: issueID, input: comment
    }
}
