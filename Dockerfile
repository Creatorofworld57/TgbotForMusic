#FROM eclipse-temurin:21-alpine as build
# Нам требуется образ, содержащий maven, при помощи
# ключевого слова as мы указываем псевдоним для контейнера сборки,
# чтобы при его помощи в дальнейшем обращаться к контейнеру
FROM openjdk:21-jdk

ARG JAR_FILE=target/Telega2-0.0.1-SNAPSHOT.jar

# Собирать проект будем в /build


# Теперь необходимо скопировать необходимые для сборки проекта файлы в конейнер
COPY ./target/Telega2-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
