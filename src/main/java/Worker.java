import Recognition.namedEntityRecognitionHandler;
import Recognition.sentimentAnalysisHandler;
//  SQS
// package com.example.sqs; ?????????????????????????????????????????????????

import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

public class Worker {
    static Recognition.sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static Recognition.namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();
    static Gson gson = new Gson();  //json

    private static final String JOB_QUEUE_NAME = "jobsQueue";
    private static final String ANSWER_QUEUE_NAME = "answerQueue";


    public static int sentimentAnalysisHandler(String review) {
        return sentimentAnalysisHandler.findSentiment(review);
    }
    public static String namedEntityRecognitionHandler(String review) {
        return namedEntityRecognitionHandler.printEntities(review);
    }

    //public void MainWorkerClass(){
    public static void main(String[] args) {
        Review review;


        SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1).build();

        GetQueueUrlRequest getJobQueueRequest = GetQueueUrlRequest.builder()
                .queueName(JOB_QUEUE_NAME)
                .build();
        String queueJobUrl = sqs.getQueueUrl(getJobQueueRequest).queueUrl();
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueJobUrl)
                .build();


        GetQueueUrlRequest getAnswerQueueRequest = GetQueueUrlRequest.builder()
                .queueName(ANSWER_QUEUE_NAME)
                .build();
        String queueAnswerUrl = sqs.getQueueUrl(getAnswerQueueRequest).queueUrl();
        //ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
        //        .queueUrl(queueUrl)
         //       .build();

        while(true){
            //get massage
            List<Message> messages = sqs.receiveMessage(receiveRequest).messages();
            // do job
            //todo: job
            for (Message m : messages) {
                //todo: make mini function and check!
                System.out.println(m.body());
                review = gson.fromJson(m.body(), Review.class);
                int sentiment = sentimentAnalysisHandler(review.text);
                String list_of_the_named_entities = namedEntityRecognitionHandler(review.text);

                String result = "<p><a style=\"color:";
                switch (sentiment){
                    case 0:
                        result = result+"DarkRed";
                        break;
                    case 1:
                        result = result+"red";
                        break;
                    case 2:
                        result = result+"black";
                        break;
                    case 3:
                        result = result+"LightGreen";
                        break;
                    case 4:
                        result = result+"DarkGreen";
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + sentiment);
                }

                result = result +"\" href=\""+review.link+"\">"+review.link+"</a> | "+list_of_the_named_entities+" | " ;
                if (review.rating<sentiment)
                    result = result + "sarcasm.";
                else
                    result = result + "no sarcasm.";
                result = result + "</p>";
                //<span></span
                //todo: return answer
                SendMessageRequest send_msg_request = SendMessageRequest.builder()
                        .queueUrl(queueAnswerUrl)
                        .messageBody(result)
                        .build();
                sqs.sendMessage(send_msg_request);

            }

            // delete messages from the queue
            for (Message m : messages) {
                DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueJobUrl)
                        .receiptHandle(m.receiptHandle())
                        .build();
                sqs.deleteMessage(deleteRequest);
            }

            //todo: fix - heartbeat time or max time, not busy wait-  long and short polling ? or die after end of message???
        }

    }



    public static class Review{
        public String id;
        public String link;
        public String text;
        public Integer rating;
    }

}


