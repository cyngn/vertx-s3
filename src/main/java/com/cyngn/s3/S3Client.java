package com.cyngn.s3;

import com.cyngn.s3.config.ConfigConstants;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for calling S3
 *
 * @author asarda@cyngn.com (Ajay Sarda), spartango (github:https://github.com/spartango/SuperS3t)
 */
public class S3Client {
    private static final Logger logger = LoggerFactory.getLogger(S3Client.class);
    private final Vertx vertx;

    private final String awsAccessKey;
    private final String awsSecretKey;

    private final HttpClient client;

    public S3Client(Vertx vertx, JsonObject config) {
        this.vertx = vertx;

        awsAccessKey = getRequiredConfig(config, ConfigConstants.AWS_ACCESS_KEY);
        awsSecretKey = getRequiredConfig(config, ConfigConstants.AWS_SECRET_KEY);
        String endpoint = getRequiredConfig(config, ConfigConstants.S3_ENDPOINT);

        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(endpoint));
    }

    /**
     * Retrieves object from S3.
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param handler - Handler for the result of this call
     */
    public void get(String bucket, String key, Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createGetRequest(bucket, key, handler);
        request.end();
    }

    /**
     * Puts object into S3
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param data    - data buffer
     * @param handler - Handler for the result of this call
     */
    public void put(String bucket, String key, Buffer data, Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createPutRequest(bucket, key, handler);
        request.end(data);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param upload  - file upload object
     * @param handler - Handler for the result of this call
     */
    public void put(String bucket,
                    String key,
                    HttpServerFileUpload upload,
                    Handler<HttpClientResponse> handler) {
        if (logger.isDebugEnabled()) {
            logger.debug("S3 request bucket: {}, key: {}", bucket, key);
        }

        S3ClientRequest request = createPutRequest(bucket, key, handler);
        Buffer buffer = Buffer.buffer();

        upload.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        upload.handler(data -> {
            buffer.appendBuffer(data);
        });
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param bucket   - S3 Bucket name
     * @param key      - S3 object key
     * @param upload   - file upload object
     * @param fileSize - file size in bytes for content length
     * @param handler  - Handler for the result of this call
     */
    public void put(String bucket,
                    String key,
                    HttpServerFileUpload upload,
                    long fileSize,
                    Handler<HttpClientResponse> handler) {
        if (logger.isDebugEnabled()) {
            logger.debug("S3 request bucket: {}, key: {}", bucket, key);
        }

        S3ClientRequest request = createPutRequest(bucket, key, handler);
        request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
        Buffer buffer = Buffer.buffer();

        upload.endHandler(event -> {
            request.end(buffer);
        });

        Pump pump = Pump.pump(upload, request);
        pump.start();
    }

    /**
     * Retrieves object from S3.
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param handler - Handler for the result of this call
     */
    public void delete(String bucket,
                       String key,
                       Handler<HttpClientResponse> handler) {
        S3ClientRequest request = createDeleteRequest(bucket, key, handler);
        request.end();
    }

    /**
     * Creates S3 put request
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param handler - Handler for the result of this call
     * @return {@link S3ClientRequest}
     */
    public S3ClientRequest createPutRequest(String bucket,
                     String key,
                     Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.put("/" + bucket + "/" + key,
                handler);
        return new S3ClientRequest("PUT",
                bucket,
                key,
                httpRequest,
                awsAccessKey,
                awsSecretKey);
    }

    /**
     * Creates S3 get request
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param handler - Handler for the result of this call
     * @return {@link S3ClientRequest}
     */
    public S3ClientRequest createGetRequest(String bucket,
                     String key,
                     Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.get("/" + bucket + "/" + key,
                handler);
        return new S3ClientRequest("GET",
                bucket,
                key,
                httpRequest,
                awsAccessKey,
                awsSecretKey);
    }

    /**
     * Creates S3 delete request
     *
     * @param bucket  - S3 Bucket name
     * @param key     - S3 object key
     * @param handler - Handler for the result of this call
     * @return {@link S3ClientRequest}
     */
    public S3ClientRequest createDeleteRequest(String bucket,
                        String key,
                        Handler<HttpClientResponse> handler) {
        HttpClientRequest httpRequest = client.delete("/" + bucket + "/" + key,
                handler);
        return new S3ClientRequest("DELETE",
                bucket,
                key,
                httpRequest,
                awsAccessKey,
                awsSecretKey);
    }

    /**
     * Closes underlying http client
     */
    public void close() {
        this.client.close();
    }

    private String getRequiredConfig(JsonObject config, String key) {
        String value = config.getString(key, null);

        if (null == value) {
            throw new IllegalArgumentException(String.format("Required config value not found key: %s", key));
        }
        return value;
    }
}
