# setup

testing and build backend with creating executable files (first line is not required).

    ./gradlew clean build test check

    ./gradlew bootJar

creating docker images for run the backend programs.

    sudo docker build \
        --build-arg JAR_FILE="./build/libs/globalstatistic-1.0.0.jar" \
        -t wordstatistic_globalstatistic:1.0.0 ./globalstatistic

    sudo docker build \
        --build-arg JAR_FILE="./build/libs/localstatistic-1.0.0.jar" \
        -t wordstatistic_localstatistic:1.0.0 ./localstatistic

    sudo docker build \
        --build-arg JAR_FILE="./build/libs/user-1.0.0.jar" \
        -t wordstatistic_user:1.0.0 ./user

    sudo docker build \
        --build-arg JAR_FILE="./build/libs/usingHistory-1.0.0.jar" \
        -t wordstatistic_usinghistory:1.0.0 ./usingHistory

creating a environment variables for nestjs application
and create docker images for a frontend service and ngix proxy.

    echo \
    "NEXT_PUBLIC_LOCAL_HOST=http://localhost:3000
    NEXT_PUBLIC_API_GLOBALSTATISTIC_HOST=http://globalstatistic.localhost:80
    NEXT_PUBLIC_API_LOCALSTATISTIC_HOST=http://localstatistic.localhost:80
    NEXT_PUBLIC_API_USER_HOST=http://user.localhost:80" \
    > ./ngix/nextjs/wordstatistic/.env.local
    sudo docker build \
        -t wordstatistic_ngix:1.0.0 ./ngix
    sudo docker build \
        -t wordstatistic_nextjs_frontend:1.0.0 ./ngix/nextjs

downloads all other dependencies and run a docker compose cluster.

    sudo docker compose up -d

# description

A website for analysis count of words in texts. It supports only the Latin alphabet.

## frontend

A service nextjs-frontend contains a nextjs frontend project with all frontend.

All connections are listened in a ngix server
that separates requests into a frontend part (the service nextjs-frontend) and
into a backend cluster (with a gategay based on traefik).

All setting of host names can be found in /ngix/ngix.conf

example

    server {
        listen 8080;
        server_name localhost;

        location / {
            proxy_set_header Host traefik;
            proxy_pass http://traefik:8080;
        }
    }

## backend

all connections in backend here written with a ngix proxy.
To connect directly in backend (traefik service) use the same ports+1.
It can be changed or removed in docker-compose.yml.

emapmle

    traefik:
        image: traefik:v3.0
        container_name: traefik
        restart: unless-stopped
        ...
        ports:
            - 81:80
            - 8081:8080
            - 15433:15432

### globalstatistic

a service for collect all word that ware added to any source. It generates statistic for everyine.

Also it can get a new text from a user with a corresponding permission directly for update statistic.

### localstatistic

A service for create and store texts are storage by topics for analysis word count for a specified text or topic or user.

It sends added texts in a globalstatistic service for a global statistic.

### user

A service for singing in, singing up for users and storage their roles and permissions.

It generates and refresh access and refresh tokens.

### usingHistory

A service for collecting all using information from all services.
It receives json messages to create a table based on it if the table does not exist and add information in it.

example

    curl -X 'POST' \
        'http://localstatistic.localhost/topicsAndTexts/addNewText' \
        -H 'accept: application/json' \
        -H 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI2MWI5NTlkZS0xN2Y2LTQwYTUtODU1Yi05OTBhODFiODVjNjQiLCJpYXQiOjE3MjE3MjQyMjUsImV4cCI6MTcyMTcyNDgyNSwidXNlcm5hbWUiOiJ0ZXN0VXNlciIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.m44m4FKEIMk-2TThJFlXE3JUzV0KhPqI-wLnNrejLZY2PovYFSlBkqNwLWScYgPa' \
        -H 'Content-Type: application/json' \
        -d '{
        "topic": "testTopic",
        "name": "testTextName",
        "text": "a test text"
        }'

by java code

    usingHistory.sendMessage(
            "addText",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length(),
                HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER, textName.length()
            ),
            Set.of(
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
            )
        );

