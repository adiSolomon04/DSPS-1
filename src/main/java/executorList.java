import com.example.ec2.EC2Operations;
import com.example.s3.S3ObjectOperations;
import com.example.sqs.SQSOperations;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class executorList {
    private int k;
    private ExecutorService executor;
    private List<Future<SendAndReceiveJsonToWorker>> futuresReceive;
    private List<Future<SendAndReceiveJsonToWorker>> futuresSend;


    public executorList(int k){
        this.k = k;
        executor = Executors.newFixedThreadPool(this.k);
        futuresReceive = new LinkedList<Future<SendAndReceiveJsonToWorker>>();
        futuresSend = new LinkedList<Future<SendAndReceiveJsonToWorker>>();
    }

    public void addMissionReceive(SendAndReceiveJsonToWorker jsonToWorker, SQSOperations sqsOperationsAnswers, AtomicInteger numJobs){
        Future<SendAndReceiveJsonToWorker> future = executor.submit(() -> {
            jsonToWorker.collectAnswers(sqsOperationsAnswers, numJobs);
            return jsonToWorker;
        });
        futuresReceive.add(future);
    }


    public void addMissionSend(String filename,SQSOperations sqsOperationsJobs,SendAndReceiveJsonToWorker jsonToWorker){
        Future<SendAndReceiveJsonToWorker> future = executor.submit(() -> {
            jsonToWorker.sendJobs(filename, sqsOperationsJobs);//"inputFiles/"/*
            return jsonToWorker;
        });
        futuresSend.add(future);
    }


    public void checkFutureReceive(S3ObjectOperations s3Operations,String HTMLHeader,String HTMLFooter,SQSOperations sqsOperationsOut) {

        for(Future<SendAndReceiveJsonToWorker> F:futuresReceive ){

            if(F.isDone()){
                futuresReceive.remove(F);
                SendAndReceiveJsonToWorker sendAndReceiveJsonToWorker = null;
                try {
                    sendAndReceiveJsonToWorker = F.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                String HTML = sendAndReceiveJsonToWorker.getHTML();
                //if(!HTML.isEmpty()) {

                    s3Operations.uploadFileString(HTMLHeader + HTML + HTMLFooter, sendAndReceiveJsonToWorker.getOutputKey());
                    SQSOperations LocalQueue = sendAndReceiveJsonToWorker.getLocalQueue();
                    LocalQueue.sendMessage(sendAndReceiveJsonToWorker.getOutputKey());
               // }

            }
        }
    }



    public void checkFutureSend(AtomicInteger numJobs,EC2Operations ec2Operations,Integer numInstances,List<String> workerIds,String workerCommand,Integer n) {

        for(Future<SendAndReceiveJsonToWorker> F:futuresSend ){
            SendAndReceiveJsonToWorker sendAndReceiveJsonToWorker = null;
            int newJobs =0;
            if(F.isDone()){
                futuresReceive.remove(F);
                try {
                    sendAndReceiveJsonToWorker = F.get();
                    newJobs = sendAndReceiveJsonToWorker.getFileJobsLeft();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                numJobs.addAndGet(newJobs);

                //open new Instances if needed
                int newInstNum = (int)(Math.ceil(numJobs.get() / (1.0*n)));
                int maxInst = ec2Operations.openMoreWorkers();
                if (numInstances < newInstNum & maxInst>0) {
                    for (int i = 0; i < Math.min(newInstNum - numInstances, maxInst) ; i++) {
                        try {
                            workerIds.add(ec2Operations.createInstance(EC2Operations.WorkerName, workerCommand));
                        }
                        catch (Ec2Exception ec2Exception){

                        }
                        numInstances++;
                    }
                }

                this.addMissionReceive(sendAndReceiveJsonToWorker,sendAndReceiveJsonToWorker.getSqsOperationsAnswers(), numJobs);





            }
        }
    }

    public boolean isEmpty(){
        return futuresReceive.isEmpty()&futuresSend.isEmpty();
    }



}
