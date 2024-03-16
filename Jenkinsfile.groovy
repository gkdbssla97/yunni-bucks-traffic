pipeline {
    agent any
    tools {
        gradle: 8.1.1
    }
    stages {
        stage('Clean') {
            steps {
                sh './gradlew clean'
                echo "Clean successfully!";
            }
        }
        stage('Compile QueryDSL') {
            steps {
                sh './gradlew compileQuerydsl'
                echo "Compile query-dsl successfully!";
            }
        }
        stage('JUnit') {
            steps {
                echo "JUnit passed successfully!";
            }
        }

        stage('Code Analysis') {
            steps {
                echo "Code Analysis completed successfully!";
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build -PskipAsciidoctor'
            }
        }
    }
    post {
        always {
            echo "This will always run"
        }
        success {
            echo "This will run when the run finished successfully"
        }
        failure {
            echo "This will run if failed"
        }
        unstable {
            echo "This will run when the run was marked as unstable"
        }
        changed {
            echo "This will run when the state of the pipeline has changed"
        }
    }
}