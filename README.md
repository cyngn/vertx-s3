# Vert.x S3

The Vert.x S3 library is an asynchronous, vert.x based library for Java. It provides commonly-used functionality for manipulating objects in S3 buckets, such as creating, downloading, and deleting objects. APIs are asynchronous and built on the fast I/O substrate, [Vert.x 3.0](http://vertx.io). 


## Getting Started

Add a dependency to vertx-s3:

```xml
<dependency>
    <groupId>com.cyngn.vertx</groupId>
    <artifactId>vertx-s3</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Features

Vert.x S3 is little more than a wrapper around Vertx's [HttpClient API](http://vertx.io/docs/vertx-core/java/#_writing_http_servers_and_clients). You can use it to PUT, GET, and DELETE objects from S3. You use it in much the same way as you would a normal HTTPClient:

    S3Client client = new S3Client(vertx, config;
    S3ClientRequest putRequest = client.createPutRequest(bucket, key, handler);
    S3ClientRequest getRequest = client.createGetRequest(bucket, key, handler);
    S3ClientRequest deleteRequest = client.createDeleteRequest(bucket, key, handler);

The handlers are `Handler<HttpClientResponse>`, so you can easily get the S3 status code from them. 
If you need to attach a `Handler<Buffer>` to `event.bodyHandler()` or `event.dataHandler()` and `event.endHandler()` as part of your response handler. 

You may write to or modify these requests as you need to, but must end them to send. For example:

    putRequest.setChunked(true);
    putRequest.write(part1);
    putRequest.write(part2);
    putRequest.end();
    
Note also that the requests are [WriteStreams](http://vertx.io/docs/apidocs/io/vertx/core/streams/WriteStream.html) and you may [Pump](http://vertx.io/docs/apidocs/io/vertx/core/streams/Pump.html) data into them as such. 

There are also some shortcut calls to quickly make requests:

    client.put(bucket, key, data, handler);
    client.get(bucket, key, handler);
    client.delete(bucket, key, handler);
    
These end the request as part of the call, sending it off and calling the handler when there is a response. 

The another key functionality of library is handling authentication of requests to and from S3; SuperS3t will automatically sign requests right as they are ready to be sent.

### Configuration

```json
{
  "producer" : {
    "awsAccessKey": "<your aws access key>",
    "awsSecretKey": "<your aws secret key>",
    "s3Endpoint" : "<s3 endpoint - s3-us-west-1.amazonaws.com", or s3-us-east-1.amazonaws.com or .... >"
  }
}
```

## Special Thanks
Credit to https://github.com/spartango for writing initial version of it in https://github.com/spartango/SuperS3t for Vertx 1.3. I have forked it off to maintain for subsequent versions of Vertx and add more functionality on top of his library.