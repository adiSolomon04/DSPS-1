import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import com.google.gson.Gson;
//import jdk.nashorn.internal.runtime.regexp.joni.constants.internal.OPCode;

import java.io.*;
import java.util.List;


public class SendAndReceiveJsonToWorker {
    private static final String QUEUE_NAME = "jobsQueue";
    private static final String ANSWER_QUEUE_NAME = "answerQueue";

    public static void main(String[] args) throws IOException, InterruptedException {
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

        String Filename = "0689835604.txt";

        //Start - Reading input json lined file
        Gson gson = new Gson();
        Reader reader = null;
        BufferedReader inStream = null;
        //File Reader
        reader = new FileReader(Filename);//(args[0]);
        assert reader != null;
        inStream = new BufferedReader(reader);
        //Read num of lines
        int lines = 0;
        while (inStream.readLine() != null) lines++;
        inStream.close();

        //Read a line, parse it
        reader = new FileReader(Filename);//(args[0]);
        assert reader != null;
        inStream = new BufferedReader(reader);
        TestWorker.JsonClassRead[] gsonLoad = new TestWorker.JsonClassRead[lines];
        for(int i=0; i<lines; i++)
            gsonLoad[i] = gson.fromJson(inStream.readLine(), TestWorker.JsonClassRead.class);

        for(int i=0; i<lines; i++)
            for (int j=0; j<gsonLoad[i].reviews.length; j++) {
                SendMessageRequest send_msg_request = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(gson.toJson(gsonLoad[i].reviews[j]))
                        .build();
                sqs.sendMessage(send_msg_request);
            }


        GetQueueUrlRequest getAnswerQueueRequest = GetQueueUrlRequest.builder()
                .queueName(ANSWER_QUEUE_NAME)
                .build();
        String queueJobUrl = sqs.getQueueUrl(getAnswerQueueRequest).queueUrl();
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueJobUrl)
                .build();

        Thread.sleep(10);
        String HTML ="";
        List<Message> messages = sqs.receiveMessage(receiveRequest).messages();
        while(messages.size()!=0) {
            for (Message m : messages) {

                HTML=HTML+m.body();
            }

            // delete messages from the queue
            for (Message m : messages) {
                DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueJobUrl)
                        .receiptHandle(m.receiptHandle())
                        .build();
                sqs.deleteMessage(deleteRequest);
            }
                messages = sqs.receiveMessage(receiveRequest).messages();
        }

        System.out.println(HTML);


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
