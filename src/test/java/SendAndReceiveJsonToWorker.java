public class SendAndReceiveJsonToWorker {

    public static void main(String[] args) {

    }
}/*
import com.example.sqs.SQSOperations;
import jdk.nashorn.internal.scripts.JO;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import com.google.gson.Gson;
//import jdk.nashorn.internal.runtime.regexp.joni.constants.internal.OPCode;

import java.io.*;
import java.util.List;




public class SendAndReceiveJsonToWorker {
    private static final String JOB_QUEUE_NAME = "jobsQueue";
    private static final String ANSWER_QUEUE_NAME = "answerQueue";

    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}

    public static void main(String[] args) throws IOException, InterruptedException {

        SQSOperations JOB_SQS = new SQSOperations(JOB_QUEUE_NAME);
        JOB_SQS.createSQS();
        JOB_SQS.getQueue();

        SQSOperations ANSWER_SQS = new SQSOperations(ANSWER_QUEUE_NAME);
        ANSWER_SQS.createSQS();
        ANSWER_SQS.getQueue();

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
        for (int i = 0; i < lines; i++)
            gsonLoad[i] = gson.fromJson(inStream.readLine(), TestWorker.JsonClassRead.class);

        for (int i = 0; i < lines; i++)
            for (int j = 0; j < gsonLoad[i].reviews.length; j++) {
                JOB_SQS.sendMessage(gson.toJson(gsonLoad[i].reviews[j]));
            }


        Thread.sleep(10);
        String HTML = "";
        List<Message> messages = ANSWER_SQS.getMessage();
        while (messages.size() != 0) {
            for (Message m : messages) {

                HTML = HTML + m.body();
            }

            // delete messages from the queue
            ANSWER_SQS.deleteMessage(messages);

            messages = ANSWER_SQS.getMessage();
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


    }
*/

