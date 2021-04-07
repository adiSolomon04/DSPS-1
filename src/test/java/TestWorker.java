import com.google.gson.Gson;
import jdk.nashorn.internal.runtime.regexp.joni.constants.internal.OPCode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class TestWorker {
    public static void main(String[] args) {
        Worker W = new Worker();
        System.out.println(W.sentimentAnalysisHandler("These These are the best!!!! That's all there is to say."));
        W.namedEntityRecognitionHandler("These These are the best!!!! That's all there is to say.");

        //Start - Reading input json file
        Gson gson = new Gson();
        Reader reader = null;
        try {
            reader = new FileReader("../resources/B000EVOSE4.txt");//(args[0]);
        } catch (FileNotFoundException e) {
        }
        assert reader != null;
        JsonClassRead[] gsonLoad = gson.fromJson(reader,JsonClassRead[].class);
        //Finish - Reading input json file

        //Start - Reading from json class JsonClassRead
        //Build the rannable Classes and create threads
        String title1 = gsonLoad[0].title;
        System.out.println(title1);

    }

    public static class JsonClassRead{
        public String title;
        public Review[] reviews;
    }

    public static class Review{
        public String id;
        public String link;
        public String title;
        public String dest;
        public Integer rating;
        public String author;
        public String date;
    }
}
