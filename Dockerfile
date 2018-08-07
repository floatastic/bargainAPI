FROM hseeberger/scala-sbt:8u171-2.12.6-1.2.0

WORKDIR /bargainAPI

ADD . /bargainAPI

CMD sbt run
