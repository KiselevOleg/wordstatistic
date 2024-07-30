FROM node:22

WORKDIR /usr/src/app

COPY ./wordstatistic .

RUN yarn install
RUN yarn build

CMD [ "yarn", "start" ]
