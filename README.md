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

curl globalstatistic.localhost:80/globalStatistic/getMostPopularWords?limit=3 && printf '\n'

curl -X POST -d "a test text for testing" globalstatistic.localhost:80/globalStatistic/addText && printf '\n' \
    && printf '/n'



curl "localstatistic.localhost:80/topicsAndTexts/getAllTopicsForUser?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b" \
    && printf '\n'

curl "localstatistic.localhost:80/topicsAndTexts/getAllTextsForTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1" \
    && printf '\n'

curl "localstatistic.localhost:80/topicsAndTexts/getTextContent?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&textName=text1" \
    && printf '\n'

curl -X POST -H "Content-Type: application/json" \
    -d '{"name": "topic1"}' \
    "localstatistic.localhost:80/topicsAndTexts/addNewTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&username=user1" \
    && printf '\n'

curl -X POST -H "Content-Type: application/json" \
    -d '{"topic": "topic1", "name": "text1", "text": "a test text"}' \
    "localstatistic.localhost:80/topicsAndTexts/addNewText?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b" \
    && printf '\n'

curl "localstatistic.localhost:80/localStatistic/getMostPopularWordsForUser?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&limit=3" \
    && printf '\n'

curl "localstatistic.localhost:80/localStatistic/getMostPopularWordsForTopic?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&limit=3" \
    && printf '\n'

curl "localstatistic.localhost:80/localStatistic/getMostPopularWordsForText?userId=3ce409d3-3cd9-449f-9625-5583d4c11d8b&topicName=topic1&textName=text1&limit=3" \
    && printf '\n'

# delete

sudo docker compose down

sudo docker volume prune

#sudo docker volume rm wordstatistic_pg_data

sudo rm -fr ./postgres_data
