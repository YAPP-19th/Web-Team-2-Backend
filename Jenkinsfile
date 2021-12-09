pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh '''cp ../properties/application.properties ./src/main/resources
./gradlew build -x test'''
      }
    }

    stage('docker push') {
      steps {
        sh '''docker image prune
'''
        sh 'docker build -t xodhkd36/yapp-server-test .'
        sh 'docker push xodhkd36/yapp-server-test'
      }
    }
    stage('SSH transfer') {
      steps([$class: 'BapSshPromotionPublisherPlugin']) {
        sshPublisher(
          continueOnError: false, failOnError: true,
          publishers: [
            configName: "dotoriham",
            verbose: true,
            transfers: [
              sshTransfer(
                removePrefix: "",
                remoteDirectory: "",
                execCommand: "docker-compose up -d"
              )
            ]
          ]
        )
      }
    }
  }
}