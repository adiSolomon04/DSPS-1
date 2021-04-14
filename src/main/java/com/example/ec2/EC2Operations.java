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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import software.amazon.awssdk.regions.Region;
 
/**
 * Creates an EC2 instance
 */
public class EC2Operations {
    //static final Logger logger = LoggerFactory.getLogger(CreateInstance.class);
    //private static String amiId = "ami-5b41123e";
    private static String amiId = "ami-079766f1a081c89d4";
    private Ec2Client ec2;
    private Integer runningCode = 16;
    private Integer pendingCode = 0;
    public static final String ManagerName = "Manager";
    //static String name = "adisolo ec2";

    public EC2Operations(){
        ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }
    public String createInstance(String name){
        return createInstance(name, "");
    }
    public String createInstance(String name, String command) {
        //To run this example, supply an instance name and AMI image id

        IamInstanceProfileSpecification role = IamInstanceProfileSpecification.builder()
                .name("WorkerAndMennager")
                .build();


        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .instanceType(InstanceType.T2_MICRO)
                .imageId(amiId)
                .maxCount(1)
                .minCount(1)
                .userData(Base64.getEncoder().encodeToString(command.getBytes()))
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

        ec2.createTags(tagRequest);
        /*
        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                instanceId, amiId);

         */
        return instanceId;
    }

    public void deleteInstanceById(List<String> instanceIds){
        if(instanceIds.isEmpty())
            return;
        //for (String instanceId : instanceIds)
        //    if(instanceId=="0")
        //        instanceIds.remove(instanceId);

        ec2.terminateInstances(TerminateInstancesRequest.builder()
                .instanceIds(instanceIds)
                .build());
    }
    public void deleteInstanceByName(String name){
        List<String> instanceIds = getInstanceId(name);
        if(instanceIds.isEmpty())
            return;
        ec2.terminateInstances(TerminateInstancesRequest.builder()
                .instanceIds(instanceIds)
                .build());
    }

    public List<String> getInstanceId(String name){
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2.describeInstances(request);
        List<String> instanceIds = new ArrayList<>();
        for(Reservation reservation : response.reservations())
            for(Instance instance : reservation.instances())
                for (Tag tag : instance.tags())
                    if ((tag.value().equals(name)) && (instance.state().code() == runningCode || instance.state().code() == pendingCode)) {
                        instanceIds.add(instance.instanceId());
                    }
        return instanceIds;

    }

    /*
    check if There is only one manager //todo: make sure !!!!
     */
    public boolean ManagerExists() {
        return !getInstanceId("Manager").isEmpty();
    }
}