package fr.unice.namb.storm.spouts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

    public String next(){
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
}
