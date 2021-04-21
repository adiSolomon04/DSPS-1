import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    private static Integer n=10;
    private static Integer numInstances = 0;
    private static SQSOperations sqsOperationsIn;
    private static SQSOperations sqsOperationsOut;
    private static SQSOperations sqsOperationsJobs;
    private static SQSOperations sqsOperationsAnswers;
    private static S3ObjectOperations s3Operations;
    private static EC2Operations ec2Operations;
    private static SendAndReceiveJsonToWorker jsonToWorker;
    private static List<String> workerIds;
    //todo: create worker.jar
    private static final String workerCommand = "#! /bin/bash\ncd /home/ec2-user\nmkdir LOL\njava -jar Worker.jar\n\n"; //-Xmx550m
    private static final String HTMLHeader = "<!DOCTYPE html><html><head><h1>AWS Project Adi & Eran</h1>" +
            "<h2>Answers:</h2><title>AWS Project Adi & Eran</title></head>\n<body>";
    private static final String HTMLFooter = "\n</body>\n</html>";

    private static boolean Terminate = false;



    public static void main(String[] args) throws IOException {
        BufferedWriter myWriter = new BufferedWriter(new FileWriter("fileName.txt"));
        myWriter.write("Manager start !\n");
        myWriter.flush();

        //Get n
        if (args.length != 2) {
            sendError("Err\n not enough args\nneed n and amiId");
            System.exit(1);
        }
        try {
            Integer n = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException nfe){
            sendError("Err\n"+nfe.getMessage());
        }

        String amiId = args[1];

        myWriter.write("got AMI "+amiId);
        myWriter.flush();


        /*
        Start init
         */
        //QUEUES
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        //sqsOperationsIn.createSQS();
        sqsOperationsIn.getQueue();

        //todo: for each local app!!
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        //sqsOperationsOut.createSQS();
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
        ec2Operations = new EC2Operations(amiId);
        //Json
        jsonToWorker = new SendAndReceiveJsonToWorker();

        workerIds = new ArrayList<>();

        /*
        End - init
         */
        myWriter.write("Manager while");
        myWriter.flush();

        while(true) {
            if (!Terminate) {
                //Get Jobs to job queue
                List<Message> Messages = sqsOperationsIn.getMessage();
                for (Message message : Messages) {
                    myWriter.write("file\n");
                    String Body = message.body();

                    s3Operations.downloadFileJson(Body, Body); //"inputFiles/"
                    jsonToWorker.sendJobs(Body, sqsOperationsJobs);//"inputFiles/"/*
                    myWriter.write(Body+"\t");
                    //Check if terminate
                    if (Body.substring(Body.length() - "[terminate]".length() - 1, Body.length() - 1).equals("[terminate]")) {
                        Terminate = true;
                        break;
                    }
                }

                //open new Instances if needed
                int newInstNum = (int)(Math.ceil(sqsOperationsJobs.getMessage().size() / (1.0*n)));
                if (numInstances < newInstNum) {
                    for (int i = 0; i < newInstNum - numInstances; i++) {
                        myWriter.write("before open worker");
                        myWriter.flush();
                        workerIds.add(ec2Operations.createInstance("Worker", workerCommand));
                        myWriter.write("after open worker");
                        myWriter.flush();
                    }
                }
            }

            jsonToWorker.collectAnswers(sqsOperationsAnswers);
            String HTML = jsonToWorker.getHTML();
            if(!HTML.isEmpty()) {
                s3Operations.uploadFileString(HTMLHeader + HTML + HTMLFooter);
                sqsOperationsOut.sendMessage(s3Operations.getOutKey());
            }
            break; //for one File!!!!!!!!!!!!!
        }



    }

    //todo:write to local QUEUE
    private static void sendError(String s) {
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        sqsOperationsOut.getQueue();
        sqsOperationsOut.sendMessage(s);
    }
}
