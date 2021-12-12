pipeline {
  agent any

  environment {
    SLACK_CHANNEL = 'team-web-2'
  }

  stages {
    stage('build') {
      steps {
        sh '''cp ../properties/application.properties ./src/main/resources
./gradlew build -x test'''
        slackSend (channel: SLACK_CHANNEL, color: '#FFFF00', message: "SUCCESS TEST: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        slackSend (channel: SLACK_CHANNEL, color: '#FF0000', message: "FAIL TEST: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
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