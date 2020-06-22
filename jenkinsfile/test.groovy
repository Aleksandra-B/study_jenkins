pipeline {
    agent { node('master') }
    stages {
        stage('Download project and clean working space') {
            steps {
                script {
                    cleanWs()
                }
                script {
                    echo 'Start download project'
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
        stage('Create docker image') {
            steps {
                script {
                    sh "docker build ${WORKSPACE}/auto -t webapp"
                    sh "docker run -d webapp"
                    sh "docker exec -it webapp 'df -h > ~/proc'"
                }
            }
        }

    }
}
