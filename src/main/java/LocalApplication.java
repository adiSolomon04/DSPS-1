import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/*
Open s3 Bucket name: "dsps-s3-adieran-2021"
Local opens default OUT_Queue and In_Queue if manager does not exist,
Starts manager and sends a file in lined Json format.
 */
public class LocalApplication {

    private static final String managerCommand = "#! /bin/bash\ncd /home/ec2-user\nmkdir LOL\njava -jar Manager.jar"; // add \n
    /*
    Update ami after creating your image
     */
    private static final String amiId = "ami-08b3b400e433f2331";
    private static String managerId = "";
    private static String n;
    private static String LocalQueueName;
    private static Boolean toTerminate;


    private static SQSOperations sqsOperationsIn;
    private static SQSOperations sqsOperationsOut;
    private static HashMap<String, String> fileNames;

// java  -jar yourjar.jar inputFileName1... inputFileNameN n [terminate]
    public static void main(String[] args) {

        int fileNum = 0;
        //Input
        if (args.length < 2) {
            System.out.println("Not enough Args");
            System.exit(1);
        }

        if (args[args.length-1].equals("[terminate]")) {
            toTerminate = true;
            n = args[args.length - 2];
            fileNum = args.length - 2;
        } else {
            toTerminate = false;
            n = args[args.length - 1];
            fileNum = args.length - 1;
        }

        try {
            Integer.parseInt(n);
        } catch (NumberFormatException nfe) {
            System.out.println("n not inserted");
            System.exit(1);
        }

        //INIT
        System.out.println("Init\n");
        fileNames = new HashMap<>(10);
        S3ObjectOperations s3Operations = new S3ObjectOperations();
        EC2Operations ec2Operations = new EC2Operations(amiId);
        //todo: test if opening multiple Local in 1 computer is working
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        if (!ec2Operations.ManagerExists()) {
            managerId = ec2Operations.createInstance(ec2Operations.ManagerName, managerCommand + " " + n + " " + amiId + "\n");
            sqsOperationsIn.createSQS();
        }
        sqsOperationsIn.getQueue();

        //Get a new Output QUEUE
        Random rand = new Random();
        LocalQueueName = SQSOperations.LOCAL_QUEUE+"_"+System.currentTimeMillis()+Math.abs(rand.nextLong());
        sqsOperationsOut = new SQSOperations(LocalQueueName);
        sqsOperationsOut.createSQS();
        sqsOperationsOut.getQueue();

        System.out.println("Uploading files to s3\n");
        //Upload files to S3
        for (int i = 0; i < fileNum; i++) {
            String fileName = args[i];
            String ID = "" + System.currentTimeMillis();
            String key = "input_" + ID;
            s3Operations.uploadFile(fileName, key);
            fileNames.put(ID, fileName.substring(0,fileName.length()-4));
            sqsOperationsIn.sendMessage(key+"-"+LocalQueueName); //input_173636363-Queue_16194582471827992286586911152223
        }
        //todo: add terminate

        /*
        //check for errors
        System.out.println("Checking for errors in Manager\n");
        List<Message> Messages = sqsOperationsIn.getMessage();
        if ((Messages.size() != 0) && Messages.get(0).body().startsWith("Err")) {
            System.out.println("Error found\n");
            System.out.println(sqsOperationsOut.getMessage().get(0).body());
            System.exit(1);
        }
        System.out.println("No Error found\n");
         */

        System.out.println("Waiting for answers from manager\n");
        List<Message> messages = sqsOperationsOut.getMessage();
        while (!fileNames.isEmpty()) {
            while (messages.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                messages = sqsOperationsOut.getMessage();
            }
            for(Message message : messages){
                String ID = getID(message.body());
                String fileName = fileNames.remove(ID);
                s3Operations.downloadFileJson("bin/" + fileName + ".html", message.body());
                s3Operations.deleteFile("input_" + ID);
                s3Operations.deleteFile(message.body());
                System.out.println("Downloaded answer to file\t" + fileName+"\n");
            }
            sqsOperationsOut.deleteMessage(messages);
            messages = sqsOperationsOut.getMessage();
        }
        System.out.println("Downloaded all answers to folder bin\n");
        if(toTerminate)
            sqsOperationsIn.sendMessage("[terminate]"); //input_173636363-Queue_16194582471827992286586911152223
        sqsOperationsOut.deleteSQS();
        System.out.println("Delete Local SQS");
    }


    private static String getID(String message){
        String[] arr = message.split("_");
        return arr[1];
    }

}
