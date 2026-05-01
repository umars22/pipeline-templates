#!/usr/bin/env groovy

def call(Map config = [:]) {
    def terraformDir = config.terraformDir ?: '.'
    def workspace = config.workspace ?: 'default'
    def action = config.action ?: 'apply'
    def varsFile = config.varsFile ?: ''
    def autoApprove = config.autoApprove != null ? config.autoApprove : false
    def planFile = config.planFile ?: 'terraform.plan'
    
    dir(terraformDir) {
        stage('Terraform Init') {
            sh 'terraform init'
        }
        
        stage('Terraform Workspace') {
            sh """
                terraform workspace select ${workspace} || terraform workspace new ${workspace}
            """
        }
        
        stage('Terraform Plan') {
            def varsArg = varsFile ? "-var-file=${varsFile}" : ''
            sh """
                terraform plan ${varsArg} -out=${planFile}
            """
        }
        
        if (action == 'apply') {
            stage('Terraform Apply') {
                if (autoApprove) {
                    sh "terraform apply -auto-approve ${planFile}"
                } else {
                    input message: 'Approve Terraform Apply?', ok: 'Apply'
                    sh "terraform apply ${planFile}"
                }
            }
        }
        
        if (action == 'destroy') {
            stage('Terraform Destroy') {
                if (autoApprove) {
                    sh "terraform destroy -auto-approve"
                } else {
                    input message: 'Approve Terraform Destroy?', ok: 'Destroy'
                    sh "terraform destroy"
                }
            }
        }
    }
}
