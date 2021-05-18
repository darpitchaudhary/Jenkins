pipeline {
  environment {
    GH_TOKEN = ''
    GIT_BRANCH = 'main'
    
  }
  agent any
  stages {
    stage('Cloning Git Repo') {
      steps {
        git branch: 'main',credentialsId: 'GITHUB_TOKEN', url: "https://github.com/CSYE7125AdvanceCloud-Darpit/helmchart-app.git"
      }
    }
    stage('Helm lint') {
        steps {
            script {
                sh(script: "helm lint k8s-chart");
            }
            
        }            
    }
    stage('Release') {
        tools {
        nodejs "nodejs"
        }
        steps {
            script {
                sh(script: "npm install");
                sh(script: "npm install semantic-release-helm");
                sh(script: "npx semantic-release");
            }
            
        }            
    }
  }
}


