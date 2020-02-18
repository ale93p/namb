package fr.unice.namb.flink.utils;

import fr.unice.namb.utils.configuration.Config;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class BuildCommons {

    public static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, Object field, boolean apply) throws IllegalArgumentException {
        if (apply){
            switch (routing) {
                case hash:
                    if (field instanceof Integer)
                        return operator.keyBy((int) field);
                    else if (field instanceof String)
                        return operator.keyBy((String) field);
                    else
                        throw new IllegalArgumentException("Field must be <int> or <String> instead it is <" + field.getClass().getName() + ">");
                case balanced:
                    return operator.rebalance();
                case broadcast:
                    return operator.broadcast();
                case none:
                    return operator;
                default:
                    throw new IllegalArgumentException(routing + " is not a valid routing type");
            }
        }
        return operator;
    }

    public static DataStream<Tuple4<String, String, Long, Long>> setRouting(DataStream<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, Object field) throws IllegalArgumentException {
        switch (routing) {
            case hash:
                if (field instanceof Integer)
                    return operator.keyBy((int) field);
                else if (field instanceof String)
                    return operator.keyBy((String) field);
                else
                    throw new IllegalArgumentException("Field must be <int> or <String> instead it is <" + field.getClass().getName() + ">");
            case balanced:
                return operator.rebalance();
            case broadcast:
                return operator.broadcast();
            case none:
                return operator;
            default:
                throw new IllegalArgumentException(routing + " is not a valid routing type");
        }
    }

    public static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, boolean apply) throws IllegalArgumentException {
        return setRouting(operator, routing, 0, apply);
    }

    public static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing) throws IllegalArgumentException {
        return setRouting(operator, routing, 0, true);
    }

    public static DataStream<Tuple4<String, String, Long, Long>> setRouting(DataStream<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing) throws IllegalArgumentException {
        return setRouting(operator, routing, 0);
    }



    public static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, int interval, boolean applyRouting) {
        switch (type) {
            case tumbling:
                return setRouting(parent, trafficRouting, applyRouting).timeWindowAll(Time.seconds(duration));
            case sliding:
                return setRouting(parent, trafficRouting, applyRouting).timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    public static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(DataStream<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, int interval) {
        switch (type) {
            case tumbling:
                return setRouting(parent, trafficRouting).timeWindowAll(Time.seconds(duration));
            case sliding:
                return setRouting(parent, trafficRouting).timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    public static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, boolean apply){
        return setWindow(parent, trafficRouting, type, duration, 0);
    }

    public static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(DataStream<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration){
        return setWindow(parent, trafficRouting, type, duration, 0);
    }
}
