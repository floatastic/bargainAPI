# bargainAPI

An example `akka-http` API with simple in-memory backend. 

Execute `sbt run` to have the service running on port `9001`.

Available endpoints with example curl commands:

- GET auctions/id
```
curl -v http://localhost:9001/v1/auctions/4ac772c5-bc52-4d3c-ba9e-4010f511e175
```

- POST auctions/
```
curl -v -H "Content-Type: application/json" http://localhost:9001/v1/auctions -d '{"data": "Test auction"}'
```

- POST lots/
```
curl -v -H "Content-Type: application/json" http://localhost:9001/v1/lots -d '{"auctionId": "0ae024c0-0ddb-4e59-9e2c-721a27c386f6", "lotData": "Test lot"}'
```
