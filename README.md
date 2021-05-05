# DSPS-1
Description
----
This project done within the Programming distributed systems course of Dr. Adler Menachem.
The code was written in Java and use the Amazon Web Services(AWS).

inorder to run the code you have to save the cardential that given from amazon and save them ןn the appropriate location on the computer.
Then, you can run in the terminal the comandline:
```bash
java -jar Local.jar inputFileName1... inputFileNameN outputFileName1... outputFileNameN n [terminate]
```
* inputFileNameI is the name of the input file I.
* outputFileName is the name of the output file.
* n is the workers’ files ratio (reviews per worker).
* terminate indicates that the application should terminate the manager at the end.

  the output of the system is html files that containing a line for each input review.
  
  time for all input: 8:20 minutes
  
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
    * check all the future of the send jobs mission, for each one that done add the number of new job to atomic Integer numJobs which contain how much jobs dont have answer.
        * open instance in the ratio of numJobs and n
        * submit to the executor mission to recevie jobs from "ANSWER_SQS_ID" every new job that have done we decrise numJobs (check by id) and add to the HTML
     * check all the future of the receive jobs mission, for each one that done upload the full html to S3 and send to the specific local sqs the name of the file to downlod
6. while there is future 
      * check all the future of the send jobs mission
      * check all the future of the receive jobs mission
7. close instances.
  
Worker
----
1. create SQSOperations object which simplifies the aws operation to send massage throw the sqs from mannager to workers (*one sqs for all the workers* "Jobs_Queue").
2. get a single job from the Jobs_Queue
    * a job is writen in a JSON format with the fields such as:
        * jobFile - the file that the job is related to;
        * jobNum - the job number in the file;
        * text - review to analyze
3. Analyze the review using sentiment and named Entity Recognition.
4. Get the file's answer queue and send the result with HTML tags(like <p> and <a>).
5. Go to 1. 

finishes when job queue is empty.
terminated by manager.
      
      
      
SQS
-----
VISIBILITY_TIMEOUT - 150
Region = US_EAST_1
      
Mandatory Requirements
-----
* Be sure to submit a README file. Does it contain all the requested information? If you miss any part, you will lose points. Yes including your names and ids.

      There is every thing
* Did you think for more than 2 minutes about security? Do not send your credentials in plain text!
      
      The credentials saved only on the local computer in file named credentials. Inorder to give the premmision to the instance in the aws server we gave them WorkerAndMennager role with every premission they need
* Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? Scalability is very important aspect of the system, be sure it is scalable!

      
      The software can work for a large number of users and a large number of files. It will be necessary to change the instance type of the manager to a larger size and allow it a larger number of threads in the pool. But these are two simple changes that will allow the program to work for as many customers as we want.
      The manager is still a bottleneck because even though everything runs on different threads it's a single computer.
* What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!

      In case of a fall of one of the workers, another worker will take the task and complete it. If this creates a double answer situation there will be no problem because each answer is directed to a specific file and also has an id.
* Threads in your application, when is it a good idea? When is it bad? Invest time to think about threads in your application!

      We used thread only in the mannage נecause there is a bottleneck and a lot of independent missions 
* Did you run more than one client at the same time? Be sure they work properly, and finish properly, and your results are correct.

      Yes.
* Do you understand how the system works? Do a full run using pen and paper, draw the different parts and the communication that happens between them.

      We understand
* Did you manage the termination process? Be sure all is closed once requested!

      Yes.
* Did you take in mind the system limitations that we are using? Be sure to use it to its fullest!

      We have limited the amount of instance to the maximum amount allowed. We also addressed the problematic that there is a chance that a message will not be processed within the allotted time.
* Are all your workers working hard? Or some are slacking? Why?

      Everyone works hard because everybody has access to all messages
* Is your manager doing more work than he's supposed to? Have you made sure each part of your system has properly defined tasks? Did you mix their tasks? Don't!

      No.
* Lastly, are you sure you understand what distributed means? Is there anything in your system awaiting another?

      Yes.No.
      
      
      
      
      
      
      
      
      
      
      Eran Aflalo *********
      Adi Solomon *********
