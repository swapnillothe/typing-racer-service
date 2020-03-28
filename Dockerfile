FROM clojure:lein

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN ["lein", "deps"]

CMD ["lein", "ring", "server-headless", "9002"]