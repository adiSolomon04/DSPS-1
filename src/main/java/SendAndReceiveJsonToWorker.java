import com.example.sqs.SQSOperations;
import com.google.gson.Gson;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class SendAndReceiveJsonToWorker {

    private Gson gson;
    private Reader reader;
    private BufferedReader inStream;
    private String HTML; //switch to hash of string to HTML string
    private boolean[] fileJobs;
    private int fileJobsLeft;
    private String outputKey; //output File name
    private SQSOperations LocalQueue;
    private SQSOperations sqsOperationsAnswers;
    //private int newJobs;

    //private HashMap<String, <Array,String, Integer>> filesAnswers;

    public SendAndReceiveJsonToWorker(String QueueName) {
        gson = new Gson();
        reader = null;
        inStream = null;
        LocalQueue = new SQSOperations(QueueName);
        //filesAnswers = new HashMap<>();
    }

    public int sendJobs(String Filename, SQSOperations JOB_SQS) throws IOException {
        outputKey = "output"+Filename;
        //File Reader
        try {
            reader = new FileReader(Filename);//(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
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
        JsonClassRead[] gsonLoad = new JsonClassRead[lines];
        for (int i = 0; i < lines; i++)
            gsonLoad[i] = gson.fromJson(inStream.readLine(), JsonClassRead.class);

        int jobCount=0;
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < gsonLoad[i].reviews.length; j++) {
                gsonLoad[i].reviews[j].jobNum = jobCount;
                gsonLoad[i].reviews[j].jobFile = Filename;
                JOB_SQS.sendMessage(gson.toJson(gsonLoad[i].reviews[j]));
                jobCount++;
            }
        }
        fileJobs = new boolean[jobCount];
        //assign number of jobs to Left
        fileJobsLeft = jobCount--;
        return fileJobsLeft;
    }

    /*
    Merge all Answers from a input files
     */
    public void collectAnswers(SQSOperations ANSWER_SQS, AtomicInteger numJobs){//, SQSOperations sqsOperationsOut) {
        LocalQueue.getQueue();
        HTML = "";
        List<Message> messages = ANSWER_SQS.getMessage();
        Answer ans;
        /*
        Exit if there are input files.
         */
        while (fileJobsLeft != 0) {
            for (Message m : messages) {
                //HTML = HTML +"\n"+ m.body();

                ans = gson.fromJson(m.body(), Answer.class);
                //check if answer was already taken.
                if(!fileJobs[ans.jobNum]){
                    fileJobsLeft--;
                    numJobs.decrementAndGet();
                    HTML = HTML +"\n"+ ans.body;
                    fileJobs[ans.jobNum] = true;
                    if(fileJobsLeft==0) {
                        break;

                        //sqsOperationsOut.sendMessage("file address/key");
                        //s3 - upload file


                    }
                }
            }

            // delete messages from the queue
            ANSWER_SQS.deleteMessage(messages);

            messages = ANSWER_SQS.getMessage();
        }
        ANSWER_SQS.deleteSQS();
    }

    /*
    Switch to get html from hash (if file is finished)
     */
    public String getHTML(){
        return HTML;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public SQSOperations getLocalQueue() {return LocalQueue; }

    public int getFileJobsLeft() { return fileJobsLeft; }

    public void setSqsOperationsAnswers(SQSOperations sqsOperationsAnswers) {
        this.sqsOperationsAnswers =  sqsOperationsAnswers;
    }
    public SQSOperations getSqsOperationsAnswers() {
        return sqsOperationsAnswers;
    }


    public class JsonClassRead {
        public String title;
        public Review[] reviews;
    }

    public class Review {
        public String id;
        public String link;
        public String title;
        public String text;
        public Integer rating;
        public String author;
        public String date;
        public Integer jobNum;
        public String jobFile;
        public String Queue;
    }

    public static class Answer {
        public Integer jobNum;
        public String jobFile;
        public String body;
        public String Queue;
    }
}

    /*
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
*/

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
