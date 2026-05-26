pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'truongdocker1'
        DOCKER_CREDENTIALS_ID = 'dockerhub-creds'
        IMAGE_NAME = 'bookstore-notification-service'
        TAG = "${BUILD_NUMBER}"

        K8S_DEPLOYMENT = 'notification-service-deployment'
        K8S_CONTAINER = 'notification-service'
    }

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    dockerImage = docker.build(
                        "${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}",
                        "."
                    )
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry(
                        'https://index.docker.io/v1/',
                        "${DOCKER_CREDENTIALS_ID}"
                    ) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'db-creds', usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD'),
                    string(credentialsId: 'user-service-username', variable: 'USER_SERVICE_USERNAME'),
                    string(credentialsId: 'user-service-creds', variable: 'USER_SERVICE_PASSWORD'),
                    string(credentialsId: 'spring-mail-username', variable: 'SPRING_MAIL_USERNAME'),
                    string(credentialsId: 'spring-mail-password', variable: 'SPRING_MAIL_PASSWORD'),
                    string(credentialsId: 'notification-mail-from', variable: 'NOTIFICATION_MAIL_FROM'),
                    string(credentialsId: 'rabbitmq-password', variable: 'SPRING_RABBITMQ_PASSWORD')
                ]) {
                    sh '''
                export KUBECONFIG=/var/jenkins_home/.kube/config

                sed -i "s|image: .*${IMAGE_NAME}:.*|image: ${DOCKER_REGISTRY}/${IMAGE_NAME}:${TAG}|g" k8s/deployment.yaml

                kubectl apply -f k8s/configmap.yaml

                kubectl create secret generic notification-service-secret \
                  --from-literal=DB_USERNAME="$DB_USERNAME" \
                  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
                  --from-literal=SPRING_RABBITMQ_PASSWORD="$SPRING_RABBITMQ_PASSWORD" \
                  --from-literal=SPRING_MAIL_USERNAME="$SPRING_MAIL_USERNAME" \
                  --from-literal=SPRING_MAIL_PASSWORD="$SPRING_MAIL_PASSWORD" \
                  --from-literal=NOTIFICATION_MAIL_FROM="$NOTIFICATION_MAIL_FROM" \
                  --from-literal=USER_SERVICE_USERNAME="$USER_SERVICE_USERNAME" \
                  --from-literal=USER_SERVICE_PASSWORD="$USER_SERVICE_PASSWORD" \
                  --dry-run=client -o yaml | kubectl apply -f -

                kubectl apply -f k8s/deployment.yaml
                kubectl apply -f k8s/service.yaml

                kubectl rollout status deployment/${K8S_DEPLOYMENT} --timeout=180s
                '''
                }
            }
        }
    }

    post {
        success {
            echo "Build & Deploy SUCCESS"
        }
        failure {
            echo "Build FAILED"
        }
    }
}
