pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKER_IMAGE = 'banking-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_TOKEN = credentials('sonar-token')
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Environment Check') {
            steps {
                sh '''
                    java -version
                    mvn --version
                    docker version
                '''
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh '''
                    mvn clean test \
                        -Dspring.profiles.active=test \
                        -B -V
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/*.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${DOCKER_IMAGE} \
                            -Dsonar.projectName=BankingSystem \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    def dockerImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Deploy to Production') {
            stages {
                stage('Approval') {
                    steps {
                        timeout(time: 5, unit: 'MINUTES') {
                            input message: 'Deploy to production?'
                        }
                    }
                }
                stage('Deploy') {
                    steps {
                        sh '''
                            docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker stop ${DOCKER_IMAGE} || true
                            docker rm ${DOCKER_IMAGE} || true
                            docker run -d --name ${DOCKER_IMAGE} \
                                -p 8081:8081 \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                ${DOCKER_IMAGE}:${DOCKER_TAG}
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            emailext (
                subject: "✅ Pipeline Success - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline s'est terminé avec succès !

                    Build URL: ${env.BUILD_URL}
                    Project: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                """,
                to: 'enneddiomar@gmail.com'
            )
        }
        failure {
            emailext (
                subject: "❌ Pipeline Failed - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline a échoué. Veuillez vérifier les logs.

                    Build URL: ${env.BUILD_URL}
                    Project: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                """,
                to: 'enneddiomar@gmail.com'
            )
        }
        always {
            cleanWs()
        }
    }
}