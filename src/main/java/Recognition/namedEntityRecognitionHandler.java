package Recognition;

import java.util.List;
import java.util.Properties;
import edu.stanford.nlp.ie.machinereading.BasicEntityExtractor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;


public class namedEntityRecognitionHandler {
    private static StanfordCoreNLP NERPipeline;
    public namedEntityRecognitionHandler() {
        Properties props = new Properties();
        props.put("annotators", "tokenize , ssplit, pos, lemma, ner");
        NERPipeline = new StanfordCoreNLP(props);
    }

    public static String printEntities(String review){
        StringBuilder list_of_the_named_entities = new StringBuilder("[");


        // create an empty Annotation just with the given text
        Annotation document = new Annotation(review);
        // run all Annotators on this text
        NERPipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);

                if(ne.equals("PERSON") | ne.equals("LOCATION") | ne.equals("ORGANIZATION")) {  //todo: need to return the words? or just the entity
                    System.out.println("\t-" + word + ":" + ne); //todo - delete print
                    list_of_the_named_entities.append("(").append(word).append(":").append(ne).append("),");
                }
            }
        }
        //todo: delete the last comma
        list_of_the_named_entities.append("]");
        return list_of_the_named_entities.toString();
    }

}
