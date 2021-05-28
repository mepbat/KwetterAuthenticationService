pipeline {
  agent any
  tools {
    maven 'maven 3.6.3'
    jdk 'jdk-11'
  }
  stages {
      stage('run test') {
        steps {
            sh 'mvn test'
        }
      }
      stage('SonarQube analysis') {
        steps {
            sh 'mvn clean package sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=e7159ea32d56c7374a869244c6d7b379afd62769'
        }
      }
      stage('Deployment') {
        when {
            branch 'master'
        }
        steps {
            echo 'This is a deployment'
        }
      }
  }
}
