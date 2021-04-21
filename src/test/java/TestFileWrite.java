import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TestFileWrite {

    public static void main(String[] args) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("fileName.txt"));
        writer.write("Hi cool");
        writer.flush();
        writer.close();
    }
}
