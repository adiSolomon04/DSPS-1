/*
 * Copyright 2011-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.s3;
 
import java.io.File;
import java.io.IOException;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;

/*
 Every Local creates a S3ObjectOperations object
 In order to upload to S3.
 Create Bucket Func. doesnt work.
 */

public class S3ObjectOperations {
    private S3Client s3;
    //
    private static String bucket_name = "dsps-s3-adieran2-2021";
    private Region region = Region.US_EAST_1;

    /*
    Need an open aws bucket
    called as bucket_name

    File upload and download with rand number
    (there may be multiple Local applictions)  ** May move it to 'Local Application'

    1. can one Local upload multiple files?
     */
    public S3ObjectOperations() {
        s3 = S3Client.builder().region(region).build();
        // Has one file only!!!
    }

    /*
    Upload file from local to s3
     */
    public void uploadFile(String file_path,String key){
        s3.putObject(PutObjectRequest.builder().bucket(bucket_name).key(key)
                        .build(),
                RequestBody.fromFile(new File(file_path)));
    }

    /*
    upload file from Manager to s3
    todo: change outputKey by file
     */
    public void uploadFileString(String content, String outputKey){
        s3.putObject(PutObjectRequest.builder().bucket(bucket_name).key(outputKey)
                        .build(),
                RequestBody.fromString(content));
    }

    /*
    Uses outputKey to download the output file.
     */
    public void downloadFileHtml(String file_path,String outputKey){
        s3.getObject(GetObjectRequest.builder().bucket(bucket_name).key(outputKey).build(),
                ResponseTransformer.toFile(new File(file_path))); //.html
    }

    public void downloadFileJson(String file_path, String key){
        s3.getObject(GetObjectRequest.builder().bucket(bucket_name).key(key).build(),
                ResponseTransformer.toFile(new File(file_path))); //.html
    }

    public void deleteFile(String key){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket_name).key(key).build();
        s3.deleteObject(deleteObjectRequest);
    }

    public void createBucket(String bucket, Region region) {
        s3.createBucket(CreateBucketRequest
                .builder()
                .bucket(bucket)
                .createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(region.id())
                                .build())
                .build());

        System.out.println(bucket);
    }
    public void deleteBucket(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
        s3.deleteBucket(deleteBucketRequest);
    }

    /*public String getKey() {
        return key;
    }

    public String getOutKey() { return outputKey;  }*/
}

    /*
        // Multipart Upload a file
        String multipartKey = "multiPartKey";
        multipartUpload(bucket, multipartKey);
 
        // List all objects in bucket
 
        // Use manual pagination
        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(bucket)
                .maxKeys(1)
                .build();
 
        boolean done = false;
        while (!done) {
            ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);
            for (S3Object content : listObjResponse.contents()) {
                System.out.println(content.key());
            }
 
            if (listObjResponse.nextContinuationToken() == null) {
                done = true;
            }
 
            listObjectsReqManual = listObjectsReqManual.toBuilder()
                    .continuationToken(listObjResponse.nextContinuationToken())
                    .build();
        }
        // Build the list objects request
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucket)
                .maxKeys(1)
                .build();
 
        ListObjectsV2Iterable listRes = s3.listObjectsV2Paginator(listReq);
        // Process response pages
        listRes.stream()
                .flatMap(r -> r.contents().stream())
                .forEach(content -> System.out.println(" Key: " + content.key() + " size = " + content.size()));
 
        // Helper method to work with paginated collection of items directly
        listRes.contents().stream()
                .forEach(content -> System.out.println(" Key: " + content.key() + " size = " + content.size()));
        // Use simple for loop if stream is not necessary
        for (S3Object content : listRes.contents()) {
            System.out.println(" Key: " + content.key() + " size = " + content.size());
        }
 
        // Get Object
        s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(),
                ResponseTransformer.toFile(Paths.get("multiPartKey")));
        // snippet-end:[s3.java2.s3_object_operations.download]
 
        // Delete Object
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
        s3.deleteObject(deleteObjectRequest);
 
        // Delete Object
        deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket).key(multipartKey).build();
        s3.deleteObject(deleteObjectRequest);
 
        deleteBucket(bucket);
    }
 

 
    private static void deleteBucket(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
        s3.deleteBucket(deleteBucketRequest);
    }
 
    /**
     * Uploading an object to S3 in parts
     *

    private static void multipartUpload(String bucketName, String key) throws IOException {
 
        int mb = 1024 * 1024;
        // First create a multipart upload and get upload id 
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName).key(key)
                .build();
        CreateMultipartUploadResponse response = s3.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();
        System.out.println(uploadId);
 
        // Upload all the different parts of the object
        UploadPartRequest uploadPartRequest1 = UploadPartRequest.builder().bucket(bucketName).key(key)
                .uploadId(uploadId)
                .partNumber(1).build();
        String etag1 = s3.uploadPart(uploadPartRequest1, RequestBody.fromByteBuffer(getRandomByteBuffer(5 * mb))).eTag();
        CompletedPart part1 = CompletedPart.builder().partNumber(1).eTag(etag1).build();
 
        UploadPartRequest uploadPartRequest2 = UploadPartRequest.builder().bucket(bucketName).key(key)
                .uploadId(uploadId)
                .partNumber(2).build();
        String etag2 = s3.uploadPart(uploadPartRequest2, RequestBody.fromByteBuffer(getRandomByteBuffer(3 * mb))).eTag();
        CompletedPart part2 = CompletedPart.builder().partNumber(2).eTag(etag2).build();
 
 
        // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
        // parts and finish the multipart operation.
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(part1, part2).build();
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload).build();
        s3.completeMultipartUpload(completeMultipartUploadRequest);
    }
 
    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
} */