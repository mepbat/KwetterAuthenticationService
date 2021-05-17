pipeline {
/*   env.JAVA_HOME="${tool 'openjdk_11_0_1'}"
  env.PATH="${env.JAVA_HOME}/bin:${env.PATH}" */
  agent any
  tools {
    maven 'maven 3.6.3'
    jdk 'openjdk-11'
  }
  stages {
      stage('run test') {
        steps {
            sh 'mvn test'
        }
      }
      stage('SonarQube analysis') {
        steps {
            sh 'mvn clean package sonar:sonar -Dsonar.login=e56a75b4a25e9212c3cf4bf08c13805096b6b051'
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
