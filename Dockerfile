FROM nginx
WORKDIR /usr/share/nginx/html
COPY ../../Downloads/study_jenkins-master/web/Hello_docker.html /usr/share/nginx/html
CMD cd /usr/share/nginx/html && sed -e s/Docker/"$AUTHOR"/ Hello_docker.html > index.html ; nginx -g 'daemon off;'