#!/usr/bin/env groovy

def call(Map config = [:]) {
    def namespace = config.namespace ?: 'default'
    def manifest = config.manifest ?: error("manifest is required")
    def kubeconfigId = config.kubeconfigId ?: error("kubeconfigId is required")
    def imageTag = config.imageTag ?: env.BUILD_NUMBER ?: 'latest'
    def wait = config.wait != null ? config.wait : true
    def timeout = config.timeout ?: '300s'
    
    stage('K8s Deploy') {
        withCredentials([kubeconfigFile(credentialsId: kubeconfigId, variable: 'KUBECONFIG')]) {
            sh """
                export KUBECONFIG=${KUBECONFIG}
                kubectl config current-context
                kubectl apply -f ${manifest} -n ${namespace}
            """
            
            if (wait) {
                sh """
                    export KUBECONFIG=${KUBECONFIG}
                    kubectl rollout status deployment/${config.deploymentName} -n ${namespace} --timeout=${timeout}
                """
            }
        }
    }
}
