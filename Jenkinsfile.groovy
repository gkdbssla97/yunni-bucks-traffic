pipeline {
    agent any
    tools {
    }
    stages {
        stage('build') {
            steps {
                sh '''
                    chmod 755 gradlew
                    ./gradlew clean compileQuerydsl build -PskipAsciidoctor
                    '''
            }
        }
        stage('deploy') {
            steps {
                deploy adapters: [tomcat9(credentialsId: 'deployer_user', path: '', url: 'http://3.39.230.26:8080')], contextPath: null, war: '**/*.war'
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