# DSPS-1
Description
----
This project done within the Programming distributed systems course of Dr. Adler Menachem.
The code was written in Java and use the Amazon Web Services(AWS).

inorder to run the code you have to save the cardential that given from amazon and save them ןn the appropriate location on the computerץ
Then, you can run in the terminal the comandline:
```bash
java -jar Local.jar inputFileName1... inputFileNameN outputFileName1... outputFileNameN n [terminate]
```
* inputFileNameI is the name of the input file I.
* outputFileName is the name of the output file.
* n is the workers’ files ratio (reviews per worker).
* terminate indicates that the application should terminate the manager at the end.

  the output of the system is html files that containing a line for each input review.
  
  time for all input: *****
Local
----
1. the local read the arguments.
2. create S3ObjectOperations object which simplifies the aws operation to uploud and download files from the bucket "dsps-s3-adieran-2021".
3. create ec2Operations object which simplifies the aws operation to make instances.
    * Region = US_EAST_1
    * ami id = 
    * InstanceType = T2_MEDIUM
    * iamInstanceProfile = WorkerAndMennager (which give the instance option to open other instance, upload and downlod file from S3 and send massage throw the SQS without the cardentioal).
4. create SQSOperations object which simplifies the aws operation to send massage throw the sqs from local to mannager (*one sqs for all the locals* "Input_Queue").
5. create mannager if not exist. 
```bash
java -jar Manager.jar n amiId
```
7. create SQSOperations object which simplifies the aws operation to send massage throw the sqs from mannager to local (*one sqs for each the local* "Queue"+time+randomNumber).
8. upload the files to S3 in the format "'input_' + time" and send throw the local-to-manager sqs "filename-localSqsName" and insert them into hash inorder to know what files we wating for.
9. wating to massage that the file done from the sqs mannager to local (sqs for each local...) and downloads the files from the sqs
10. when got all the massage send "[teminate]" if needed

  
Manager
----
1. create SQSOperations object which simplifies the aws operation to send massage throw the sqs from local to mannager (*one sqs for all the locals* "Input_Queue").
2. create SQSOperations object which simplifies the aws operation to send massage throw the sqs from mannager to workers (*one sqs for all the workers* "Jobs_Queue").
3. create ec2Operations object which simplifies the aws operation to make instances.
    * Region = US_EAST_1
    * ami id = 
    * InstanceType = T2_MEDIUM
    * iamInstanceProfile = WorkerAndMennager (which give the instance option to open other instance, upload and downlod file from S3 and send massage throw the SQS without the cardentioal).
4. create executor with fix thread pool in size k.
5. while not Terminate
    * read massges from sqs from local to mannager
    * for each massage 
        * if it is [Terminate] skip to 5
        * else it downlad the file from s3, create SQSOperations to collect the answer from the worker to the specific file (part of the massage is which file it belong to. "Answer_Queue_ID") 
        * create SendAndReceiveJsonToWorker which is Object for receiving and sending messages of one file
        * submit to the executor mission to send jobs to the "Jobs_Queue" and count them
    * check all the future of the send jobs mission, for each one that done add the number of new job to atomic Integer which contain how much jobs dont have answer.
      
