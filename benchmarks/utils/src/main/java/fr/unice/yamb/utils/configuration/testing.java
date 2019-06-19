package fr.unice.yamb.utils.configuration;

import java.util.Random;

public class testing {

    public static void main (String[] args) throws Exception{
//        double filtering = 0.000006;
//        int precision = 10000000;
//        Random rnd = new Random();
//        int count = 0;
//        int pass = 0;
//        while(count < precision * 10){
//            count++;
//            if (rnd.nextInt(precision) < filtering * precision) {
//                pass ++;
//            }
//        }
//        double percentage = (double) pass / (double) count;
//        System.out.println(pass + " / " + count + " = " +  percentage);

        double frequency = 0.0005;
        int rate = (int) (1/ frequency);
        System.out.println(rate);
        int count = 0;
        for(int i=0; i < 10000; i++){
            count++;
            if(count % rate == 0){
                System.out.println(count);
            }
        }
    }
}
