package fr.unice.namb.utils.configuration;

import fr.unice.namb.utils.configuration.ConfigDefaults.*;

public class ConfigScheme {

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

            public ConnectionShape getShape() {
                return shape;
            }

            public void setShape(ConnectionShape shape) {
                this.shape = shape;
            }
        }
        public static class Workload{
            private int processing = ConfigDefaults.DF_WORKLOAD_PROCESSING;
            private Balancing balancing = ConfigDefaults.DF_WORKLOAD_BALANCING;

            public int getProcessing() {
                return processing;
            }

            public Balancing getBalancing() {
                return balancing;
            }

            public void setProcessing(int processing) {
                this.processing = processing;
            }

            public void setBalancing(Balancing balancing) {
                this.balancing = balancing;
            }
        }

        private int depth = ConfigDefaults.DF_DEPTH;
        private Scalability scalability = new Scalability();
        private Connection connection = new Connection();
        private boolean traffic_balancing = ConfigDefaults.DF_TRAFFIC_BALANCING;
        private boolean message_reliability = ConfigDefaults.DF_MESSAGE_RELIABILITY;
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

        public boolean isTraffic_balancing() {
            return traffic_balancing;
        }

        public void setTraffic_balancing(boolean traffic_balancing) {
            this.traffic_balancing = traffic_balancing;
        }

        public boolean isMessage_reliability() {
            return message_reliability;
        }

        public void setMessage_reliability(boolean message_reliability) {
            this.message_reliability = message_reliability;
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
            private int data_size = ConfigDefaults.DS_SYNTHETIC_DATA_SIZE;
            private DataType data_type = ConfigDefaults.DS_SYNTHETIC_DATA_TYPE;
            private Distribution arrival_distribution = ConfigDefaults.DS_SYNTHETIC_ARRIVAL_DISTRIBUTION;
            private int arrival_rate = ConfigDefaults.DS_SYNTHETIC_ARRIVAL_RATE;

            public int getArrival_rate() {
                return arrival_rate;
            }

            public void setArrival_rate(int arrival_rate) {
                this.arrival_rate = arrival_rate;
            }

            public int getData_size() {
                return data_size;
            }

            public void setData_size(int data_size) {
                this.data_size = data_size;
            }

            public DataType getData_type() {
                return data_type;
            }

            public void setData_type(DataType data_type) {
                this.data_type = data_type;
            }

            public Distribution getArrival_distribution() {
                return arrival_distribution;
            }

            public void setArrival_distribution(Distribution arrival_distribution) {
                this.arrival_distribution = arrival_distribution;
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

    private int total_test_duration = ConfigDefaults.TOTAL_TEST_DURATION;
    private DataFlow dataflow = new DataFlow();
    private DataStream data_stream = new DataStream();

    public void setTotal_test_duration(int total_test_duration){
        this.total_test_duration = total_test_duration;
    }
    public void setDataflow(DataFlow dataflow){
        this.dataflow = dataflow;
    }
    public void setData_stream(DataStream data_stream){
        this.data_stream = data_stream;
    }

    public int getTotal_test_duration(){
        return this.total_test_duration;
    }
    public DataFlow getDataflow(){
        return this.dataflow;
    }
    public DataStream getData_stream(){
        return this.data_stream;
    }

}
