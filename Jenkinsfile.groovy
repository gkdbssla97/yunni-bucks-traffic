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

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarQube-server') {
                    sh './gradlew sonarqube'
                }
            }
        }

        stage('deploy') {
            steps {
                deploy adapters: [tomcat9(credentialsId: 'deployer_user', path: '', url: 'http://13.209.72.244:8080/')], contextPath: null, war: '**/*.war'
                deploy adapters: [tomcat9(credentialsId: 'deployer_user', path: '', url: 'http://13.125.81.206:8080/')], contextPath: null, war: '**/*.war'
            }
        }

//        stage('restart tomcat') {
//            steps {
//                script {
//                    sshagent(credentials: ['tomcat']) {
//                        sh '''
//                            ssh ec2-user@172.31.41.40 '
//                            TOMCAT_PID=$(ps -ef | grep tomcat | grep -v grep | awk '"'"'{print $2}'"'"')
//                            if [[ -n $TOMCAT_PID ]]; then
//                                echo "Tomcat is running with PID $TOMCAT_PID, stopping..."
//                                sudo kill -15 $TOMCAT_PID
//                                while ps -p $TOMCAT_PID > /dev/null; do sleep 1; done
//                                echo "Tomcat stopped."
//                            else
//                                echo "Tomcat is not running."
//                            fi
//                            echo "Starting Tomcat..."
//                            cd /opt/apache-tomcat-9.0.86/
//                            sudo ./bin/startup.sh
//                            echo "Tomcat started."
//                        '
//                        '''
//                    }
//                }
//            }
//        }
    }

    post {
        success {
            script {
                emailext(
                        to: 'gkdbssla97@gmail.com',
                        subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                        body: """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
             <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
                )
            }
        }
        failure {
            echo "This will run if failed"
        }
        changed {
            echo "This will run when the state of the pipeline has changed"
        }
    }
}