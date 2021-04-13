import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    private Integer n=10;
    private Integer numInstances = 0;
    private SQSOperations sqsOperationsIn;
    private SQSOperations sqsOperationsOut;
    private SQSOperations sqsOperationsJobs;
    private SQSOperations sqsOperationsAnswers;
    private S3ObjectOperations s3Operations;
    private EC2Operations ec2Operations;
    private SendAndReceiveJsonToWorker jsonToWorker;
    private List<String> workerIds;
    //todo: create worker.jar
    private final String workerCommand = "#! /bin/bash\njava -jar worker.jar\n";

    private boolean Terminate = false;



    public void main(String[] args) throws IOException {

        /*
        //Get n
        if (args.length != 1) {
            System.out.println("Please Enter number of Jobs per Instance");
            System.exit(1);
        }
        Integer n = args[0];
         */


        /*
        Start init
         */
        //QUEUES
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        //sqsOperationsIn.createSQS();
        sqsOperationsIn.getQueue();

        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        sqsOperationsOut.createSQS();
        sqsOperationsOut.getQueue();

        sqsOperationsJobs = new SQSOperations(SQSOperations.JOB_QUEUE);
        sqsOperationsJobs.createSQS();
        sqsOperationsJobs.getQueue();

        sqsOperationsAnswers = new SQSOperations(SQSOperations.ANSWER_QUEUE);
        sqsOperationsAnswers.createSQS();
        sqsOperationsAnswers.getQueue();
        //S3
        s3Operations = new S3ObjectOperations();
        //EC2
        ec2Operations = new EC2Operations();
        //Json
        jsonToWorker = new SendAndReceiveJsonToWorker();

        workerIds = new ArrayList<>();

        /*
        End - init
         */

        while(true) {
            if (!Terminate) {
                //Get Jobs to job queue
                List<Message> Messages = sqsOperationsIn.getMessage();
                for (Message message : Messages) {
                    String Body = message.body();
                    s3Operations.downloadFileJson("inputFiles/" + Body, Body);
                    jsonToWorker.sendJobs("inputFiles/" + Body, sqsOperationsJobs);
                    //Check if terminate
                    if (Body.substring(Body.length() - "[terminate]".length() - 1, Body.length() - 1).equals("[terminate]")) {
                        Terminate = true;
                        break;
                    }
                }

                //open new Instances if needed
                int newInstNum = sqsOperationsJobs.getMessage().size() / n;
                if (numInstances < newInstNum) {
                    for (int i = 0; i < newInstNum - numInstances; i++) {
                        workerIds.add(ec2Operations.createInstance("Worker", workerCommand));
                    }
                }
            }

            jsonToWorker.collectAnswers(sqsOperationsAnswers);
            String HTML = jsonToWorker.getHTML();
        }



    }
}
