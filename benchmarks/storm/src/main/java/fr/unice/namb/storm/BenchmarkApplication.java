package fr.unice.namb.storm;


import fr.unice.namb.utils.ConfigDefaults.Balancing;
import fr.unice.namb.utils.ConfigDefaults.Distribution;
import fr.unice.namb.utils.ConfigParser;
import fr.unice.namb.utils.ConfigScheme;
import fr.unice.namb.storm.bolts.BusyWaitBolt;
import fr.unice.namb.storm.spouts.TextSpout;
import fr.unice.namb.utils.StormConfigScheme;
import fr.unice.namb.utils.StormConfigScheme.StormDeployment;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichSpout;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class BenchmarkApplication {

    private static String nambConfFileName = "default.yml";
    private static String stormConfFileName = "storm-benchmark.yml";

    private static boolean checkConf(ConfigScheme conf){
        return true;
    }

    //TODO: this is just a dummy implementation
    //TODO: the increasing, decreasing and bell funcitons must be better ingegnerized
    private static int computeNextProcessing(int actual, Balancing balancing) throws Exception{
        switch (balancing){
            case balanced:
                return actual;
            case increasing:
                return (int)(actual * 1.2);
            case decresing:
                return (int)(actual * 0.8);
            case bell:
            default:
                throw new Exception("case " + balancing + " not yet implemented");
        }
    }

    //TODO: this is basic implementation
    //TODO: to be improved after implementing balancing on scalability configuration
    private static ArrayList<Integer> computeComponentsParallelism(int parallelism, int spouts, int bolts) throws ArithmeticException{
        ArrayList<Integer> componentsParallelism = new ArrayList<>();
        int totComponents = spouts + bolts;
        int remainingExecutors = parallelism%totComponents;
        int basePar = parallelism / totComponents;
        int par;
        for(int i=0; i<totComponents; i++){
            componentsParallelism.add( (remainingExecutors==0) ? basePar : basePar + 1 );
            remainingExecutors--;
        }
        if (componentsParallelism.size() != totComponents){
            throw new ArithmeticException("Error computing components parallelism: final array length mismatch");
        }
        return componentsParallelism;
    }

    private static TopologyBuilder buildBenchmarkTopology(ConfigScheme conf) throws Exception{
        // General configurations
        int depth = conf.getDataflow().getDepth();
        int totalParallelism = conf.getDataflow().getScalability().getParallelism();

        // Spout configurations
        int numberOfSpouts = 1; //TODO: to be obtained by configuration
        int dataSize = conf.getData_stream().getSynthetic().getData_size();
        Distribution distribution = conf.getData_stream().getSynthetic().getArrival_distribution();
        int rate = conf.getData_stream().getSynthetic().getArrival_rate();

        // Bolts configurations
        int numberOfBolts = depth - numberOfSpouts; //TODO: to add possibility of more bolts in the same tree level
        int cycles = conf.getDataflow().getWorkload().getProcessing();
        Balancing balancing = conf.getDataflow().getWorkload().getBalancing();
        boolean reliability = conf.getDataflow().isMessage_reliability();

        /*
        TODO:   for the moment is correct, but spouts and bolts it's not always like this
        TODO:   spouts can be more than one given the topology shape
        TODO:   bolts can be different than depth - 1
        TODO:   (i.e. it should be - #spouts + #bolts on the same level, in case of different topology shapes)
        */
        ArrayList<Integer> componentsParallelism = computeComponentsParallelism(totalParallelism, numberOfSpouts, numberOfBolts);
        Iterator<Integer> cpIterator = componentsParallelism.iterator();
        ArrayList<String> spoutsList = new ArrayList<>();
        ArrayList<String> boltsList = new ArrayList<>();

        TopologyBuilder builder = new TopologyBuilder();

        for(int i=1; i<=numberOfSpouts; i++) {
            String spoutName = "spout_" + i;
            spoutsList.add(spoutName);
            builder.setSpout(spoutName,  new TextSpout(dataSize, distribution, rate), cpIterator.next());
        }
        for(int i=1; i<depth; i++){ //TODO: must handle multiple bolts on the same detph level
            String boltName = "bolt_" + i;
            boltsList.add(boltName);
            cycles = computeNextProcessing(cycles, balancing);
            if (i==1) {
                builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability))
                        .shuffleGrouping(spoutsList.get(0));
            }
            else{
                builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability))
                        .shuffleGrouping(boltsList.get(i-2));
            }
        }
        return builder;
    }

    public static void main (String[] args) throws Exception{

        String confPath = args[0];
        String nambConfFilePath = confPath + "/" + nambConfFileName;
        String stormConfFilePath = confPath + "/" + stormConfFileName;

        // Obtaining Configurations
        ConfigScheme benchConf = ConfigParser.parseNambConfigurationFile(new File(nambConfFilePath));

        if (checkConf(benchConf)) {

            System.out.println(benchConf.getDataflow().isTraffic_balancing());

            TopologyBuilder builder = buildBenchmarkTopology(benchConf);
            if (builder != null) {
                StormConfigScheme stormConf = ConfigParser.parseStormConfigurationFile(new File(stormConfFilePath));

                Config conf = new Config();
                conf.setNumWorkers(stormConf.getWorkers());

                if (stormConf.getDeployment() == StormDeployment.local){
                    System.out.println("RUNNING IN LOCAL");
                    LocalCluster cluster = new LocalCluster();
                    cluster.submitTopology("local-testing", conf, builder.createTopology());
                    Thread.sleep(100000); //100s of test duration
                    cluster.shutdown();
                }
                else{
                    System.out.println("RUNNING IN CLUSTER MODE");
                    String topologyName = "namb_bench_" + System.currentTimeMillis();
                    StormSubmitter.submitTopologyWithProgressBar(topologyName, conf, builder.createTopology());
                }
            } else {
                throw new Exception("Something went wrong during the application deployment");
            }
        } else{
            throw new Exception("Something went wrong during configuration checking");
        }
    }
}

