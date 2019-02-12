package fr.unice.namb.utils.configuration;

import fr.unice.namb.utils.configuration.ConfigDefaults.*;

public class NambConfigSchema extends ConfigSchema{

    public static class DataFlow {
        public static class Scalability{
            private int parallelism = ConfigDefaults.DF_SCALABILITY_PARALLELISM;

            public int getParallelism() {
                return parallelism;
            }

            public void setParallelism(int parallelism) {
                this.parallelism = parallelism;
            }
        }
        public static class Connection{
            private ConnectionShape shape = ConfigDefaults.DF_CONNECTION_SHAPE;
            private TrafficRouting routing = ConfigDefaults.DF_TRAFFIC_ROUTING;

            public ConnectionShape getShape() {
                return shape;
            }

            public void setShape(ConnectionShape shape) {
                this.shape = shape;
            }

            public TrafficRouting getRouting() {
                return routing;
            }

            public void setRouting(TrafficRouting routing) {
                this.routing = routing;
            }
        }
        public static class Workload{
            private int processing = ConfigDefaults.DF_WORKLOAD_PROCESSING;
            private LoadBalancing balancing = ConfigDefaults.DF_WORKLOAD_BALANCING;

            public int getProcessing() {
                return processing;
            }

            public LoadBalancing getBalancing() {
                return balancing;
            }

            public void setProcessing(int processing) {
                this.processing = processing;
            }

            public void setBalancing(LoadBalancing balancing) {
                this.balancing = balancing;
            }
        }

        private int depth = ConfigDefaults.DF_DEPTH;
        private Scalability scalability = new Scalability();
        private Connection connection = new Connection();
        private boolean reliable = ConfigDefaults.DF_MESSAGE_RELIABILITY;
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

                private int size = ConfigDefaults.DS_SYNTHETIC_DATA_SIZE;
                private int values = ConfigDefaults.DS_DATA_VALUES;
                private DataBalancing balancing = ConfigDefaults.DS_DATA_BALANCING;

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

                public DataBalancing getBalancing() {
                    return balancing;
                }

                public void setBalancing(DataBalancing balancing) {
                    this.balancing = balancing;
                }
            }
            public static class Flow{
                private Distribution distribution = ConfigDefaults.DS_SYNTHETIC_ARRIVAL_DISTRIBUTION;
                private int rate = ConfigDefaults.DS_SYNTHETIC_ARRIVAL_RATE;

                public Distribution getDistribution() {
                    return distribution;
                }

                public void setDistribution(Distribution distribution) {
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
