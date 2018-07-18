# bargainAPI

An example `akka-http` API with simple in-memory backend. 

Execute `sbt run` to have the service running on port `9001`.

Available endpoints with example curl commands:

- GET auctions/id
```
curl -v http://localhost:9001/v1/auctions/4ac772c5-bc52-4d3c-ba9e-4010f511e175
```
Response:
```
HTTP/1.1 200 OK
{
  "id":"4ac772c5-bc52-4d3c-ba9e-4010f511e175",
  "data":"First"
}
```

- POST auctions/
```
curl -v -H "Content-Type: application/json" http://localhost:9001/v1/auctions -d '{"data": "Test auction"}'
```

- POST lots/
```
curl -v -H "Content-Type: application/json" http://localhost:9001/v1/lots -d '{"auctionId": "0ae024c0-0ddb-4e59-9e2c-721a27c386f6", "lotData": "Test lot"}'
```

- GET lots/?auctionId[&limit&offset]
```
curl -v http://localhost:9001/v1/lots?auctionId=4ac772c5-bc52-4d3c-ba9e-4010f511e175&offset=1&limit=2
```

Response:
```
HTTP/1.1 200 OK
{
  "items": [
    {
      "id":"826a8ebb-8c9c-45b7-b08e-c784c442f55b",
      "auctionId":"4ac772c5-bc52-4d3c-ba9e-4010f511e175",
      "data":"Lot 2 for auction 1"
    }, {
      "id":"2e5faabf-47eb-40c1-a961-b1ca7e928b49",
      "auctionId":"4ac772c5-bc52-4d3c-ba9e-4010f511e175",
      "data":"Lot 3 for auction 1"
    }
  ],
  "limit":2,
  "offset":1,
  "total":3
}
```

## Parameters validation

Validation was done with scalaz validation. An example call to demonstrate it:
```
curl -v http://localhost:9001/v1/lots?auctionId=4bc52-4d3c&offset=-1&limit=0
```
will produce a following response
```
HTTP/1.1 400 Bad Request
{
  "errors": [
    "Invalid auction Id",
    "Invalid limit",
    "Invalid offset"
  ]
}
```