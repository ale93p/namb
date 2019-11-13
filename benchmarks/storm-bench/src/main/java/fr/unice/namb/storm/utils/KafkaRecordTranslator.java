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
    private long count;

    public KafkaRecordTranslator() {
        this.fields = new Fields("tuple_id", "tuple_value", "ts", "num");
        this.stream = "default";
        this.count = 0;
    }

    public List<Object> apply(ConsumerRecord<String, String> record) {
        String value = record.value();
        if (value == null) {
            return null;
        } else {
            KafkaTuple ret = new KafkaTuple();
            ret.add(1, UUID.randomUUID().toString());
            ret.add(0, value);
            ret.add(3, System.currentTimeMillis());
            ret.add(2, ++count);
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
