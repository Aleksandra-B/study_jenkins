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
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                        }
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
        stage('Build & run docker image') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t abritsheva"
                        sh "echo '${password}' | sudo -S docker run -d -p 8080:80 --name abritsheva -v /home/adminci/is_mount_dir:/stat abritsheva"
                    }
                }
            }
        }
        stage('Get stats & write to file') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }

        }
    }
}