leads to a json

    {
	    "serviceName": "localStatistic",
	    "historyTableName": "addText",
	    "created": 1721724290356,
	    "shortData": {},
	    "integerData": {
		    "text_name_length": 12,
		    "topic_name_length": 9
	    },
	    "longData": {},
	    "floatData": {},
	    "doubleData": {},
	    "stringData": {
		    "user_id": "61b959de-17f6-40a5-855b-990a81b85c64",
		    "topic_name": "testTopic",
		    "text_name": "testTextName"
	    },
	    "dateData": {},
	    "primaryKey": [
		    "topic_name"
	    ]
    }

leads to creating a table

    select * from usingHistory.localStatistic_addText

| text_name_length | topic_name_length | user_id | topic_name | text_name    | record_created |
| :---: | :---: | :---: | :---: | :---: | :---: |
| 12 | 9 | 61b959de-17f6-40a5-855b-990a81b85c64 | testTopic | testTextName | 2024-07-23 08:44:50 |

# swagger

It can be used for check all res api and automatically generate curl requests
(use direct connect at a port 81 to have ability to execute requests).

http://globalstatistic.localhost:80/swagger-ui.html

http://localstatistic.localhost:80/swagger-ui.html

http://user.localhost:80/swagger-ui.html

# postgres manager

a pdadmin service for contact with
all postgresql databases in a backend cluster
(use parameters for databases from a docker compose cluster).

http://pdadmin.localhost:15432/

docker-compose.yml contains all names and password

examples

traefik http authorization

//haart test

    - 'traefik.http.middlewares.admin-auth.basicauth.users=haart:$$2a$$12$$SUDmkLybXr3LQVCoHfmo4.bao6PIZe1R8vESkiCBAqbbNZ2jAdQkm'

pgadmin-password

        environment:
            - PGADMIN_DEFAULT_EMAIL=haart@admin.com
            - PGADMIN_DEFAULT_PASSWORD=test

postgres-passwords

        environment:
            - POSTGRES_DB=user
            - POSTGRES_USER=user
            - POSTGRES_PASSWORD=password

# kafka manager

It can be used to use and manually change
all kafka messages in a backend cluster
(use parameters for connecting from a docker compose cluster).

http://kafkaui.localhost:15432/

docker-compose.yml contains all names and password

examples

traefik http authorization

//haart test

    - 'traefik.http.middlewares.admin-auth.basicauth.users=haart:$$2a$$12$$SUDmkLybXr3LQVCoHfmo4.bao6PIZe1R8vESkiCBAqbbNZ2jAdQkm'

# redis manager

It can be used to use and manually change
all redis data.

http://rediscomander.localhost:15432/

docker-compose.yml contains all names and password

examples

traefik http authorization

//haart test

    - 'traefik.http.middlewares.admin-auth.basicauth.users=haart:$$2a$$12$$SUDmkLybXr3LQVCoHfmo4.bao6PIZe1R8vESkiCBAqbbNZ2jAdQkm'

# clickhouse manager

It can be used to connect and manipulate
all statistic information in a statistic service clickhouse database
by sql requests.

http://usinghistoryclickhouse.localhost:15432/play

view all tables

    select table_name
    from information_schema.tables
    where table_type = 'BASE TABLE';

interact with a table where usingHistory is a database name

    select *
    from usingHistory.table_name;

docker-compose.yml contains all names and password

examples

traefik http authorization

//haart test

    - 'traefik.http.middlewares.admin-auth.basicauth.users=haart:$$2a$$12$$SUDmkLybXr3LQVCoHfmo4.bao6PIZe1R8vESkiCBAqbbNZ2jAdQkm'

clickhouse password

        environment:
            - CLICKHOUSE_DB=usingHistory
            - CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT=1
            - CLICKHOUSE_USER=haart
            - CLICKHOUSE_PASSWORD=test

# use

## frontend

### nextjs-frontend

main page with global statistic

    http://localhost:80/

for sign in

    http://localhost/auth/signIn

sign up, sign up

    http://localhost/auth/signIn
    http://localhost/auth/signIn

for changing a current username, password, deleting a current user

    http://localhost/auth/changeUsername
    http://localhost/auth/changePassword
    http://localhost/auth/deleteUser

for manipulating own topics, texts and showing statistic

    http://localhost/statistic/data/topics
    http://localhost/statistic/data/topic/[topic]/texts
    http://localhost/statistic/data/topic/[topic]/text/[text]
    http://localhost/statistic/userStatistic

## backend

### globalstatistic service

