#!/usr/bin/env groovy

def call(Map config = [:]) {
    def channel = config.channel ?: '#build-notifications'
    def message = config.message ?: "Build ${env.BUILD_NUMBER} - ${env.JOB_NAME}"
    def status = config.status ?: 'SUCCESS'
    def type = config.type ?: 'slack'
    
    def color = status == 'SUCCESS' ? 'good' : (status == 'FAILURE' ? 'danger' : 'warning')
    def emoji = status == 'SUCCESS' ? ':white_check_mark:' : (status == 'FAILURE' ? ':x:' : ':warning:')
    
    stage('Notify') {
        if (type == 'slack') {
            slackSend(
                channel: channel,
                color: color,
                message: "${emoji} ${message}\nJob: ${env.JOB_URL}\nBuild: ${env.BUILD_URL}"
            )
        } else if (type == 'email') {
            emailext(
                subject: "${status}: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """${message}
                
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Status: ${status}
""",
                to: config.recipients ?: env.CHANGE_AUTHOR_EMAIL
            )
        }
    }
}
