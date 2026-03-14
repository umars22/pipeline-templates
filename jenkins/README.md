# Jenkins Pipeline Templates

This folder contains reusable Jenkins pipeline definitions for common project types. Each template:

- Uses **Declarative Pipeline** syntax so it can be dropped into any Jenkins job.
- Assumes AWS credentials are stored in Jenkins Credentials as `aws-cli-creds` (username = access key, password = secret key).
- Supports multi-environment promotion (dev → stage → prod) with manual gates where appropriate.
- Uses Docker agents so pipelines remain portable. Switch the agent labels or container images to match your Jenkins setup.

## AWS Credential Usage

All templates wrap deployment stages in:

```groovy
withCredentials([
  usernamePassword(credentialsId: 'aws-cli-creds', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')
]) {
  sh 'aws sts get-caller-identity'
  // deployment commands
}
```

Rename `aws-cli-creds` to whatever ID you use in Jenkins.

## Template Index

| Template | Description |
| --- | --- |
| `templates/backend-fastapi/Jenkinsfile` | Test + Dockerize + push to ECR + deploy FastAPI service on ECS/EC2. |
| `templates/frontend-react/Jenkinsfile` | Build React app, upload to S3, invalidate CloudFront. |
| `templates/expo-mobile/Jenkinsfile` | Build Expo/React Native app via EAS CLI and publish artifacts. |
| `templates/docker-microservice/Jenkinsfile` | Generic multi-environment Docker service with approval gates. |
| `templates/terraform/Jenkinsfile` | fmt/validate/plan/apply Terraform with remote state. |

Copy the Jenkinsfile you need, adjust environment variables, and you’re ready to go.
