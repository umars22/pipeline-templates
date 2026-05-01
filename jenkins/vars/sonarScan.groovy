#!/usr/bin/env groovy

def call(Map config = [:]) {
    def projectKey = config.projectKey ?: error("projectKey is required")
    def sources = config.sources ?: '.'
    def sonarHost = config.sonarHost ?: env.SONAR_HOST_URL
    def sonarToken = config.sonarToken ?: env.SONAR_AUTH_TOKEN
    def qualityGate = config.qualityGate != null ? config.qualityGate : true
    
    stage('SonarQube Scan') {
        withSonarQubeEnv('SonarQube') {
            sh """
                sonar-scanner \
                    -Dsonar.projectKey=${projectKey} \
                    -Dsonar.sources=${sources} \
                    -Dsonar.host.url=${sonarHost}
            """
        }
        
        if (qualityGate) {
            timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
        }
    }
}
