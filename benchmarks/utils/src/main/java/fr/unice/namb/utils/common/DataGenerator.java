package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DataGenerator implements Serializable{

    private final static int firstASCIIValue = 97;
    private final static int lastASCIIValue = 122;

    private int dataSize;
    private int dataValues;
    private char[] currentString;
    private int pivot;
    private int characters;
    private ArrayList<byte[]> payload;
    private Config.DataDistribution distribution;

    public DataGenerator(Data conf) {
        this.dataSize = conf.getSize();
        this.dataValues = conf.getValues();
        this.currentString = new char[this.dataSize];
        Arrays.fill(this.currentString, (char) firstASCIIValue);
        this.pivot = 0;
        this.characters = lastASCIIValue - firstASCIIValue + 1;
        this.payload = generatePayload();
        this.distribution = conf.getDistribution();
    }

    private String next(){
        String returnString = new String(this.currentString);
        this.pivot = 0;
        for(int i=0; i<this.dataSize; i++){
            int value = (int) this.currentString[this.pivot] - firstASCIIValue;
            value = (value + 1) % this.characters;
            currentString[this.pivot] = (char) (value + firstASCIIValue);
            if(value == 0)
                this.pivot++;
            else break;
        }

        return returnString;
    }

    private ArrayList<byte[]> generatePayload(){
        String nextString;
        ArrayList<byte[]> payloadArray = new ArrayList<>();
        for(int i=0; i< this.dataValues; i++) {
            nextString = this.next();
            payloadArray.add(nextString.getBytes());
        }
        return payloadArray;
    }

    private int generateIndex() throws Exception{
        Random rnd =  new Random();

        switch(this.distribution){
            case uniform: {
                return rnd.nextInt(this.dataValues);
            }
            case nonuniform: {
                int index;
                int mean = this.dataValues / 2;
                int stdev = this.dataValues / 5;
                do {
                    index = (int) Math.round(mean + rnd.nextGaussian() * stdev);
                }while(index < 0 || index > this.dataValues);
                return index;
            }
            default:
            {
                throw new Exception("Unknown Distribution Type <" + this.distribution + ">");
            }
        }

    }

    public byte[] getNextValue() throws Exception{
        return payload.get(generateIndex());
    }
}
