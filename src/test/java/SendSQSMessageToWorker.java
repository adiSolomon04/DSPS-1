import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

public class SendSQSMessageToWorker {
    private static final String QUEUE_NAME = "jobsQueue";
    private static final String ANSWER_QUEUE_NAME = "answerQueue";

    public static void main(String[] args) {
        SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1).build();


        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            CreateQueueResponse create_result = sqs.createQueue(request);
            CreateQueueRequest request_ANSWER = CreateQueueRequest.builder()
                    .queueName(ANSWER_QUEUE_NAME)
                    .build();
            CreateQueueResponse create_result_ANSWER = sqs.createQueue(request_ANSWER);


        } catch (QueueNameExistsException e) {
            throw e;

        }

        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(QUEUE_NAME)
                .build();
        String queueUrl = sqs.getQueueUrl(getQueueRequest).queueUrl();

        SendMessageRequest send_msg_request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody("{\"id\":\"R14D3WP6J91DCU\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R14D3WP6J91DCU/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=0689835604\",\"title\":\"Five Stars\",\"text\":\"My dad and Ana loved it.\",\"rating\":5,\"author\":\"Nikki J\",\"date\":\"2017-05-01T21:00:00.000Z\"}")
                .build();
        sqs.sendMessage(send_msg_request);

/*
        // Send multiple messages to the queue
        SendMessageBatchRequest send_batch_request = SendMessageBatchRequest.builder()
                .queueUrl(queueUrl)
                .entries(
                        SendMessageBatchRequestEntry.builder()
                                .messageBody("Hello from message 1")
                                .id("msg_1")
                                .build()
                        ,
                        SendMessageBatchRequestEntry.builder()
                                .messageBody("Hello from message 2")
                                //.delaySeconds(10)
                                .id("msg_2")
                                .build())
                .build();
        sqs.sendMessageBatch(send_batch_request);

*/
    }

}
