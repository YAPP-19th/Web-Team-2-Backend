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
      steps {
        sshPublisher(failOnError: true, publishers: [
                    sshPublisherDesc(
                        configName: "dotoriham",
                        verbose: true,
                        transfers: [
                            sshTransfer(
                                sourceFiles: "run.zsh",
                                removePrefix: "",
                                remoteDirectory: "",
                                execCommand: "sh run.zsh"
                              )
                            ]
                          )
                        ])
              }
            }

          }
        }