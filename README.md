# DSPS-1
Description
----
This project done within the Programming distributed systems course of Dr. Adler Menachem.
The code was written in Java and use the aws service of Amazon Web Services(AWS).

inorder to run the code you have to save the cardential that given from amazon and save them ןn the appropriate location on the computerץ
Then, you can run in the terminal the comandline:
```bash
java -jar *******.jar inputFileName1... inputFileNameN outputFileName1... outputFileNameN n [terminate]
```
* inputFileNameI is the name of the input file I.
* outputFileName is the name of the output file.
* n is the workers’ files ratio (reviews per worker).
* terminate indicates that the application should terminate the manager at the end.

  the output of the system is html files that containing a line for each input review.
  
Local
----
* the local read the arguments.
* then create S3ObjectOperations object which simplifies the aws operation to uploud and download files from the bucket "dsps-s3-adieran-2021".
* them create ec2Operations object which simplifies the aws operation to make instances.
  * Region = US_EAST_1
  * ami id = 
  * InstanceType = T2_MEDIUM
  * iamInstanceProfile = WorkerAndMennager (which give the instance option to open other instance, upload and downlod file from S3 and send massage throw the SQS without the cardentioal)
* 
