import Recognition.namedEntityRecognitionHandler;
import Recognition.sentimentAnalysisHandler;

import com.example.sqs.SQSOperations;
import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.HashMap; // import the HashMap class


public class Worker {
    static Recognition.sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static Recognition.namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();
    static Gson gson = new Gson();  //json

    static HashMap<String, SQSOperations> ANSWER_SQS_Hash = new HashMap<>();


    //private static final String JOB_QUEUE_NAME = "jobsQueue";
    //private static final String ANSWER_QUEUE_NAME = "answerQueue";


    public static int sentimentAnalysisHandler(String review) {
        return sentimentAnalysisHandler.findSentiment(review);
    }
    public static String namedEntityRecognitionHandler(String review) {
        return namedEntityRecognitionHandler.printEntities(review);
    }

    //public void MainWorkerClass(){
    public static void main(String[] args) {



        SQSOperations JOB_SQS =  new SQSOperations(SQSOperations.JOB_QUEUE);
        JOB_SQS.getQueue();

        //SQSOperations ANSWER_SQS =  new SQSOperations(SQSOperations.ANSWER_QUEUE);
        //ANSWER_SQS.getQueue();


        while(true){
            //get massages
            List<Message> messages = JOB_SQS.getMessage();
            //go throw the massages
            for (Message m : messages) {
                //read the massage body
                SendAndReceiveJsonToWorker.Review review = gson.fromJson(m.body(), SendAndReceiveJsonToWorker.Review.class);
                //do job
                String ans = jsonToHTML(review);
                //todo: check
                //If there are 2 same messages and queue already destroyed - put will not work
                try {
                    getAnswetSQS(review.jobFile).sendMessage(ans);
                } catch (Exception e) {
                }
            }

            // delete messages from the queue
            JOB_SQS.deleteMessage(messages);
            //todo: fix - heartbeat time or max time, not busy wait-  long and short polling ? or die after end of message???
        }

    }

    private static SQSOperations getAnswetSQS(String jobFile){
        SQSOperations ANSWER_SQS = ANSWER_SQS_Hash.get(jobFile);
        if (ANSWER_SQS == null){
            ANSWER_SQS = new SQSOperations(SQSOperations.ANSWER_QUEUE+'_'+jobFile);
            ANSWER_SQS.getQueue();
            ANSWER_SQS_Hash.put(jobFile,ANSWER_SQS);
        }
        return ANSWER_SQS;
    }

    private static String jsonToHTML(SendAndReceiveJsonToWorker.Review review){
        //SendAndReceiveJsonToWorker.Review review = gson.fromJson(Message, SendAndReceiveJsonToWorker.Review.class);
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
        SendAndReceiveJsonToWorker.Answer ans = new SendAndReceiveJsonToWorker.Answer();
        ans.body = result;
        ans.jobNum = review.jobNum;
        ans.jobFile = review.jobFile;
        return gson.toJson(ans);

    }
    /*
    public static class Review{
        public String id;
        public String link;
        public String text;
        public Integer rating;
    }

     */

}


