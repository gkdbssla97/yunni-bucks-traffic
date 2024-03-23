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
                deploy adapters: [tomcat9(credentialsId: 'deployer_user', path: '', url: 'http://13.125.254.101:8080/')], contextPath: null, war: '**/*.war'
            }
        }

        stage('restart tomcat') {
            steps {
                script {
                    sshagent (credentials: ['tomcat']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no ec2-user@13.125.254.101 '
                                TOMCAT_PID=$(ps -ef | grep tomcat | grep -v grep | awk "{print \$2}")
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

        stage('notification') {
            steps {
                emailext body: 'Jenkins pipeline job for gradle build job completed', subject: 'Job Completed', to: 'hy97@sju.ac.kr'
            }
        }
    }

    post {
        success {
            echo "This will run when the run finished successfully"
        }
        failure {
            echo "This will run if failed"
        }
        changed {
            echo "This will run when the state of the pipeline has changed"
        }
    }
}
