# Jenkins Pipeline Templates & Shared Library

This folder contains:
1. **Reusable Jenkins pipeline templates** for common project types
2. **Jenkins Shared Library** with reusable pipeline steps

---

## Part 1: Pipeline Templates

Ready-to-use Jenkinsfile templates for common deployment patterns.

### AWS Credential Usage

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

### Template Index

| Template | Description |
| --- | --- |
| `templates/backend-fastapi/Jenkinsfile` | Test + Dockerize + push to ECR + deploy FastAPI service on ECS/EC2. |
| `templates/frontend-react/Jenkinsfile` | Build React app, upload to S3, invalidate CloudFront. |
| `templates/expo-mobile/Jenkinsfile` | Build Expo/React Native app via EAS CLI and publish artifacts. |
| `templates/docker-microservice/Jenkinsfile` | Generic multi-environment Docker service with approval gates. |
| `templates/terraform/Jenkinsfile` | fmt/validate/plan/apply Terraform with remote state. |
| `templates/bluegreen-ecs/Jenkinsfile` | Blue-green deployment on ECS. |
| `templates/data-pipeline/Jenkinsfile` | Data pipeline deployment. |
| `templates/microfrontend-pnpm/Jenkinsfile` | Microfrontend build with pnpm. |
| `templates/mobile-ota/Jenkinsfile` | Mobile OTA updates. |
| `templates/monorepo-matrix/Jenkinsfile` | Monorepo builds with matrix strategy. |
| `templates/nightly-security/Jenkinsfile` | Nightly security scans. |
| `templates/serverless-lambda/Jenkinsfile` | Serverless Lambda deployment. |

---

## Part 2: Jenkins Shared Library

Reusable pipeline components for DevOps automation using the `@Library` annotation.

### Setup

Go to **Manage Jenkins → Configure System → Global Pipeline Libraries**

| Field | Value |
|-------|-------|
| Name | `sbpo-pipeline-library` |
| Default version | `main` (or your branch) |
| Retrieval method | Modern SCM |
| Source Code Management | Git |
| Project Repository | `https://github.com/umars22/pipeline-templates.git` |
| Credentials | (your Git credentials) |
| Library Path | `jenkins` |

### Usage

At the top of your Jenkinsfile:

```groovy
@Library('sbpo-pipeline-library') _
```

### Available Steps

#### `dockerBuildPush`

Build and push Docker images.

```groovy
dockerBuildPush(
    imageName: 'myapp',
    registry: 'registry.example.com',
    tag: env.BUILD_NUMBER,
    dockerfile: 'Dockerfile',
    context: '.',
    push: true,
    registryCredentials: 'docker-creds'
)
```

#### `terraformDeploy`

Run Terraform init, plan, and apply.

```groovy
terraformDeploy(
    terraformDir: './infra',
    workspace: 'production',
    action: 'apply',
    varsFile: 'production.tfvars',
    autoApprove: false
)
```

#### `k8sDeploy`

Deploy to Kubernetes.

```groovy
k8sDeploy(
    manifest: 'k8s/deployment.yaml',
    namespace: 'production',
    kubeconfigId: 'kubeconfig-credentials',
    imageTag: env.BUILD_NUMBER,
    deploymentName: 'myapp',
    wait: true,
    timeout: '300s'
)
```

#### `sonarScan`

Run SonarQube analysis.

```groovy
sonarScan(
    projectKey: 'my-project',
    sources: '.',
    qualityGate: true
)
```

#### `sendNotification`

Send Slack or email notifications.

```groovy
sendNotification(
    channel: '#deployments',
    message: "Deployment complete",
    status: 'SUCCESS',
    type: 'slack'
)
```

### Utility Classes

#### `PipelineUtils`

```groovy
import org.sbpo.PipelineUtils

def utils = new PipelineUtils(this)
def version = utils.getVersion('v')  // v42-abc1234
```

### Examples

See `jenkins/examples/`:
- `Jenkinsfile.simple` - Basic Docker build
- `Jenkinsfile.complete-example` - Full CI/CD pipeline

### Directory Structure

```
jenkins/
├── templates/              # Ready-to-use Jenkinsfile templates
├── vars/                   # Global pipeline steps (Shared Library)
│   ├── dockerBuildPush.groovy
│   ├── terraformDeploy.groovy
│   ├── k8sDeploy.groovy
│   ├── sonarScan.groovy
│   └── sendNotification.groovy
├── src/org/sbpo/          # Groovy classes
│   └── PipelineUtils.groovy
├── resources/             # External files
└── examples/              # Example Jenkinsfiles
```

### Adding New Shared Library Steps

1. Create a file in `jenkins/vars/<stepName>.groovy`
2. Start with: `#!/usr/bin/env groovy`
3. Define the `call` method:

```groovy
#!/usr/bin/env groovy

def call(Map config = [:]) {
    def required = config.requiredParam ?: error("requiredParam is required")
    stage('My Step') {
        echo "Running with ${required}"
    }
}
```