#### get most popular list

    curl -i globalstatistic.localhost:80/globalStatistic/getMostPopularWords?limit=3 && printf '\n'

#### add addictional text in global statistic (admin permission is required)

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X POST -d "a test text for testing" \
        "globalstatistic.localhost:80/globalStatistic/addText"  \
        && printf '\n'

### localstatistic service

#### get all own topics

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/topicsAndTexts/getAllTopicsForUser" \
        && printf '\n'

#### get all topic's texts

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/topicsAndTexts/getAllTextsForTopic?topicName=topic1" \
        && printf '\n'

#### get text content

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/topicsAndTexts/getTextContent?topicName=topic1&textName=text1" \
        && printf '\n'

#### add a new own topic

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X POST \
        -d '{"name": "topic1"}' \
        "localstatistic.localhost:80/topicsAndTexts/addNewTopic?username=user1" \
        && printf '\n'

#### add a new text in a selected topic

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X POST \
        -d '{"topic": "topic1", "name": "text1", "text": "a test text"}' \
        "localstatistic.localhost:80/topicsAndTexts/addNewText" \
        && printf '\n'

#### update an own topic

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X PUT \
        -d '{"oldName": "topic1", "newName": "topic1test"}' \
        "localstatistic.localhost:80/topicsAndTexts/updateTopic" \
        && printf '\n'

#### update a text in a selected topic

###### a test name

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X PUT \
        -d '{"topic": "topic1", "newName": "text1", "oldName": "text1test"}' \
        "localstatistic.localhost:80/topicsAndTexts/updateText" \
        && printf '\n'

###### a test name and test content

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X PUT \
        -d '{"topic": "topic1", "newName": "text1", "oldName": "text1test", "text":"a new test 2"}' \
        "localstatistic.localhost:80/topicsAndTexts/updateText" \
        && printf '\n'

#### delete an own topic

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X DELETE \
        -d '{"name": "topic1"}' \
        "localstatistic.localhost:80/topicsAndTexts/deleteTopic" \
        && printf '\n'

#### delete a text in a selected topic

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        -X DELETE \
        -d '{"topic": "topic1", "name":"text1"}' \
        "localstatistic.localhost:80/topicsAndTexts/deleteText" \
        && printf '\n'

#### get post popular words for all own texts

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/localStatistic/getMostPopularWordsForUser?limit=3" \
        && printf '\n'

#### get post popular words for selected topic's texts

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/localStatistic/getMostPopularWordsForTopic?topicName=topic1&limit=3" \
        && printf '\n'

#### get post popular words for a selected text

    curl -i -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
        "localstatistic.localhost:80/localStatistic/getMostPopularWordsForText?topicName=topic1&textName=text1&limit=3" \
        && printf '\n'

### user service

#### sing up a new user

    curl -i -X POST -H "Content-Type: application/json" \
        -d '{"name": "haart","password": "pas"}' \
        "user.localhost:80/registry/signUp" \
        && printf '\n'

#### get jwt access and refresh tokens

    curl -i -X POST -H "Content-Type: application/json" \
        -d '{"name": "haart","password": "pas"}' \
        "user.localhost:80/registry/signIn" \
        && printf '\n'

#### refresh jwt access and refresh tokens

    curl -i -X POST -H "Content-Type: application/json" \
        -d '{
            "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbWUiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyalq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu",
            "refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzUzMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"
        }' \
        "user.localhost:80/registry/refreshToken" \
        && printf '\n'

#### change a username

    curl -i -X PUT -H "Content-Type: application/json" \
        -H "Authorization: Bearer {token}" \
        -d '{"currentPassword": "test","newUsername": "haart_1024"}' \
        "user.localhost:80/changeUser/changeUsername" \
        && printf '\n'

#### change a password

    curl -i -X PUT -H "Content-Type: application/json" \
        -H "Authorization: Bearer {token}" \
        -d '{"currentPassword": "test","newPassword": "TestPassword"}' \
        "user.localhost:80/changeUser/changePassword" \
        && printf '\n'

#### delete a user

    curl -i -X DELETE -H "Content-Type: application/json" \
        -H "Authorization: Bearer {token}" \
        -d '{"currentPassword": "test"}' \
        "user.localhost:80/changeUser/deleteUser" \
        && printf '\n'

# delete

sudo docker compose down

sudo docker volume prune

sudo rm -fr ./databases_data
