import Recognition.namedEntityRecognitionHandler;
import Recognition.sentimentAnalysisHandler;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import java.util.Date;
import java.util.List;

public class Worker {
    static Recognition.sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static Recognition.namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();
    private static final String QUEUE_NAME = "jobsQueue";


    public int sentimentAnalysisHandler(String review) {
        return sentimentAnalysisHandler.findSentiment(review);
    }
    public void namedEntityRecognitionHandler(String review) {
        namedEntityRecognitionHandler.printEntities(review);
    }

    //public void MainWorkerClass(){
    public static void main(String[] args) {
        SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1).build();

        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(QUEUE_NAME)
                .build();
        String queueUrl = sqs.getQueueUrl(getQueueRequest).queueUrl();
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();

        while(true){
            //get massage
            List<Message> messages = sqs.receiveMessage(receiveRequest).messages();
            // do job
            //todo: job
            for (Message m : messages) {
                System.out.println(m.body());
            }
            //todo: return answer

            // delete messages from the queue
            for (Message m : messages) {
                DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(m.receiptHandle())
                        .build();
                sqs.deleteMessage(deleteRequest);
            }

            //todo: fix - heartbeat time or max time, not busy wait-  long and short polling ? or die after end of message???
        }




    }
}