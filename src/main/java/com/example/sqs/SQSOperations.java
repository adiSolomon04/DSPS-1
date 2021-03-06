package com.example.sqs;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSOperations {
    private String QUEUE_NAME;
    private SqsClient sqs;
    private String queueUrl;
    private ReceiveMessageRequest receiveRequest;
    public static final String JOB_QUEUE= "Jobs_Queue";
    public static final String ANSWER_QUEUE = "Answer_Queue";
    public static final String IN_QUEUE = "Input_Queue";
    public static final String OUT_QUEUE = "Output_Queue";
    public static final String LOCAL_QUEUE = "Queue";

    public SQSOperations(String QUEUE_NAME) {
        this.QUEUE_NAME = QUEUE_NAME;
        sqs = SqsClient.builder().region(Region.US_EAST_1).build();
    }

    public void createSQS() {
        Map<QueueAttributeName,String> attr = new HashMap<>();
        attr.put(QueueAttributeName.VISIBILITY_TIMEOUT,"150");
        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(QUEUE_NAME)
                    .attributes(attr)
                    .build();
            CreateQueueResponse create_result = sqs.createQueue(request);
        } catch (QueueNameExistsException e) {
            throw e;

        }
    }
    public void getQueue(){
        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(QUEUE_NAME)
                .build();
        queueUrl = sqs.getQueueUrl(getQueueRequest).queueUrl();

        //todo:  can move this place
        receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();
    }
    public void sendMessage(String message){
        SendMessageRequest send_msg_request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
        sqs.sendMessage(send_msg_request);
    }

    public List<Message> getMessage(){
        return sqs.receiveMessage(receiveRequest).messages();
    }

    public void deleteMessage(List<Message> messages){
        for (Message m : messages) {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(m.receiptHandle())
                    .build();
            sqs.deleteMessage(deleteRequest);
        }
    }

    public void deleteSQS() {
        try {
            DeleteQueueRequest request = DeleteQueueRequest.builder()
                    .queueUrl(queueUrl)
                    .build();
            DeleteQueueResponse delete_result = sqs.deleteQueue(request);
        } catch (QueueNameExistsException e) {
            throw e;
        }
    }
}


