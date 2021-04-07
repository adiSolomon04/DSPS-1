import Recognition.namedEntityRecognitionHandler;
import Recognition.sentimentAnalysisHandler;

public class Worker {
    static Recognition.sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static Recognition.namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();

    public int sentimentAnalysisHandler(String review) {
        return sentimentAnalysisHandler.findSentiment(review);
    }
    public void namedEntityRecognitionHandler(String review) {
        namedEntityRecognitionHandler.printEntities(review);
    }

}