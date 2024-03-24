pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                sh '''
                    chmod 755 gradlew
                    ./gradlew clean compileQuerydsl build -PskipAsciidoctor
                    '''
            }
        }

        stage('test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('deploy') {
            steps {
                deploy adapters: [tomcat9(credentialsId: 'deployer_user', path: '', url: 'http://52.78.34.39:8080/')], contextPath: null, war: '**/*.war'
            }
        }

        stage('restart tomcat') {
            steps {
                script {
                    sshagent (credentials: ['tomcat']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no ec2-user@172.31.41.40 '
                            TOMCAT_PID=$(ps -ef | grep tomcat | grep -v grep | awk '"'"'{print $2}'"'"')
                            if [[ -n $TOMCAT_PID ]]; then
                                echo "Tomcat is running with PID $TOMCAT_PID, stopping..."
                                sudo kill -15 $TOMCAT_PID
                                while ps -p $TOMCAT_PID > /dev/null; do sleep 1; done
                                echo "Tomcat stopped."
                            else
                                echo "Tomcat is not running."
                            fi
                            echo "Starting Tomcat..."
                            cd /opt/apache-tomcat-9.0.86/
                            sudo ./bin/startup.sh
                            echo "Tomcat started."
                        '
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "This will run when the run finished successfully"
            emailext body: 'Jenkins pipeline job for gradle build successfully completed.', subject: 'Build Success', to: 'hy97@sju.ac.kr'
        }
        failure {
            echo "This will run if failed"
            emailext body: 'Jenkins pipeline job for gradle build failed.', subject: 'Build Failure', to: 'hy97@sju.ac.kr'
        }
        changed {
            echo "This will run when the state of the pipeline has changed"
        }
    }
}
