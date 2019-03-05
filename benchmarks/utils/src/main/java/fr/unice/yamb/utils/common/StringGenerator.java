package fr.unice.yamb.utils.common;

import fr.unice.yamb.utils.configuration.Config;

import java.util.ArrayList;
import java.util.Arrays;

public class StringGenerator {

    private final static int firstASCIIValue = 97;
    private final static int lastASCIIValue = 122;

    private int count;
    private int length;
    private char[] currentString;
    private int pivot;
    private int characters;

    public StringGenerator(int length) {
        this.length = length;
        this.count = 0;
        this.currentString = new char[length];
        Arrays.fill(this.currentString, (char) firstASCIIValue);
        this.pivot = 0;
        this.characters = lastASCIIValue - firstASCIIValue + 1;
    }

    private ArrayList<Character> generateArrayList(int length, char value){
        ArrayList<Character> array = new ArrayList<>(length);
        for(int i=0; i<length; i++){
            array.add(value);
        }
        return array;
    }

    private String next(){
        String returnString = new String(this.currentString);
        this.pivot = 0;
        for(int i=0; i<this.length; i++){
            int value = (int) this.currentString[this.pivot] - firstASCIIValue;
            value = (value + 1) % this.characters;
            currentString[this.pivot] = (char) (value + firstASCIIValue);
            if(value == 0)
                this.pivot++;
            else break;
        }

        this.count++;
        return returnString;
    }

    public ArrayList<byte[]> generatePayload(int dataValues, Config.DataBalancing balancing){
        String nextString;
        ArrayList<byte[]> payloadArray = new ArrayList<>();
        for(int i=0; i< dataValues; i++) { //can this be optimized?
            nextString = this.next();
            payloadArray.add(nextString.getBytes());
            if(balancing == Config.DataBalancing.unbalanced){
                for(int j=1; i<Math.pow(2,i); i++){
                    payloadArray.add(nextString.getBytes());
                }
            }
        }
        return payloadArray;
    }
}
