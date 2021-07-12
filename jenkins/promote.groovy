// promotion path = libs-snapshot-local -> libs-release-local

// 1. promote by plugin script
def server = Artifactory.server 'art1'
def buildInfo = Artifactory.newBuildInfo()

// String parameters
buildInfo.name = buildName
buildInfo.number = buildNumber

def promotionConfig = [
    // Optional parameters
    'buildName'          : buildInfo.name,
    'buildNumber'        : buildInfo.number,
    'comment'            : 'this is the promotion comment', // -> Release History tab of build
    'status'             : 'Released', // Staged, Released, SIT, UAT...
    'sourceRepo'         : 'libs-snapshot-local',
    
    // Mandatory parameters
    'targetRepo'         : 'libs-release-local',
    
    // Optional parameters
    'includeDependencies': false, // False by default
    'copy'               : true, // Move is the default
    'failFast'           : true // False by default.
]

// Promote build
server.promote promotionConfig

// optional, enable interactive promote button to jenkins build
// Artifactory.addInteractivePromotion server: server, promotionConfig: promotionConfig, displayName: "Promote me please"

// 2. promote by rest API
// https://www.jfrog.com/confluence/display/JFROG/Artifactory+REST+API -> build promotion

// 3. promote by jfro cli
// https://www.jfrog.com/confluence/display/CLI/CLI+for+JFrog+Artifactory -> Promoting a Build
