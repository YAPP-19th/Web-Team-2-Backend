pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh '''pwd
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

  }
}