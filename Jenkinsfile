pipeline {
  agent any

  environment {
    SLACK_CHANNEL = 'team-web-2'
    SUCCESS_COLOR = '#00FF00'
    FAIL_COLOR = '#FF0000'
  }

  stages {
    stage('build') {
      steps {
        catchError {
          sh '''cp ../properties/application.properties ./src/main/resources
          ./gradlew build test'''
        }
      }
    }

    stage('docker push') {
      steps {
        catchError {
          sh '''docker image prune
'''
          sh 'docker build -t xodhkd36/yapp-server-test .'
          sh 'docker push xodhkd36/yapp-server-test'
        }
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

          post {
            success {
                slackSend (channel: SLACK_CHANNEL, color: SUCCESS_COLOR, message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
            }
            failure {
                slackSend (channel: SLACK_CHANNEL, color: FAIL_COLOR, message: "FAIL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
            }
          }

        }