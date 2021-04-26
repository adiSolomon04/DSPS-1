import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static final String amiId = "ami-024fffd05e677c367";
    private static String managerId = "";
    private static String n = "50";

    private static SQSOperations sqsOperationsIn;
    private static SQSOperations sqsOperationsOut;
    private static HashMap<String, String> fileNames;

// java  -jar yourjar.jar inputFileName1... inputFileNameN n [terminate]
    public static void main(String[] args) {
        String n;
        int fileNum = 0;
        //Input
        if (args.length < 2) {
            System.out.println("Not enough Args");
            System.exit(1);
        }

        if (args[args.length].equals("[terminate]")) {
            n = args[args.length - 2];
            fileNum = args.length - 2;
        } else {
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
        fileNames = new HashMap<>(10);
        S3ObjectOperations s3Operations = new S3ObjectOperations();
        EC2Operations ec2Operations = new EC2Operations(amiId);
        //todo: test if opening multiple Local in 1 computer is working
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        if (!ec2Operations.ManagerExists()) {
            managerId = ec2Operations.createInstance(ec2Operations.ManagerName, managerCommand + " " + n + " " + amiId + "\n");
            sqsOperationsIn.createSQS();
            sqsOperationsOut.createSQS();
        }
        sqsOperationsIn.getQueue();
        sqsOperationsOut.getQueue();

        //Upload files to S3
        for (int i = 0; i < fileNum; i++) {
            String fileName = args[i];
            String ID = "" + System.currentTimeMillis();
            String key = "input_" + ID;
            s3Operations.uploadFile(fileName, key);
            fileNames.put(ID, fileName.substring(0,fileName.length()-4));
            sqsOperationsIn.sendMessage(key);
        }
        //todo: add terminate

        //check for errors
        List<Message> Messages = sqsOperationsIn.getMessage();
        if ((Messages.size() != 0) && Messages.get(0).body().startsWith("Err")) {
            System.out.println(sqsOperationsOut.getMessage().get(0).body());
            System.exit(1);
        }

        //todo:local opens answer queue before putting message in QUEUE to manager
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
                System.out.println("Downloaded answer to file\t" + fileName+"\n");
            }
            sqsOperationsOut.deleteMessage(messages);
        }
    }


    private static String getID(String message){
        String[] arr = message.split("_");
        return arr[1];
    }

}
