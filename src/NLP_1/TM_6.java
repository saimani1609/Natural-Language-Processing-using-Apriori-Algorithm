package NLP_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.util.WordlistLoader;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class TM_6 {

    List<Integer> bodyStart;
    List<Integer> bodyEnd;
    String article;
    List<String> documents;
    List<String> stopwords;
    List<Map<String, List<Double>>> totalMapList;
    List<List<String>> lemmaDoc;
    List<Double> wordRankingList;
    Map<String, Set<String>> aprioriMap;
    int searchSize; // number of ducuments to be pulled from 000.sgm
    int searchWordsNum; // number of words to be add to the pool
    String inputfileName;

    public static void main(String[] args) {

        TM_6 test = new TM_6();
        test.doThings();
    }

    private int getSearchSize() {
        int input;
        System.out.print("How many documents you want to be searched?" + ": ");
        input = new Scanner(System.in).nextInt();
        return input;
    }

    private int getSearchWordsNum() {
        int input;
        System.out.print("Enter the total number of highest tf-idf words to be added into the pool " + ": ");
        input = new Scanner(System.in).nextInt();
        return input;
    }

    private String getInputFileName() {
        String input;
        System.out.print("Enter the NAME of DATASET (eg. xxx.sgm) " + ": ");
        input = new Scanner(System.in).nextLine();
        return input;
    }

    public void doThings() {
        inputfileName = getInputFileName();
        searchSize = getSearchSize();
        searchWordsNum = getSearchWordsNum();

        documents = new ArrayList<String>();
        lemmaDoc = new ArrayList<List<String>>();
        article = inputFile(inputfileName);
        totalMapList = new ArrayList<Map<String, List<Double>>>();
        getStopWords("stopwords.txt");
        findMatches(article, "<BODY>");
        findMatches(article, "</BODY>");
        AprioriCalculator_V2 aCalc = new AprioriCalculator_V2();

        for (int i = 0; i < bodyEnd.size(); i++) {
            documents.add(article.substring(bodyStart.get(i), bodyEnd.get(i)));
        }

        for (int i = 0; i < searchSize; i++) {

            Map<String, List<Double>> wordTable = new HashMap<String, List<Double>>();
            List<String> doc = parse(documents.get(i)); // 每一篇的lemma
            lemmaDoc.add(doc); // use for search df

            for (int j = 0; j < doc.size(); j++) {
                if (wordTable.containsKey(doc.get(j))) {
                    List<Double> temp = wordTable.get(doc.get(j));
                    double count;
                    count = temp.get(0) + 1.0;
                    temp.set(0, count);

                    wordTable.put(doc.get(j), temp);
                } else {
                    List<Double> temp = new ArrayList<Double>();
                    temp.add(1.0);
                    wordTable.put(doc.get(j), temp);
                }
            }
            // System.out.println(wordTable.keySet());
            totalMapList.add(wordTable);
        }

        findTF_IDF(totalMapList);
        Collections.sort(wordRankingList, Collections.reverseOrder());
        // System.out.println(totalMapList);
        // System.out.println(wordRankingList);
        aprioriTableGen();
        // System.out.println(aprioriMap);
        // aCalc.aprioriCalculator(aprioriMap);
        outputTXT(aprioriMap);
        aCalc.doThings();
        // outputTXT(aprioriMap);

    }

    public void outputTXT(Map<String, Set<String>> aprioriMap) {
        BufferedWriter writer = null;
        String doc = "KeywordsFromDocuments";

        try {
            writer = new BufferedWriter(new FileWriter("KeywordsOfEachRequiredDocument.txt"));
            writer.write(doc);
            writer.newLine();

            for (Map.Entry<String, Set<String>> entry : aprioriMap.entrySet()) {
                String line;
                // String doctNum;
                String keyWords = "";
                Set<String> words = new HashSet<String>();
                words = entry.getValue();

                // line = entry.getKey()+ "\t\t\t";
                for (String word : words) {
                    keyWords += word + " ";
                }
                // System.out.println("****"+keyWords.charAt(keyWords.length()-1));
                if (keyWords.endsWith(" ")) {
                    keyWords = keyWords.substring(0, keyWords.length() - 1) + "";
                }
                // System.out.println("****"+keyWords);
                line = entry.getKey() + "\t\t\t" + keyWords;

                writer.write(line);
                writer.newLine();
            }

            // writer.write( doc);

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
    }

    public void aprioriTableGen() {
        aprioriMap = new HashMap<String, Set<String>>();
        Set<String> topKwords = new HashSet<String>();

        for (int i = 0; i < searchWordsNum; i++) {
            double targetTFIDF = wordRankingList.get(i);
            for (int j = 0; j < totalMapList.size(); j++) {
                String docTitle = "Doc" + j;
                // Set<String> keyWords = new HashSet<String>();
                Map<String, List<Double>> totalMapCopy = new HashMap<String, List<Double>>();
                totalMapCopy = totalMapList.get(j);

                for (Map.Entry<String, List<Double>> entry : totalMapCopy.entrySet()) {
                    if (entry.getValue().get(0) == targetTFIDF) {
                        String keyword = entry.getKey();
                        // System.out.println(keyword + " " + targetTFIDF);

                        if (aprioriMap.containsKey(docTitle)) {
                            Set<String> value = new HashSet<String>();
                            value = aprioriMap.get(docTitle);
                            value.add(keyword);
                            aprioriMap.put(docTitle, value);

                        } else {
                            Set<String> value = new HashSet<String>();
                            value.add(keyword);
                            aprioriMap.put(docTitle, value);
                        }
                    }
                }
            }
        }
    }

    public void findTF_IDF(List<Map<String, List<Double>>> totalMapList) {
        List<Double> newStats;
        wordRankingList = new ArrayList<Double>();

        for (int i = 0; i < totalMapList.size(); i++) {
            Map<String, List<Double>> tempMap = new HashMap<String, List<Double>>();
            tempMap = totalMapList.get(i);
            for (String word : tempMap.keySet()) {
                int count = 1;
                for (int j = 0; j < totalMapList.size(); j++) {
                    if (j == i) {
                        j++;
                    } else {
                        if (totalMapList.get(j).keySet().contains(word)) {
                            count++; // df
                        }
                    }
                }

                double tf_idf = tempMap.get(word).get(0) * Math.log10(searchSize / count);

                newStats = new ArrayList<Double>();
                newStats.add(tf_idf); // tf_idf
                tempMap.put(word, newStats);
                wordRankingList.add(tf_idf);
            }

            totalMapList.set(i, tempMap);
        }
    }

    public List<String> parse(String text) {

        StanfordCoreNLP pipeline;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        List<String> lemmas = new LinkedList<String>();

        for (CoreMap sentence : sentences) {
            for (CoreLabel word : sentence.get(TokensAnnotation.class)) {
                lemmas.add(word.get(LemmaAnnotation.class));
            }
        }
        // System.out.println("ori doc: "+documents.get(0));
        // System.out.println("lem bef stpw: "+lemmas);

        List<String> temp = new ArrayList<String>(lemmas);

        for (String word : lemmas) {
            for (String target : stopwords) {
                if (target.equals(word) || isNumeric(word)) {

                    temp.remove(word);
                }
            }
        }
        // System.out.println("lem aft stpw: "+temp);
        return temp;

    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
                                                // '-' and decimal.
    }

    /**
     * get article as one String
     * 
     * @param location
     * @return
     */
    public String inputFile(String location) {
        String line = "";
        String article;
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(location));
            bodyStart = new ArrayList<Integer>();
            bodyEnd = new ArrayList<Integer>();

            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            br.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return article = builder.toString();

    }

    public void getStopWords(String txtLocation) {
        String line;
        stopwords = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(txtLocation));
            while ((line = br.readLine()) != null) {

                stopwords.add(line);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println(stopwords);
    }

    public void findMatches(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (regex.equals("<BODY>")) {
            while (matcher.find()) {

                bodyStart.add(matcher.end());
            }
        }

        if (regex.equals("</BODY>")) {
            while (matcher.find()) {

                bodyEnd.add(matcher.start() - 1);
            }
        }

    }

}
