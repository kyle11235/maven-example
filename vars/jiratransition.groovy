def call(String jiraSite, String issueID) {    
    withEnv(['JIRA_SITE=' + jiraSite]) {
        def transitionInput =
        [
            transition: [
                id: '31'
            ]
        ]

        jiraTransitionIssue idOrKey: issueID, input: transitionInput
        }
}
