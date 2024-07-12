# setup

./gradlew clean build test check

./gradlew bootJar



sudo docker build \
    --build-arg JAR_FILE="./build/libs/globalstatistic-1.0.0.jar" \
    -t wordstatistic_globalstatistic:1.0.0 ./globalstatistic

sudo docker build \
    --build-arg JAR_FILE="./build/libs/localstatistic-1.0.0.jar" \
    -t wordstatistic_localstatistic:1.0.0 ./localstatistic

sudo docker build \
    --build-arg JAR_FILE="./build/libs/user-1.0.0.jar" \
    -t wordstatistic_user:1.0.0 ./user

sudo docker compose up -d

# use

curl -i globalstatistic.localhost:80/globalStatistic/getMostPopularWords?limit=3 && printf '\n'

curl -i -X POST -d "a test text for testing" globalstatistic.localhost:80/globalStatistic/addText && printf '\n' \
    && printf '/n'



curl -i "localstatistic.localhost:80/topicsAndTexts/getAllTopicsForUser?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b" \
    && printf '\n'

curl -i "localstatistic.localhost:80/topicsAndTexts/getAllTextsForTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1" \
    && printf '\n'

curl -i "localstatistic.localhost:80/topicsAndTexts/getTextContent?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&textName=text1" \
    && printf '\n'

curl -i -X POST -H "Content-Type: application/json" \
    -d '{"name": "topic1"}' \
    "localstatistic.localhost:80/topicsAndTexts/addNewTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&username=user1" \
    && printf '\n'

curl -i -X POST -H "Content-Type: application/json" \
    -d '{"topic": "topic1", "name": "text1", "text": "a test text"}' \
    "localstatistic.localhost:80/topicsAndTexts/addNewText?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b" \
    && printf '\n'

curl -i "localstatistic.localhost:80/localStatistic/getMostPopularWordsForUser?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&limit=3" \
    && printf '\n'

curl -i "localstatistic.localhost:80/localStatistic/getMostPopularWordsForTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&limit=3" \
    && printf '\n'

curl -i "localstatistic.localhost:80/localStatistic/getMostPopularWordsForText?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&textName=text1&limit=3" \
    && printf '\n'



curl -i -X POST -H "Content-Type: application/json" \
    -d '{"name": "haart","password": "pas"}' \
    "user.localhost:80/registry/signUp" \
    && printf '\n'

curl -i -X POST -H "Content-Type: application/json" \
    -d '{"name": "haart","password": "pas"}' \
    "user.localhost:80/registry/signIn" \
    && printf '\n'

curl -i -X POST -H "Content-Type: application/json" \
    -d '{"accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbWUiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyalq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu","refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzUzMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"}' \
    "user.localhost:80/registry/refreshToken" \
    && printf '\n'

# delete

sudo docker compose down

sudo docker volume prune

#sudo docker volume rm wordstatistic_pg_data

sudo rm -fr ./postgres_data
