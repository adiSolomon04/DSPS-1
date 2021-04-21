import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
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


    public static void main(String[] args) {
        S3ObjectOperations s3Operations = new S3ObjectOperations();
        EC2Operations ec2Operations = new EC2Operations(amiId);
        //todo: test if opening multiple Local in 1 computer is working

        //Create Manager Instance
        //List<String> instanceIds = new ArrayList<>();
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        if(!ec2Operations.ManagerExists()) {
            managerId = ec2Operations.createInstance(ec2Operations.ManagerName, managerCommand+" "+n+" "+amiId+"\n");
            sqsOperationsIn.createSQS();
            sqsOperationsOut.createSQS();
        }
        sqsOperationsIn.getQueue();
        sqsOperationsOut.getQueue();

        //Upload file to S3
        s3Operations.uploadFile("B000EVOSE4.txt");
        List<Message> Messages = sqsOperationsIn.getMessage();
        if((Messages.size()!=0)&& Messages.get(0).body().startsWith("Err")){
            System.out.println(sqsOperationsOut.getMessage().get(0).body());
            System.exit(1);
        }

        //todo:local opens answer queue before putting message in QUEUE to manager
        sqsOperationsIn.sendMessage(s3Operations.getKey());
        List<Message> messages = sqsOperationsOut.getMessage();
        while(messages.isEmpty()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messages = sqsOperationsOut.getMessage();
        }
        s3Operations.downloadFileHtml("bin/"+messages.get(0).body()+".html");

        System.out.println("Downloaded file\t" + messages.get(0).body());

        //ec2Operations.deleteInstanceById(instanceIds);
        //ec2Operations.deleteInstanceByName(ec2Operations.ManagerName);
    }

}
