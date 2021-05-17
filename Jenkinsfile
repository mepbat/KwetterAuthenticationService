pipeline {
  agent any
  tools {
    maven 'MAVEN_HOME'
  }
  stages {
      stage('run test') {
        steps {
            bat 'mvn test'
        }
      }
      stage('SonarQube analysis') {
        steps {
            bat 'mvn clean package sonar:sonar -Dsonar.login=e56a75b4a25e9212c3cf4bf08c13805096b6b051'
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
