package fr.unice.yamb.utils.configuration.schema;

import fr.unice.yamb.utils.configuration.Config;

import java.awt.*;

public class YambConfigSchema extends ConfigSchema {

    public static class DataFlow {
        public static class Scalability{
            private int parallelism = Config.DF_SCALABILITY_PARALLELISM;
            private Config.ParaBalancing balancing = Config.DF_SCALABILITY_BALANCING;

            public int getParallelism() {
                return parallelism;
            }

            public void setParallelism(int parallelism) {
                this.parallelism = parallelism;
            }

            public Config.ParaBalancing getBalancing() {
                return balancing;
            }

            public void setBalancing(Config.ParaBalancing balancing) {
                this.balancing = balancing;
            }
        }
        public static class Connection{
            private Config.ConnectionShape shape = Config.DF_CONNECTION_SHAPE;
            private Config.TrafficRouting routing = Config.DF_TRAFFIC_ROUTING;

            public Config.ConnectionShape getShape() {
                return shape;
            }

            public void setShape(Config.ConnectionShape shape) {
                this.shape = shape;
            }

            public Config.TrafficRouting getRouting() {
                return routing;
            }

            public void setRouting(Config.TrafficRouting routing) {
                this.routing = routing;
            }
        }
        public static class Windowing{
            private boolean enabled = Config.DF_WINDOWING_ENABLED;
            private Config.WindowingType type = Config.DF_WINDOWING_TYPE;
            private int duration = Config.DF_WINDOW_DURATION;
            private int interval = Config.DF_WINDOW_INTERVAL;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Config.WindowingType getType() {
                return type;
            }

            public void setType(Config.WindowingType type) {
                this.type = type;
            }

            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }

            public int getInterval() {
                return interval;
            }

            public void setInterval(int interval) {
                this.interval = interval;
            }
        }
        public static class Workload{
            private float processing = Config.DF_WORKLOAD_PROCESSING;
            private Config.LoadBalancing balancing = Config.DF_WORKLOAD_BALANCING;

            public float getProcessing() {
                return processing;
            }

            public Config.LoadBalancing getBalancing() {
                return balancing;
            }

            public void setProcessing(float processing) {
                this.processing = processing;
            }

            public void setBalancing(Config.LoadBalancing balancing) {
                this.balancing = balancing;
            }
        }

        private int depth = Config.DF_DEPTH;
        private Scalability scalability = new Scalability();
        private Connection connection = new Connection();
        private boolean reliable = Config.DF_MESSAGE_RELIABILITY;
        private Windowing windowing = new Windowing();
        private Workload workload = new Workload();

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public Scalability getScalability() {
            return scalability;
        }

        public void setScalability(Scalability scalability) {
            this.scalability = scalability;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public boolean isReliable() {
            return reliable;
        }

        public void setReliable(boolean message_reliability) {
            this.reliable = message_reliability;
        }

        public Windowing getWindowing() {
            return windowing;
        }

        public void setWindowing(Windowing windowing) {
            this.windowing = windowing;
        }

        public Workload getWorkload() {
            return workload;
        }

        public void setWorkload(Workload workload) {
            this.workload = workload;
        }
    }

    public static class DataStream {
        public static class Synthetic {
            public static class Data{

                private int size = Config.DS_SYNTHETIC_DATA_SIZE;
                private int values = Config.DS_DATA_VALUES;
                private Config.DataBalancing balancing = Config.DS_DATA_BALANCING;

                public int getSize() {
                    return size;
                }

                public void setSize(int size) {
                    this.size = size;
                }

                public int getValues() {
                    return values;
                }

                public void setValues(int values) {
                    this.values = values;
                }

                public Config.DataBalancing getBalancing() {
                    return balancing;
                }

                public void setBalancing(Config.DataBalancing balancing) {
                    this.balancing = balancing;
                }
            }
            public static class Flow{
                private Config.Distribution distribution = Config.DS_SYNTHETIC_ARRIVAL_DISTRIBUTION;
                private int rate = Config.DS_SYNTHETIC_ARRIVAL_RATE;

                public Config.Distribution getDistribution() {
                    return distribution;
                }

                public void setDistribution(Config.Distribution distribution) {
                    this.distribution = distribution;
                }

                public int getRate() {
                    return rate;
                }

                public void setRate(int rate) {
                    this.rate = rate;
                }
            }

            private Data data = new Data();
            private Flow flow = new Flow();

            public Data getData() {
                return data;
            }

            public void setData(Data data) {
                this.data = data;
            }

            public Flow getFlow() {
                return flow;
            }

            public void setFlow(Flow flow) {
                this.flow = flow;
            }
        }

        //TODO: implement external data source

        private Synthetic synthetic = new Synthetic();

        public Synthetic getSynthetic() {
            return synthetic;
        }

        public void setSynthetic(Synthetic synthetic) {
            this.synthetic = synthetic;
        }
    }

    private DataFlow dataflow = new DataFlow();
    private DataStream datastream = new DataStream();

    public void setDataflow(DataFlow dataflow){
        this.dataflow = dataflow;
    }
    public void setDatastream(DataStream datastream){
        this.datastream = datastream;
    }

    public DataFlow getDataflow(){
        return this.dataflow;
    }
    public DataStream getDatastream(){
        return this.datastream;
    }

}
