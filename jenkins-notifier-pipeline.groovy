  pipeline {
    environment {
    GH_TOKEN = ''
    REPO_NAME = "notifier-webapp"

    registry = "darpitchaudhary/${REPO_NAME}"

    registryCredential = 'docker_hub_user'   //dockerhub_cred
    registry_url = "https://registry-1.docker.io/"

    GIT_HASH = '';
    }
    agent any
    stages {

    stage('Cloning Git Repo') {
    steps {
  git branch: 'main',credentialsId: 'GITHUB_TOKEN', url: "https://github.com/CSYE7125AdvanceCloud-Darpit/${REPO_NAME}.git"
  }
  }
    stage('Maven Build Package') {
    steps {
    script {
  sh(script: "mvn clean install -DskipTests=true");
  }
  }
  }
    stage('Building Docker image') {
    steps{
    sh 'ls -al'
    script {
  GIT_HASH = sh(script: "git rev-parse HEAD", returnStdout: true).trim();
                                                sh(script: "docker build . -t ${REPO_NAME}");
  }
  }
  }
    stage('Deploy Image') {
    steps{
    script {
  withCredentials([usernamePassword( credentialsId: 'docker_hub_user', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                                                                                                                     withDockerRegistry([ credentialsId: "docker_hub_user", url: "${registry_url}" ]){
                                                                                                                                                                                   sh(script: "docker login -u $USER -p $PASSWORD")
                                                                                                                                                                                   sh(script: "docker tag ${REPO_NAME} ${registry}:${GIT_HASH}")
                                                                                                                                                                                   sh(script: "docker push ${registry}:${GIT_HASH}")
  }

  }
  }
  }
  }

    stage('Setting the variables values') {
    steps {

    script{
    sh '''
    #!/bin/bash
    rm -rf kafka*
    GITHUB_API_TOKEN=793328ddd54cfaaee740174d5754456b7d7bd1b7
    GH_API="https://api.github.com"
    GH_REPO="$GH_API/repos/CSYE7125AdvanceCloud-Darpit/notifier-webapp-helm-chart"
    GH_LATEST="$GH_REPO/releases/latest"
  AUTH="Authorization: token $GITHUB_API_TOKEN"
    response=$(curl -sH "$AUTH" $GH_LATEST)
    id=`echo "$response" | tr '\r\n' ' ' |jq '.assets[0] .id' |  tr -d '"'`
    name=`echo "$response" | tr '\r\n' ' ' | jq '.assets[0] .name' |  tr -d '"'`
    echo "$id"
    echo "$name"
    GH_ASSET="$GH_REPO/releases/assets/$id"
  curl -v -L -o "$name" -H "$AUTH" -H 'Accept: application/octet-stream' "$GH_ASSET"
    tar -xf "$name"
    '''
    }
    }
    }

    stage('Helm install') {
    steps {
  withCredentials([file(credentialsId: 'kube', variable: 'config')]) {
    sh '''
    imagename="${registry}:$(git rev-parse HEAD)"
    export KUBECONFIG=\${config}
    helm upgrade release notifier-chart --install --set imageCredentials.username="" --set imageCredentials.password="docker password" --set notifieralerts.container.image=$imagename --set postgresnotifier.config.data[0].key="host" --set postgresnotifier.config.data[0].value="rds-notifier.c8xrccb2wii3.us-east-1.rds.amazonaws.com" --set postgresnotifier.config.data[1].key="name" --set postgresnotifier.config.data[1].value="postgres" --set postgresnotifier.config.data[2].key="region" --set postgresnotifier.config.data[2].value="us-east-1" --set postgresnotifier.config.data[3].key="email" --set postgresnotifier.config.data[3].value="SES Email" --set postgresnotifier.config.data[4].key="eshost" --set postgresnotifier.config.data[4].value="eshost" --set postgresuser.config.data[0].key="host" --set postgresuser.config.data[0].value="rds-webapp.c8xrccb2wii3.us-east-1.rds.amazonaws.com" --set postgresuser.config.data[1].key="name" --set postgresuser.config.data[1].value="postgres"
    '''

  }

  }
  }


  }
  }