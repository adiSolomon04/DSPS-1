import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
import java.util.List;

/*
Open s3 Bucket name: "dsps-s3-adieran-2021"
 */
public class LocalApplication {

    private static SQSOperations sqsOperationsIn;
    private static SQSOperations sqsOperationsOut;
    //private static Region region = Region.US_EAST_1;


    public static void main(String[] args) {
        S3ObjectOperations s3Operations = new S3ObjectOperations();
        EC2Operations ec2Operations = new EC2Operations();
        //todo: test if opening multiple Local in 1 computer is working

        //Create Manager Instance
        List<String> instanceIds = new ArrayList<>();
        if(!ec2Operations.ManagerExists()) {
            String Id = ec2Operations.createInstance(ec2Operations.ManagerName);
            instanceIds.add(Id);
            sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
            sqsOperationsIn.createSQS();
            sqsOperationsIn.getQueue();
        }
        sqsOperationsIn = new SQSOperations(SQSOperations.IN_QUEUE);
        sqsOperationsIn.createSQS();
        sqsOperationsIn.getQueue();
        //Upload file to S3
        s3Operations.uploadFile("B000EVOSE4.txt");
        sqsOperationsIn.sendMessage(s3Operations.getKey());

        /*
        while(true) {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            String name = reader.readLine();

        }
         */
        sqsOperationsOut = new SQSOperations(SQSOperations.OUT_QUEUE);
        List<Message> messages = sqsOperationsOut.getMessage();
        while(messages.isEmpty()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messages = sqsOperationsOut.getMessage();
        }
        s3Operations.downloadFileHtml("bin/"+messages.get(0).body()+".txt");

        System.out.println("Downloaded file\t" + messages.get(0).body());

        //ec2Operations.deleteInstanceById(instanceIds);
        //ec2Operations.deleteInstanceByName(ec2Operations.ManagerName);
    }

}
