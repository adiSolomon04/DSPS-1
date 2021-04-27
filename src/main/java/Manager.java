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
import java.util.concurrent.atomic.AtomicInteger;

public class Manager {

    private static Integer n;
    private static Integer k=10;

    private static Integer numInstances = 0;
    private static SQSOperations sqsOperationsIn;
    private static SQSOperations sqsOperationsOut;
    private static SQSOperations sqsOperationsJobs;
    //private static SQSOperations sqsOperationsAnswers;
    private static S3ObjectOperations s3Operations;
    private static EC2Operations ec2Operations;
    //private static SendAndReceiveJsonToWorker jsonToWorker;
    private static List<String> workerIds;
    private static executorList executorGetAnswer = new executorList(k);
    private static AtomicInteger numJobs = new AtomicInteger(0);

    //String constants
    private static final String workerCommand = "#! /bin/bash\ncd /home/ec2-user\nmkdir LOL\njava -jar Worker.jar\n\n"; //-Xmx550m
    private static final String HTMLHeader = "<!DOCTYPE html><html><head><h1>AWS Project Adi & Eran</h1>" +
            "<h2>Answers:</h2><title>AWS Project Adi & Eran</title></head>\n<body>";
    private static final String HTMLFooter = "\n</body>\n</html>";

    private static boolean Terminate = false;


// // java  -jar yourjar.jar n ami
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
            n = Integer.parseInt(args[0]);
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


        sqsOperationsJobs = new SQSOperations(SQSOperations.JOB_QUEUE);
        sqsOperationsJobs.createSQS();
        sqsOperationsJobs.getQueue();

        //S3
        s3Operations = new S3ObjectOperations();
        //EC2
        ec2Operations = new EC2Operations(amiId);

        workerIds = new ArrayList<>();

        /*
        End - init
         */
        myWriter.write("Manager while");
        myWriter.flush();

        boolean moreFuturesAvailable = true;


        while(moreFuturesAvailable) {
            while (!Terminate) {
                //Get Jobs to job queue
                List<Message> Messages = sqsOperationsIn.getMessage();
                for (Message message : Messages) {
                    myWriter.write("file\n");
                    //Check if terminate
                    if (message.body().equals("[terminate]")) {
                        Terminate = true;
                        break;
                    }

                    String[] KeyQueue = message.body().split("-");
                    s3Operations.downloadFileJson(KeyQueue[0], KeyQueue[0]); //"input_173636363
                    SQSOperations sqsOperationsAnswers = new SQSOperations(SQSOperations.ANSWER_QUEUE + '_' + KeyQueue[0]);
                    sqsOperationsAnswers.createSQS();
                    sqsOperationsAnswers.getQueue();

                    //Json
                    SendAndReceiveJsonToWorker jsonToWorker = new SendAndReceiveJsonToWorker(KeyQueue[1]);
                    int newJobs = jsonToWorker.sendJobs(KeyQueue[0], sqsOperationsJobs);//"inputFiles/"/*
                    numJobs.addAndGet(newJobs);
                    myWriter.write(KeyQueue[0]+"\t");
                    //open new Instances if needed
                    int newInstNum = (int)(Math.ceil(numJobs.get() / (1.0*n)));
                    int maxInst = ec2Operations.openMoreWorkers();
                    if (numInstances < newInstNum & maxInst>0) {
                        for (int i = 0; i < Math.min(newInstNum - numInstances, maxInst) ; i++) {
                            myWriter.write("before open worker");
                            myWriter.flush();
                            workerIds.add(ec2Operations.createInstance(EC2Operations.WorkerName, workerCommand));
                            myWriter.write("after open worker");
                            myWriter.flush();
                            numInstances++;
                        }
                    }

                    executorGetAnswer.addMission(jsonToWorker,sqsOperationsAnswers, numJobs);

                }
                executorGetAnswer.checkFuture(s3Operations,HTMLHeader,HTMLFooter,sqsOperationsOut);
                sqsOperationsIn.deleteMessage(Messages);
            }
            if(moreFuturesAvailable = !executorGetAnswer.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executorGetAnswer.checkFuture(s3Operations, HTMLHeader, HTMLFooter, sqsOperationsOut);
            }
        }
        ec2Operations.deleteInstanceById(workerIds);
        ec2Operations.deleteInstanceByName(EC2Operations.ManagerName);
    }

    //todo:write to local QUEUE
    private static void sendError(String s) {
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        sqsOperationsOut.getQueue();
        sqsOperationsOut.sendMessage(s);
    }
}
