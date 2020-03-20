package fr.unice.namb.utils.configuration.schema;

import fr.unice.namb.utils.configuration.Config;

public class NambConfigSchema extends ConfigSchema {

    public static class Scalability{
        private int parallelism = Config.WF_SCALABILITY_PARALLELISM;
        private Config.ParaBalancing balancing = Config.WF_SCALABILITY_BALANCING;
        private double variability = Config.WF_SCALABILITY_VARIABILTY;

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

        public double getVariability() {
            return variability;
        }

        public void setVariability(double variability) {
            this.variability = variability;
        }
    }
    public static class Connection{
        private Config.ConnectionShape shape = Config.WF_CONNECTION_SHAPE;
        private Config.TrafficRouting routing = Config.WF_TRAFFIC_ROUTING;

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
        private boolean enabled = Config.WF_WINDOWING_ENABLED;
        private Config.WindowingType type = Config.WF_WINDOWING_TYPE;
        private int duration = Config.WF_WINDOW_DURATION;
        private int interval = Config.WF_WINDOW_INTERVAL;

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
        private double processing = Config.WF_WORKLOAD_PROCESSING;
        private Config.LoadBalancing balancing = Config.WF_WORKLOAD_BALANCING;

        public double getProcessing() {
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

    public static class Data{

        private int size = Config.DS_SYNTHETIC_DATA_SIZE;
        private int values = Config.DS_DATA_VALUES;
        private Config.DataDistribution distribution = Config.DS_DATA_DISTRIBUTION;

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

        public Config.DataDistribution getDistribution() {
            return distribution;
        }

        public void setDistribution(Config.DataDistribution balancing) {
            this.distribution = balancing;
        }
    }
    public static class Flow{
        private Config.ArrivalDistribution distribution = Config.DS_SYNTHETIC_ARRIVAL_DISTRIBUTION;
        
        // Maximum throughput (base throughput for burst)
        private int rate = Config.DS_SYNTHETIC_ARRIVAL_RATE;
        
        // For burst distribution
        private long duration = Config.DS_SYNTHETIC_BURST_DURATION;
        private long interval = Config.DS_SYNTHETIC_BURST_INTERVAL;
        
        
        // For sin and sawtooth distribution
        private long phase = Config.DS_SYNTETHIC_PHASE_DURATION;
        

		public Config.ArrivalDistribution getDistribution() {
            return distribution;
        }

        public void setDistribution(Config.ArrivalDistribution distribution) {
            this.distribution = distribution;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }
        
        public long getPhase() {
			return phase;
		}

		public void setPhase(long phase) {
			this.phase = phase;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

		public long getInterval() {
			return interval;
		}

		public void setInterval(long interval) {
			this.interval = interval;
		}
	
    }
    
    public static class Synthetic {
    	
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
    public static class ExternalSourceKafka{
        private String server = Config.DS_KAFKA_BOOTSTRAP_SERVER;
        private String zookeeper = Config.DS_ZOOKEEPER_SERVER;
        private String group = Config.DS_KAFKA_GROUP;
        private String topic = Config.DS_KAFKA_TOPIC;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getZookeeper() {
            return zookeeper;
        }

        public void setZookeeper(String zookeeper) {
            this.zookeeper = zookeeper;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
    public static class ZookeeperCluster{
        private String server = Config.DS_ZOOKEEPER_SERVER;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }
    }

    public static class WorkFlow {

        private int depth = Config.WF_DEPTH;
        private Scalability scalability = new Scalability();
        private Connection connection = new Connection();
        private boolean reliability = Config.WF_MESSAGE_RELIABILITY;
        private Windowing windowing = new Windowing();
        private Workload workload = new Workload();
        private double filtering = Config.WF_FILTERING;

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

        public boolean isReliability() {
            return reliability;
        }

        public void setReliability(boolean message_reliability) {
            this.reliability = message_reliability;
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

        public double getFiltering() {
            return filtering;
        }

        public void setFiltering(double filtering) {
            this.filtering = filtering;
        }
    }

    public static class DataStream {
        public static class External {

            private ExternalSourceKafka kafka = new ExternalSourceKafka();
            private ZookeeperCluster zookeeper = new ZookeeperCluster();

            public ExternalSourceKafka getKafka() {
                return kafka;
            }

            public void setKafka(ExternalSourceKafka kafka) {
                this.kafka = kafka;
            }

            public ZookeeperCluster getZookeeper() {
                return zookeeper;
            }

            public void setZookeeper(ZookeeperCluster zookeeper) {
                this.zookeeper = zookeeper;
            }
        }

	        private Synthetic synthetic = new Synthetic();
	        private External external = new External();
	
	        public External getExternal() {
	            return external;
	        }
	
	        public void setExternal(External external) {
	            this.external = external;
	        }
	
	        public NambConfigSchema.Synthetic getSynthetic() {
	            return synthetic;
	        }
	
	        public void setSynthetic(NambConfigSchema.Synthetic synthetic) {
	            this.synthetic = synthetic;
	        }
	    }

    public static class Tasks {
        private String name = null;
        private Config.ComponentType type = null;
        private long parallelism = 1;
        private Config.TrafficRouting routing = Config.TrafficRouting.none;
        private double processing = Config.WF_WORKLOAD_PROCESSING;
        private double filtering = 0;
        private boolean reliability = Config.WF_MESSAGE_RELIABILITY;
        private Windowing windowing = new Windowing();
        private Data data = new Data();
        private Flow flow = new Flow();
        private int resizeddata = 0;
        
        private ExternalSourceKafka kafka = new ExternalSourceKafka();
        
        private String[] parents = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Config.ComponentType getType() {
            return type;
        }

        public void setType(Config.ComponentType type) {
            this.type = type;
        }

        public long getParallelism() {
            return parallelism;
        }

        public void setParallelism(long parallelism) {
            this.parallelism = parallelism;
        }

        public Config.TrafficRouting getRouting() {
            return routing;
        }

        public void setRouting(Config.TrafficRouting routing) {
            this.routing = routing;
        }

        public double getProcessing() {
            return processing;
        }

        public void setProcessing(double processing) {
            this.processing = processing;
        }


        public boolean isReliability() {
            return reliability;
        }

        public void setReliability(boolean reliability) {
            this.reliability = reliability;
        }

        public double getFiltering() {
            return filtering;
        }

        public void setFiltering(double filtering) {
            this.filtering = filtering;
        }

        public Windowing getWindowing() {
            return windowing;
        }

        public void setWindowing(Windowing windowing) {
            this.windowing = windowing;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public Flow getFlow() {
            return flow;
        }

        public int getResizeddata() {
            return resizeddata;
        }

        public void setResizeddata(int resizeddata) {
            this.resizeddata = resizeddata;
        }

        public void setFlow(Flow flow) {
            this.flow = flow;
        }

        public String[] getParents() {
            return parents;
        }

        public void setParents(String[] parents) {
            this.parents = parents;
        }

        public ExternalSourceKafka getKafka() {
            return kafka;
        }

        public void setKafka(ExternalSourceKafka kafka) {
            this.kafka = kafka;
        }
    }

    public static class Pipeline{
        private Tasks tasks[] = null;

        public Tasks[] getTasks() {
            return tasks;
        }

        public void setTasks(Tasks[] tasks) {
            this.tasks = tasks;
        }
    }


	private WorkFlow workflow = new WorkFlow();
    private DataStream datastream = new DataStream();
    private Pipeline pipeline = new Pipeline();

    public void setWorkflow(WorkFlow workflow){
        this.workflow = workflow;
    }
    public void setDatastream(DataStream datastream){
        this.datastream = datastream;
    }
    public void setPipeline(Pipeline pipeline){ this.pipeline = pipeline; }

    public WorkFlow getWorkflow(){
        return this.workflow;
    }
    public DataStream getDatastream(){
        return this.datastream;
    }
    public Pipeline getPipeline() { return this.pipeline; }

}
