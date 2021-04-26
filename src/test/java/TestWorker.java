import com.google.gson.Gson;
//import jdk.nashorn.internal.runtime.regexp.joni.constants.internal.OPCode;

import java.io.*;

public class TestWorker {
    public static void main(String[] args) throws IOException {
        //Worker W = new Worker();
        //System.out.println(W.sentimentAnalysisHandler("My dad and Ana loved it!"));
        //W.namedEntityRecognitionHandler("My dad and Ana loved it!");

        String Filename = "adi2.txt";

        //Start - Reading input json lined file
        Gson gson = new Gson();
        Reader reader = null;
        BufferedReader inStream = null;
        //File Reader
        reader = new FileReader(Filename);//(args[0]);
        assert reader != null;
        inStream = new BufferedReader(reader);
        //Read num of lines
        int lines = 0;
        while (inStream.readLine() != null) lines++;
        inStream.close();

        //Read a line, parse it
        reader = new FileReader(Filename);//(args[0]);
        assert reader != null;
        inStream = new BufferedReader(reader);
        JsonClassRead[] gsonLoad = new JsonClassRead[lines];

        int numJobs = 0;
        for(int i=0; i<lines; i++) {
            gsonLoad[i] = gson.fromJson(inStream.readLine(), JsonClassRead.class);
            numJobs+= gsonLoad[i].reviews.length;
        }
        System.out.println(numJobs);

        //Reading data from json class JsonClassRead
        //String title1 = gsonLoad[0].title;
        //System.out.println(title1);

    }

    public static class JsonClassRead{
        public String title;
        public Review[] reviews;
    }

    public static class Review{
        public String id;
        public String link;
        public String title;
        public String text;
        public Integer rating;
        public String author;
        public String date;
    }
}
