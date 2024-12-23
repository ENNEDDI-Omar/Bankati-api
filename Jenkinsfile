pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        APP_NAME = 'e-bankati'
        DOCKER_IMAGE = 'banking-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_TOKEN = credentials('sonar-token')
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Build & Test') {
            stages {
                stage('Compile') {
                    steps {
                        sh 'mvn clean compile -DskipTests'
                    }
                }

                stage('Unit Tests') {
                    steps {
                        sh 'mvn test -Dspring.profiles.active=test'
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
                        failure {
                            emailext(
                                subject: "❌ Tests Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                                body: "Les tests unitaires ont échoué. Voir: ${env.BUILD_URL}",
                                to: 'enneddiomar@gmail.com'
                            )
                        }
                    }
                }

                stage('Package') {
                    steps {
                        sh 'mvn package -DskipTests'
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
                }
            }
        }

        stage('Quality Analysis') {
            parallel {
                stage('SonarQube') {
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            sh '''
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=${APP_NAME} \
                                    -Dsonar.projectName="E-Bankati System" \
                                    -Dsonar.host.url=http://sonarqube:9000 \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                            '''
                        }
                        timeout(time: 2, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                }

                stage('Dependency Check') {
                    steps {
                        sh 'mvn dependency-check:check'
                    }
                    post {
                        always {
                            dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                        }
                    }
                }
            }
        }

        stage('Docker Build & Push') {
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
                            input message: '⚠️ Déployer en production ?', ok: 'Approuver'
                        }
                    }
                }

                stage('Deploy') {
                    steps {
                        script {
                            sh """
                                docker stop ${DOCKER_IMAGE} || true
                                docker rm ${DOCKER_IMAGE} || true
                                docker run -d \
                                    --name ${DOCKER_IMAGE} \
                                    -p 8081:8081 \
                                    -e SPRING_PROFILES_ACTIVE=prod \
                                    -e DB_Username=\${DB_USERNAME} \
                                    -e DB_Password=\${DB_PASSWORD} \
                                    ${DOCKER_IMAGE}:${DOCKER_TAG}
                            """
                        }
                    }
                    post {
                        success {
                            echo "🚀 Application déployée avec succès!"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            emailext(
                subject: "✅ Pipeline Réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline s'est terminé avec succès!

                    Détails:
                    - Job: ${env.JOB_NAME}
                    - Build: ${env.BUILD_NUMBER}
                    - URL: ${env.BUILD_URL}

                    Image Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}
                """,
                to: 'enneddiomar@gmail.com',
                attachLog: true
            )
        }
        failure {
            emailext(
                subject: "❌ Pipeline Échoué - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline a échoué.

                    Détails:
                    - Job: ${env.JOB_NAME}
                    - Build: ${env.BUILD_NUMBER}
                    - URL: ${env.BUILD_URL}

                    Veuillez vérifier les logs pour plus d'informations.
                """,
                to: 'enneddiomar@gmail.com',
                attachLog: true
            )
        }
        always {
            cleanWs()
        }
    }
}