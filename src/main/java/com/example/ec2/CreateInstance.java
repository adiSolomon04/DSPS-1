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

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;
import software.amazon.awssdk.regions.Region;
 
/**
 * Creates an EC2 instance
 */
public class CreateInstance {
    //static final Logger logger = LoggerFactory.getLogger(CreateInstance.class);
    public static void main(String[] args) {

        String amiId = "ami-5b41123e";
        String name = "adisolo ec2";
        createInstance(name, amiId);

        System.exit(1);

        // snippet-end:[ec2.java2.create_instance.main]
        System.out.println("Done!");
    }

    private static void createInstance(String name, String amiId) {
        //To run this example, supply an instance name and AMI image id

        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .instanceType(InstanceType.T2_MICRO)
                .imageId(amiId)
                .maxCount(1)
                .minCount(1)
                .userData(Base64.getEncoder().encodeToString("hi there".getBytes()))
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

        ec2.createTags(tagRequest);
        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                instanceId, amiId);
    }
}