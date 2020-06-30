pipeline {
    agent { node('master') }
    stages {
        stage('Download project and clean working space') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop abritsheva"
                            sh "echo '${password}' | sudo -S docker container rm abritsheva"
                            currentBuild.result = 'SUCCESS'
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                            currentBuild.result = 'FAILURE'

                        }
                        echo "RESULT: ${currentBuild.result}"
                    }
                }
                script {
                    echo 'Update'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'AleksandraBritshevaGit', url: 'https://github.com/Aleksandra-B/study_jenkins.git']]])
                }
            }
        }
        stage('Run docker') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t abritsheva"
                        sh "echo '${password}' | sudo -S docker run -d -p 8142:80 --name abritsheva -v /home/adminci/study_ansible/BritshevaAV:/res abritsheva"
                    }
                }
            }
        }
        stage('Write info to file') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker exec -t abritsheva bash -c 'df -h > /res/info.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t abritsheva bash -c 'top -n 1 -b >> /res/info.txt'"
                    }
                }
            }

        }
        stage('Stop docker container') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker stop abritsheva"
                    }
                }
            }
        }
    }
}