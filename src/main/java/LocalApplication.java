import com.example.ec2.EC2Operations;
//import com.example.s3.S3ObjectOperations;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;

/*
Open s3 Bucket name: "dsps-s3-adieran-2021"
 */
public class LocalApplication {

    private static Region region = Region.US_EAST_1;

    public static void main(String[] args) {
        S3ObjectOperations s3Operations = new S3ObjectOperations(region);
        EC2Operations ec2Operations = new EC2Operations();
        //todo: test if opening multiple Local in 1 computer is working

        //Create Manager Instance
        List<String> instanceIds = new ArrayList<>();
        if(!ec2Operations.ManagerExists()) {
            String Id = ec2Operations.createInstance(ec2Operations.ManagerName);
            instanceIds.add(Id);
        }

        //Upload file to S3
        s3Operations.uploadFile("B000EVOSE4.txt");
        //sqsOperations.


        //ec2Operations.deleteInstanceById(instanceIds);
        //ec2Operations.deleteInstanceByName(ec2Operations.ManagerName);

    }

}
