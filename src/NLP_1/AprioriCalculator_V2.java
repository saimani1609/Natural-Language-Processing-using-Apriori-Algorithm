package NLP_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class AprioriCalculator_V2 {
    private int lineCounter;
    private int support_User;
    private int confi_User;
    private String fileLocation = "KeywordsOfEachRequiredDocument.txt";
    private List<Set<String>> removeList;
    

    /*
    public static void main(String[] args) {

        AprioriCalculator_V2 apriori = new AprioriCalculator_V2();
        apriori.doThings();
    }
*/
    

    /**
     * User input required Support value
     * 
     * @return
     */
    private int getSupport() {
        int input;
        System.out
                .print("Please enter Support value(e.g. Enter 20 means 20%): ");
        input = new Scanner(System.in).nextInt();
        return input;
    }

    /**
     * User input required Confidence value
     * 
     * @return
     */
    private int getConfidence() {
        int input;
        System.out
                .print("Please enter Confidence value(e.g. Enter 20 means 20%): ");
        input = new Scanner(System.in).nextInt();
        return input;
    }

    /**
     * this is a pack of all the methods
     */
    public void doThings() {
        support_User = getSupport();
        confi_User = getConfidence();
        List<String> allFiles = new ArrayList<String>();
        
        printAllData(fileLocation, support_User, confi_User);

    }

    /**
     * print all the data that meet Supp & Confi requirement
     * 
     * @param fileLocation
     * @param support_User
     * @param confi_User
     */
    public void printAllData(String fileLocation, int support_User,
            int confi_User) {
        List<Map<Set<String>, Double>> allFrequentList = new ArrayList<Map<Set<String>, Double>>();
        if (fileLocation.equals("error")) {
            System.out
                    .println("ERROR: Please Use DB name as suggested also it is CASE SENSTIVE!!");
        } else {
            TreeMap<String, Double> c1 = scanFileToGetC1(fileLocation);
            //itemSetMap = getItemSet(itemNameLocation);
            List<Set<String>> f1Set = getFrequentListFromList(c1,
                    allFrequentList);
            List<Set<String>> input = f1Set;

            do {
                List<Set<String>> candidate = candidateGen(input);

                Map<Set<String>, Double> candidateAfterCount = candidateAfterCount(
                        fileLocation, candidate);

                input = getFreqFromCandidate(candidateAfterCount,
                        allFrequentList);
            } while (input.size() != 0);
            System.out.println("\n****************** "
                    + "ASSOCIATION RULE **********************\n");
            aprioriClac(allFrequentList);
            System.out.println("\n*****************"
                    + "*****************************************\n");

        }
    }

    /**
     * the Apriori Calculator
     */
    private void aprioriClac(List<Map<Set<String>, Double>> allFrequentList) {
        int afterMapSetSize;
        int k;
        int prevIndex;
        int listSize = allFrequentList.size();
        double AB;
        double A;
        double B;
        Set<String> tempSet;
        Set<String> restSet;
        Map<Set<String>, Double> afterMap;
        Map<Set<String>, Double> previousMap;
        List<Set<String>> afterMapKeySetArray;
        List<String> subSetArr;

        for (int i = listSize - 2; i >= 1; i--) {

            if (i == 1) {
                afterMap = allFrequentList.get(i);
                previousMap = allFrequentList.get(i - 1);

                for (Set<String> set : afterMap.keySet()) {
                    tempSet = new HashSet<String>();
                    restSet = new HashSet<String>();
                    subSetArr = new ArrayList<String>();

                    AB = afterMap.get(set);
                    for (String ele : set) {
                        subSetArr.add(ele);
                    }

                    tempSet.add(subSetArr.get(0));
                    restSet.add(subSetArr.get(1));
                    A = findSetCount(tempSet, allFrequentList);
                    B = findSetCount(restSet, allFrequentList);

                    if (100 * AB / A >= confi_User) {
                        System.out.println(tempSet + "--->"
                                + restSet + ": " + "Support = "
                                + 100 * AB / lineCounter + "%; Confidence = "
                                + 100 * AB / A + "%");
                    }
                    if (100 * AB / B >= confi_User) {
                        System.out.println(restSet + "--->"
                                + tempSet + ": " + "Support = "
                                + 100 * AB / lineCounter + "%; Confidence = "
                                + 100 * AB / B + "%");
                    }
                }
            } else {
                afterMap = allFrequentList.get(i);

                afterMapKeySetArray = new ArrayList<Set<String>>();
                for (Set<String> setInAfterMap : afterMap.keySet()) {
                    afterMapKeySetArray.add(setInAfterMap);
                }

                afterMapSetSize = afterMapKeySetArray.get(0).size();
                // AB = afterMapSetSize;

                k = floorDivTwo(afterMapSetSize);
                int flag = k;
                prevIndex = i - 1;

                while (k > 0) {

                    if (k == flag) {
                        previousMap = allFrequentList.get(prevIndex);
                    } else {
                        prevIndex -= 1;
                        previousMap = allFrequentList.get(prevIndex);

                    }

                    for (Set<String> setAfter : afterMap.keySet()) {

                        AB = afterMap.get(setAfter);

                        for (Set<String> setPrevious : previousMap.keySet()) {
                            if (setAfter.containsAll(setPrevious)) {
                                tempSet = new HashSet<String>();
                                tempSet.addAll(setAfter);
                                if (tempSet.removeAll(setPrevious)) {
                                    restSet = new HashSet<String>();
                                    restSet.addAll(tempSet);
                                    A = findSetCount(restSet, allFrequentList);
                                    B = previousMap.get(setPrevious);
                                    // AB = laterMap.get(setAfter);
                                    if (100 * AB / A >= confi_User) {
                                        System.out.println(restSet
                                                + "--->"
                                                + setPrevious
                                                + ": " + "Support = " + 100
                                                * AB / lineCounter
                                                + "%; Confidence = " + 100 * AB
                                                / A + "%");
                                    }
                                    if (100 * AB / B >= confi_User) {
                                        System.out
                                                .println(setPrevious
                                                        + "--->"
                                                        + restSet
                                                        + ": "
                                                        + "Support = "
                                                        + 100
                                                        * AB
                                                        / lineCounter
                                                        + "%; Confidence = "
                                                        + 100 * AB / B + "%");
                                    }
                                }
                            }
                        }
                    }
                    k--;
                }
            }
        }
    }



    /**
     * find a set's count number in frequent Item sets
     * 
     * @param restSet
     * @return
     */
    private double findSetCount(Set<String> restSet,
            List<Map<Set<String>, Double>> allFrequentList) {
        int setSize = restSet.size();
        Map<Set<String>, Double> targetMap = allFrequentList.get(setSize - 1);
        return targetMap.get(restSet);

    }

    /**
     * a mini method to take the floor of number/2
     * 
     * @param k
     * @return
     */
    private int floorDivTwo(int k) {
        if (k % 2 != 0) {
            return (k - 1) / 2;
        }
        return k / 2;
    }

    /**
     * scan DataBase to count candidate, illiminate < support elements
     * 
     * @param fileLocation
     * @param candidate
     * @return
     */
    private Map<Set<String>, Double> candidateAfterCount(String fileLocation,
            List<Set<String>> candidate) {
        String line;
        String[] allItems;
        String[] item;
        String tempInt;
        Set<String> readFile;
        Set<String> readSet;
        int transectionNumber = 0;

        Map<Set<String>, Double> c = new HashMap<Set<String>, Double>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileLocation));
            while ((line = br.readLine()) != null) {

                if (transectionNumber == 0) { // do nothing to line 1 since it's
                                                // the titles
                    line = br.readLine();
                }

                transectionNumber++;
                allItems = line.split("\t\t\t");

                item = allItems[1].split(" ");
                readFile = new TreeSet<String>();
                for (String ele : item) {
                    //tempInt = Integer.parseInt(ele);
                    readFile.add(ele);
                }
                readSet = new TreeSet<String>();
                for (int i = 0; i < candidate.size(); i++) {
                    readSet = candidate.get(i);

                    if (c.get(readSet) == null) {
                        c.put(readSet, 0.0);
                    }

                    if (readFile.containsAll(readSet)) {
                        c.put(readSet, c.get(readSet) + 1.0);
                    }
                }

            }

        } catch (IOException e) {

            e.printStackTrace();
        }
        return c;
    }

    /**
     * input candidate map output frequent itemSets as a list
     * 
     * @param candidate
     * @return
     */
    private List<Set<String>> getFreqFromCandidate(
            Map<Set<String>, Double> candidate,
            List<Map<Set<String>, Double>> allFrequentList) {
        double relativeSupport;
        removeList = new ArrayList<Set<String>>();
        List<Set<String>> result = new ArrayList<Set<String>>();

        for (Map.Entry<Set<String>, Double> entry : candidate.entrySet()) {
            relativeSupport = 100 * entry.getValue() / lineCounter; // convert
                                                                    // absolute
                                                                    // support
                                                                    // to
                                                                    // relative
                                                                    // support %
            if (relativeSupport < support_User) {
                removeList.add(entry.getKey());
            } else {
                result.add(entry.getKey());
            }
        }

        for (int i = 0; i < removeList.size(); i++) {
            candidate.remove(removeList.get(i));
        }

        allFrequentList.add(candidate);
        return result;
    }

    /**
     * check if the input set is in the removeList (a list including all the
     * sets that has support less then User defined)
     * 
     * @param removeList
     * @param tempSet
     * @return
     */
    private boolean isInRemoveList(List<Set<String>> removeList,
            Set<String> tempSet) {

        for (int i = 0; i < removeList.size(); i++) {
            if (tempSet.containsAll(removeList.get(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * output candidate from input frequent set
     * 
     * @param freqSet
     * @return
     */
    private List<Set<String>> candidateGen(List<Set<String>> freqSet) {
        Set<String> tempSet;
        List<Set<String>> result = new ArrayList<Set<String>>();

        for (int i = 0; i < freqSet.size(); i++) {
            for (int j = i + 1; j < freqSet.size(); j++) {
                tempSet = new HashSet<String>(freqSet.get(i));
                tempSet.addAll(freqSet.get(j));
                if (freqSet.get(i).size() == 1) {
                    result.add(tempSet);
                } else if (!isInRemoveList(removeList, tempSet)
                        && tempSet.size() == freqSet.get(i).size() + 1
                        && !isContainInResult(result, tempSet)) {

                    result.add(tempSet);
                }
            }
        }

        return result;
    }

    /**
     * check if the input set is already in the result list or not
     * 
     * @param result
     * @param tempSet
     * @return
     */
    private boolean isContainInResult(List<Set<String>> result,
            Set<String> tempSet) {
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).containsAll(tempSet)) {
                return true;
            }
        }

        return false;
    }

    /**
     * input c1 and return f1
     * 
     * @param c1
     * @return
     */
    private List<Set<String>> getFrequentListFromList(
            TreeMap<String, Double> c1,
            List<Map<Set<String>, Double>> allFrequentList) {

        ArrayList<Double> valueToRemove = new ArrayList<Double>();
        List<Set<String>> result = new ArrayList<Set<String>>();
        TreeSet<String> tempSet;
        double relativeSupport;
        Map<Set<String>, Double> f1 = new HashMap<Set<String>, Double>();

        for (Map.Entry<String, Double> entry : c1.entrySet()) {
            relativeSupport = 100 * entry.getValue() / lineCounter; // convert
                                                                    // absolute
                                                                    // support
                                                                    // to
                                                                    // relative
                                                                    // support %
            if (relativeSupport < support_User) {
                valueToRemove.add(entry.getValue());
            } else {
                tempSet = new TreeSet<String>();
                tempSet.add(entry.getKey());
                result.add(tempSet);
            }
        }

        for (int i = 0; i < valueToRemove.size(); i++) {
            c1.values().remove(valueToRemove.get(i));
        }

        for (String key : c1.keySet()) {
            Set<String> temp = new TreeSet<String>();
            temp.add(key);
            f1.put(temp, c1.get(key));
        }

        allFrequentList.add(f1);

        return result; // a list of frequent items, eg. F1
    }

    /**
     * get C1 from scanning DB
     * 
     * @param file
     * @return
     */
    private TreeMap<String, Double> scanFileToGetC1(String file) {
        String line;
        lineCounter = 0;
        String[] allItems;
        String[] item;
        String[] itemNumbers;

        TreeMap<String, Double> c1 = new TreeMap<String, Double>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            System.out
                    .println("\n***********************DATABASE**************************");
            while ((line = br.readLine()) != null) {

                if (lineCounter == 0) { // do nothing to line 1 since it's the
                                        // titles
                    System.out.println(line);
                    line = br.readLine();
                }

                System.out.println(line);
                lineCounter++;
                allItems = line.split("\t\t\t");

                item = allItems[1].split(" ");
                itemNumbers = new String[item.length];

                for (int i = 0; i < itemNumbers.length; i++) {
                    itemNumbers[i] = item[i];
                    if (c1.keySet().contains(itemNumbers[i])) {
                        c1.put(item[i], c1.get(itemNumbers[i]) + 1);
                    } else {
                        c1.put(item[i], 1.0);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//System.out.println(c1.size());
        return c1;
    }
}



