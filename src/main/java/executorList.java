import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class executorList {
    private int k;
    private ExecutorService executor;
    private List<Future<SendAndReceiveJsonToWorker>> futures;

    public executorList(int k){
        this.k = k;
        executor = Executors.newFixedThreadPool(this.k);
        futures = new LinkedList<Future<SendAndReceiveJsonToWorker>>();
    }

    public void addMission(SendAndReceiveJsonToWorker jsonToWorker, SQSOperations sqsOperationsAnswers){
        Future<SendAndReceiveJsonToWorker> future = executor.submit(() -> {
            jsonToWorker.collectAnswers(sqsOperationsAnswers);
            return jsonToWorker;
        });
        futures.add(future);
    }

    public void checkFuture(S3ObjectOperations s3Operations,String HTMLHeader,String HTMLFooter,SQSOperations sqsOperationsOut) {

        for(Future<SendAndReceiveJsonToWorker> F:futures ){

            if(F.isDone()){
                futures.remove(F);
                SendAndReceiveJsonToWorker sendAndReceiveJsonToWorker = null;
                try {
                    sendAndReceiveJsonToWorker = F.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                String HTML = sendAndReceiveJsonToWorker.getHTML();
                //if(!HTML.isEmpty()) {

                    s3Operations.uploadFileString(HTMLHeader + HTML + HTMLFooter, sendAndReceiveJsonToWorker.getOutputKey());
                    sqsOperationsOut.sendMessage(sendAndReceiveJsonToWorker.getOutputKey());
               // }

            }
        }
    }



}
