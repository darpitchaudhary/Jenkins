    pipeline {
        environment {

        GIT_REPO_NAME = "cronjobs"

        jobname1 = "cronjobnew"
        jobname2 = "cronjobbest"
        jobname3 = "cronjob"
        docker_repo_name = "darpitchaudhary"
        registry1 = "${docker_repo_name}/${jobname3}"
        registry2 = "${docker_repo_name}/${jobname1}"
        registry3 = "${docker_repo_name}/${jobname2}"

        registryCredential = 'docker_hub_user'   //dockerhub_cred
        registry_url = "https://registry-1.docker.io/"

        GIT_HASH = '';
        }
        agent any
        stages {
        stage('Cloning Git Repo') {
        steps {
    git branch: 'main',credentialsId: 'GITHUB_TOKEN', url: "https://github.com/CSYE7125AdvanceCloud-Darpit/${GIT_REPO_NAME}.git"
    }
    }
        stage('Maven Build Package') {
        steps {
        script {
        sh '''
      #!/bin/bash
        cd noon/
        mvn clean install -DskipTests=true
        cd ../noonbest/
        mvn clean install -DskipTests=true
        cd ../noonnew/
        mvn clean install -DskipTests=true

        '''
  }
  }
  }

    stage('Building Docker image') {
        steps{
        sh 'ls -al'
        script {
        sh '''
      #!/bin/bash
        cd noon/
        docker build . -t ${jobname3}
        cd ../noonbest/
        docker build . -t ${jobname2}
        cd ../noonnew/
        docker build . -t ${jobname1}
        '''

  }
  }
  }
    stage('Deploy Image') {
        steps{
        script {
    withCredentials([usernamePassword( credentialsId: 'docker_hub_user', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                                                                                                                         withDockerRegistry([ credentialsId: "docker_hub_user", url: "${registry_url}" ]){
                                                                                                                                                                                         GIT_HASH = sh(script: "git rev-parse HEAD", returnStdout: true).trim();
                                                                                                                                                                                         sh(script:"echo ${GIT_HASH}")
                                                                                                                                                                                         sh '''
                                                                                                                           #!/bin/bash

                                                                                                                                                                                         docker login -u $USER -p $PASSWORD
                                                                                                                                                                                         docker tag ${jobname3} ${registry1}:$(git rev-parse HEAD)
                                                                                                                                                                                         docker push ${registry1}:$(git rev-parse HEAD)
                                                                                                                                                                                         docker tag ${jobname2} ${registry3}:$(git rev-parse HEAD)
                                                                                                                                                                                         docker push ${registry3}:$(git rev-parse HEAD)
                                                                                                                                                                                         docker tag ${jobname1} ${registry2}:$(git rev-parse HEAD)
                                                                                                                                                                                         docker push ${registry2}:$(git rev-parse HEAD)

                                                                                                                                                                                         '''
    }

    }
    }
    }
    }
    }
    }
