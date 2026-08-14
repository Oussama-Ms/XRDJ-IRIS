 pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_CMD = 'docker-compose' // or docker compose depending on the host setup
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Backend (Maven)') {
            steps {
                dir('backend') {
                    // Requires Maven installed on the Jenkins server or using a maven agent
                    sh 'mvn clean test'
                }
            }
        }

        stage('Build Frontend (Node)') {
            steps {
                dir('frontend') {
                    // Requires Node installed on the Jenkins server or using a node agent
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                // Use Docker Compose to build the images defined in the docker-compose.yml
                sh "${DOCKER_COMPOSE_CMD} build"
            }
        }

        stage('Deploy (Local)') {
            steps {
                // Bring down existing containers and bring up the newly built ones
                sh "${DOCKER_COMPOSE_CMD} down"
                sh "${DOCKER_COMPOSE_CMD} up -d"
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Deployment successful! Application is running on ports 80 and 8080.'
        }
        failure {
            echo 'Pipeline failed. Check the logs for more details.'
        }
    }
}
