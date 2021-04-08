import com.google.gson.Gson;
//import jdk.nashorn.internal.runtime.regexp.joni.constants.internal.OPCode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class TestWorker {
    public static void main(String[] args) {
        Worker W = new Worker();
        //System.out.println(W.sentimentAnalysisHandler("These These are the best!!!! That's all there is to say."));
        //W.namedEntityRecognitionHandler("These These are the best!!!! That's all there is to say.");

        //Start - Reading input json file
        Gson gson = new Gson();
       /* Reader reader = null;
        try {
            reader = new FileReader("../resources/B000EVOSE4.txt");//(args[0]);
        } catch (FileNotFoundException e) {
        }
        assert reader != null;*/
        JsonClassRead gsonLoad = gson.fromJson("{\"title\":\"Haribo Original Gold-Bears Gummi Candy, 5-Pound Bag of Delicious Bears!  Ships to You  in Either Clear Packaging or the New Gold Updated Packaging.  The Same Delicious Gummi Bears in Either Packaging!\",\"reviews\":[{\"id\":\"RYER0W0DOQGSC\",\"link\":\"https://www.amazon.com/gp/customer-reviews/RYER0W0DOQGSC/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Love Haribo Gummi Bears\",\"text\":\"These are the best!!!! That's all there is to say.\",\"rating\":5,\"author\":\"lulabell5678\",\"date\":\"2016-09-20T21:00:00.000Z\"},{\"id\":\"R2H1I3BH2013W2\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R2H1I3BH2013W2/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"gummi for your tummy!\",\"text\":\"Is it a good ole biggun or a big ole goodun!? I think it's a little -O- both. I bought...I ate....I have no regrets. 5lb bag is the cheapest gummi you gonna get! They were fresh n tasty and brought right to my door!\",\"rating\":5,\"author\":\"ANGLIN ARMY\",\"date\":\"2014-10-22T21:00:00.000Z\"},{\"id\":\"R1H8I10HZJB9KZ\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R1H8I10HZJB9KZ/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Nice Bears....\",\"text\":\"DELICIOUS!!!! good price. Never over them....\",\"rating\":5,\"author\":\"David Cornejo P.\",\"date\":\"2015-07-30T21:00:00.000Z\"},{\"id\":\"R2M9GQ7J9AARJF\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R2M9GQ7J9AARJF/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Best Gummi Bears Ever\",\"text\":\"In perfect condition. They were not melted together. They are fresh and taste great. I put them in multiple little baggies to carry with me for work. Definitely worth the money.\",\"rating\":5,\"author\":\"PatienceRN\",\"date\":\"2015-03-07T22:00:00.000Z\"},{\"id\":\"RXGT1DO9DP20H\",\"link\":\"https://www.amazon.com/gp/customer-reviews/RXGT1DO9DP20H/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Five Stars\",\"text\":\"yummy!\",\"rating\":5,\"author\":\"Amazon Customer\",\"date\":\"2017-10-16T21:00:00.000Z\"},{\"id\":\"RG35LRUWRI7FE\",\"link\":\"https://www.amazon.com/gp/customer-reviews/RG35LRUWRI7FE/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Five Stars\",\"text\":\"Enjoying my purchase\",\"rating\":5,\"author\":\"rita dawkins\",\"date\":\"2015-10-21T21:00:00.000Z\"},{\"id\":\"R1S4OOOXVUJGIF\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R1S4OOOXVUJGIF/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"A Million Bears\",\"text\":\"All the delicious Haribo Gummy Bear you can eat! Haribo delivers the best tasting, chewy bears .\",\"rating\":5,\"author\":\"Socalinicole\",\"date\":\"2016-02-26T22:00:00.000Z\"},{\"id\":\"R2JD9W8DXKFGUM\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R2JD9W8DXKFGUM/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Five Stars\",\"text\":\"Delicious! You can never have too much gummy bears.\",\"rating\":5,\"author\":\"Alice\",\"date\":\"2016-08-12T21:00:00.000Z\"},{\"id\":\"R1LVSG0GUC5AW6\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R1LVSG0GUC5AW6/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"Five Stars\",\"text\":\"Who doesn't love Gummy Bears!!! Yum Yum!!!\",\"rating\":5,\"author\":\"Douglas Gulledge\",\"date\":\"2015-07-29T21:00:00.000Z\"},{\"id\":\"R1TK0PGD21ZO5V\",\"link\":\"https://www.amazon.com/gp/customer-reviews/R1TK0PGD21ZO5V/ref=cm_cr_arp_d_rvw_ttl?ie=UTF8&ASIN=B000EVOSE4\",\"title\":\"The gummi jackpot!\",\"text\":\"The best of the Gummi Bears!  Be aware - this is a LOT of gummi bears!!!\",\"rating\":5,\"author\":\"Susan Stern\",\"date\":\"2016-05-26T21:00:00.000Z\"}]}\n",JsonClassRead.class);
        //Finish - Reading input json file

        //Start - Reading from json class JsonClassRead
        //Build the rannable Classes and create threads
        String title1 = gsonLoad.title;
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
