package fr.unice.namb.storm.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.kafka.spout.KafkaTuple;
import org.apache.storm.kafka.spout.RecordTranslator;
import org.apache.storm.tuple.Fields;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class KafkaRecordTranslator implements RecordTranslator<String, String> {

    private static final long serialVersionUID = 4678369144122009596L;
    private final Fields fields;
    private final String stream;
    private long counter;
    private int rate;
    private String me;

    public KafkaRecordTranslator(double frequency, String me) {
        this.fields = new Fields("tuple_id", "tuple_value", "ts", "num");
        this.stream = "default";
        this.counter = 0;
        this.me = me;
        if (frequency > 0) this.rate = (int) (1/frequency);
    }

    public List<Object> apply(ConsumerRecord<String, String> record) {
        String nextValue = record.value();

        if (nextValue == null) {
            return null;
        } else {
            String tuple_id = UUID.randomUUID().toString();
            ++counter;
            Long ts = System.currentTimeMillis();

            KafkaTuple ret = new KafkaTuple();
            ret.add(0, nextValue);
            ret.add(1, tuple_id);
            ret.add(2, this.counter);
            ret.add(3, ts);


            if (this.rate > 0 && this.counter % this.rate == 0){
                System.out.println("[DEBUG] [" + this.me + "] : " + tuple_id + "," + this.counter + "," + ts + "," + nextValue);
            }

            return ret.routedTo(this.stream);
        }
    }

    public Fields getFieldsFor(String stream) {
        return this.fields;
    }

    public List<String> streams() {
        return Arrays.asList(this.stream);
    }
}
