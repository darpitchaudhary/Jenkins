  pipeline {
    environment {
    GH_TOKEN = ''
    REPO_NAME = "processor-webapp"

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
    GH_REPO="$GH_API/repos/CSYE7125AdvanceCloud-Darpit/processor-webapp-helm-chart"
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
    helm upgrade release kafka-processor-chart --install --set imageCredentials.username="darpitchaudhary" --set imageCredentials.password="docker pwd" --set appbeststories.container.image=$imagename --set postgresbeststories.config.data[0].key="host" --set postgresbeststories.config.data[0].value="rds-beststories.c8xrccb2wii3.us-east-1.rds.amazonaws.com" --set postgresbeststories.config.data[1].key="name" --set postgresbeststories.config.data[1].value="postgres" --set postgresbeststories.config.data[2].key="kafka_host" --set postgresbeststories.config.data[2].value="a1d446f13b7dc46d5883b09afdbf6904-936495959.us-east-1.elb.amazonaws.com" --set postgresbeststories.config.data[3].key="topic_name" --set postgresbeststories.config.data[3].value="beststories" --set postgresbeststories.config.data[4].key="topic_group" --set postgresbeststories.config.data[4].value="cg-beststories" --set postgresbeststories.config.data[5].key="eshost" --set postgresbeststories.config.data[5].value="aec38d7f564754971aee3afc3664dced-1969714884.us-east-1.elb.amazonaws.com" --set apptopstories.container.image=$imagename --set postgrestopstories.config.data[0].key="host" --set postgrestopstories.config.data[0].value="rds-topstories.c8xrccb2wii3.us-east-1.rds.amazonaws.com" --set postgrestopstories.config.data[1].key="name" --set postgrestopstories.config.data[1].value="postgres" --set postgrestopstories.config.data[2].key="kafka_host" --set postgrestopstories.config.data[2].value="a1d446f13b7dc46d5883b09afdbf6904-936495959.us-east-1.elb.amazonaws.com" --set postgrestopstories.config.data[3].key="topic_name" --set postgrestopstories.config.data[3].value="topstories" --set postgrestopstories.config.data[4].key="topic_group" --set postgrestopstories.config.data[4].value="cg-topstories" --set postgrestopstories.config.data[5].key="eshost" --set postgrestopstories.config.data[5].value="aec38d7f564754971aee3afc3664dced-1969714884.us-east-1.elb.amazonaws.com" --set appnewstories.container.image=$imagename --set postgresnewstories.config.data[0].key="host" --set postgresnewstories.config.data[0].value="rds-newstories.c8xrccb2wii3.us-east-1.rds.amazonaws.com" --set postgresnewstories.config.data[1].key="name" --set postgresnewstories.config.data[1].value="postgres" --set postgresnewstories.config.data[2].key="kafka_host" --set postgresnewstories.config.data[2].value="a1d446f13b7dc46d5883b09afdbf6904-936495959.us-east-1.elb.amazonaws.com" --set postgresnewstories.config.data[3].key="topic_name" --set postgresnewstories.config.data[3].value="newstories" --set postgresnewstories.config.data[4].key="topic_group" --set postgresnewstories.config.data[4].value="cg-newstories" --set postgresnewstories.config.data[5].key="eshost" --set postgresnewstories.config.data[5].value="aec38d7f564754971aee3afc3664dced-1969714884.us-east-1.elb.amazonaws.com" 
    '''

  }

  }
  }


  }
  }