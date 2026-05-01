package org.sbpo

class PipelineUtils implements Serializable {
    def steps
    
    PipelineUtils(steps) {
        this.steps = steps
    }
    
    def checkoutRepo(String url, String branch = 'main', String credentialsId = '') {
        def checkoutConfig = [
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            extensions: [],
            userRemoteConfigs: [[url: url]]
        ]
        
        if (credentialsId) {
            checkoutConfig.userRemoteConfigs[0].credentialsId = credentialsId
        }
        
        steps.checkout(checkoutConfig)
    }
    
    def getVersion(String prefix = 'v') {
        def buildNum = steps.env.BUILD_NUMBER
        def gitCommit = steps.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        return "${prefix}${buildNum}-${gitCommit}"
    }
    
    def isMainBranch(String branchName) {
        return branchName in ['main', 'master']
    }
    
    def isPullRequest() {
        return steps.env.CHANGE_ID != null
    }
    
    def createTag(String tag, String message = '') {
        def msg = message ?: "Release ${tag}"
        steps.sh """
            git tag -a ${tag} -m "${msg}"
            git push origin ${tag}
        """
    }
    
    def runTests(String testCommand = 'npm test', String reportDir = 'reports') {
        steps.sh """
            mkdir -p ${reportDir}
            ${testCommand} || true
        """
        steps.junit "${reportDir}/*.xml"
    }
}
