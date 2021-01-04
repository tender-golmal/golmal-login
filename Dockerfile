FROM openjdk:11
RUN useradd -ms /bin/bash spring
USER spring
ADD build/libs/*.jar /home/spring/application.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /home/spring/application.jar" ]

