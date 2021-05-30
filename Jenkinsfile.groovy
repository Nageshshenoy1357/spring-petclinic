pipeline {
    agent none

    options {
        timestamps()
        skipDefaultCheckout()
    }   


    stages {

        stage('Starting CI-CD on Master') {
            agent {
                label 'master'
            }
            stages {
                stage('Checkout') {
                    steps {
                        // Get source code from repository
                        echo 'Checking out Source code from repository'
                        checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [], gitTool: 'Default', userRemoteConfigs: [[url: 'https://github.com/Nageshshenoy1357/spring-petclinic.git']]])
                    }           
                }

                stage('Build') {
                    steps {
                        echo 'Compiling Source code'
                        // To run Maven on a Windows agent, use
                        bat "mvn spring-javaformat:apply"
                        bat "mvn clean compile"
                        // bat "mvn -Dmaven.test.failure.ignore=true clean package"
                    }
                }

                stage('Unit test') {
                    steps {
                        echo 'Executing Unit tests'
                        // To run Maven on a Windows agent, use
                        bat "mvn test"
                        // bat "mvn -Dmaven.test.failure.ignore=true clean package"
                    }

                    post {
                        // If Maven was able to run the tests, even if some of the test
                        // failed, record the test results and archive the jar file.
                        always {
                            junit '**/target/surefire-reports/TEST-*.xml'
                            // archiveArtifacts 'target/*.jar'
                        }
                    }
                }

                stage('SonarQube Analysis') {
                    steps {
                        echo 'Analysing Code Quality'
                        // Execute SONAR Analysis
                        withSonarQubeEnv('Sonar7.6') {
                            // def scannerHome = tool 'SonarQube Scanner'
                            bat 'mvn sonar:sonar'
                        }
                    }
                }
                stage("Quality Gate") {
                    steps {
                        echo 'Wait for Quality gate update'
                        // timeout(time: 1, unit: 'HOURS') {
                        //     // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                        //     // true = set pipeline to UNSTABLE, false = don't
                        //     waitForQualityGate abortPipeline: true
                        // }
                    }
                }
                
                stage('Package') {
                    steps {
                        echo 'Creating package'
                        // Execute Packaging
                        bat 'mvn package'
                    }
                }

                stage('Deploy') {
                    steps {
                        echo 'Deploy Package'
                    }
                }                          
            }
        }

        stage('Starting Functional Testing in Test Server') {
            agent {
                label 'master'
            }
            stages {
                stage('Integration Tests') {
                    steps {
                        // Get some code from a GitHub repository
                        echo 'Executing Integration Tests'
                    }           
                }
                stage('Regression Tests') {
                    steps {
                        // Get some code from a GitHub repository
                        echo 'Executing Regression Tests'
                    }           
                }        
            }
        }

    }
}
