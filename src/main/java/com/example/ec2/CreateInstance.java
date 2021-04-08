//snippet-sourcedescription:[CreateInstance.java demonstrates how to create an EC2 instance.]
//snippet-keyword:[SDK for Java 2.0]
//snippet-keyword:[Code Sample]
//snippet-service:[ec2]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[11/02/2020]
//snippet-sourceauthor:[scmacdon]
/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.example.ec2;
// snippet-start:[ec2.java2.create_instance.complete]
 
// snippet-start:[ec2.java2.create_instance.import]
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;
import software.amazon.awssdk.regions.Region;
//import org.slf4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.slf4j.LoggerFactory;
// snippet-end:[ec2.java2.create_instance.import]
 
/**
 * Creates an EC2 instance
 */
public class CreateInstance {
    //static final Logger logger = LoggerFactory.getLogger(CreateInstance.class);
    public static void main(String[] args) {
        final String USAGE =
                "To run this example, supply an instance name and AMI image id\n" +
                        "Both values can be obtained from the AWS Console\n" +
                        "Ex: CreateInstance ec2 ami-076515f20540e6e0b\n";
        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(1);
        }
 
        String name = args[0];
        String amiId = "ami-5b41123e";
        // snippet-start:[ec2.java2.create_instance.main]
        //Ec2Client ec2 = Ec2Client.create();

        DefaultCredentialsProvider CredsProvider = DefaultCredentialsProvider.create();
        AwsBasicCredentials awsCreds = (AwsBasicCredentials) CredsProvider.resolveCredentials();

        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        IamInstanceProfileSpecification role = IamInstanceProfileSpecification.builder()
                .name("MELECH")
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .instanceType(InstanceType.T2_MICRO)
                .imageId(amiId)
                .maxCount(1)
                .minCount(1)
                .userData(Base64.getEncoder().encodeToString("#! /bin/bash".getBytes()))
                .iamInstanceProfile(role)
                .build();
 
        RunInstancesResponse response = ec2.runInstances(runRequest);
 
        String instanceId = response.instances().get(0).instanceId();
 
        Tag tag = Tag.builder()
                .key("Name")
                .value(name)
                .build();
 
        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();
 
        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "Successfully started EC2 instance %s based on AMI %s",
                    instanceId, amiId);
       
        } catch (Ec2Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        // snippet-end:[ec2.java2.create_instance.main]
        System.out.println("Done!");
    }
}
// snippet-end:[ec2.java2.create_instance.complete]