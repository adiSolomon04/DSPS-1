import com.example.ec2.CreateInstance;
import com.example.s3.S3ObjectOperations;
import software.amazon.awssdk.regions.Region;
/*
Open s3 Bucket name: "dsps-s3-adieran-2021"
 */
public class LocalApplication {

    private static Region region = Region.US_EAST_1;

    public static void main(String[] args) {
        region = Region.US_EAST_1;
        //S3ObjectOperations s3 = new S3ObjectOperations(region);
        //todo: test if opening multiple Local in 1 computer is working

        //Upload file to S3
        //s3.uploadFile("B000EVOSE4.txt");

        //Create Manager Instance
        CreateInstance.createInstance("Manager");

    }

}
