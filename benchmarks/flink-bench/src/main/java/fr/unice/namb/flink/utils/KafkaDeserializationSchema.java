package fr.unice.namb.flink.utils;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class KafkaDeserializationSchema implements DeserializationSchema<Tuple4<String, String, Long, Long>>, SerializationSchema<String> {
    private static final long serialVersionUID = 1L;

    private transient Charset charset;
    private long counter = 0;


    public KafkaDeserializationSchema() {
        this(StandardCharsets.UTF_8);
    }

    public KafkaDeserializationSchema(Charset charset) {
        this.charset = checkNotNull(charset);
    }

    public Charset getCharset() {
        return charset;
    }

    // ------------------------------------------------------------------------
    //  Kafka Serialization
    // ------------------------------------------------------------------------

    @Override
    public Tuple4<String, String, Long, Long> deserialize(byte[] message) {
        return new Tuple4<>(new String(message, charset), UUID.randomUUID().toString(), ++counter, System.currentTimeMillis());
    }

    @Override
    public boolean isEndOfStream(Tuple4<String, String, Long, Long> nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(String element) {
        return element.getBytes(charset);
    }

    @Override
    public TypeInformation<Tuple4<String, String, Long, Long>> getProducedType() {
        return TypeInformation.of(new TypeHint<Tuple4<String, String, Long, Long>>(){});
    }

    // ------------------------------------------------------------------------
    //  Java Serialization
    // ------------------------------------------------------------------------

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(charset.name());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String charsetName = in.readUTF();
        this.charset = Charset.forName(charsetName);
    }
}
