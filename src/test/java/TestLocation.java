import com.example.ec2.EC2Operations;

public class TestLocation {

    public static void main(String[] args) {
        EC2Operations ec2 = new EC2Operations("ami-0b582762c56763f57");
        String command = "#! /bin/bash\ncd /home/ec2-user\nmkdir NKL-TEST\n";
        ec2.createInstance("Test2", command);
    }
}
