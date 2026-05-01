#!/usr/bin/env groovy

def call(Map config = [:]) {
    def imageName = config.imageName ?: error("imageName is required")
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context = config.context ?: '.'
    def registry = config.registry ?: ''
    def tag = config.tag ?: env.BUILD_NUMBER ?: 'latest'
    def push = config.push != null ? config.push : true
    
    def fullImageName = registry ? "${registry}/${imageName}:${tag}" : "${imageName}:${tag}"
    
    stage('Docker Build') {
        script {
            echo "Building Docker image: ${fullImageName}"
            sh """
                docker build -f ${dockerfile} -t ${fullImageName} ${context}
            """
            
            if (push && registry) {
                echo "Pushing to registry: ${registry}"
                docker.withRegistry("https://${registry}", config.registryCredentials) {
                    sh "docker push ${fullImageName}"
                }
            }
            
            return fullImageName
        }
    }
}
