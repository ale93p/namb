---
layout: default
title: Experiments
parent: Documentation
nav_order: 4
---

# CPU Load Abstraction

## Objective

Find the CPU load from different tasks, so to define a load abstraction in the configurations.

## Methodology

### Common DSP Tasks
Defined different common CPU-intensive DSP tasks categories[1-5]:

* Identity (i.e. "Input data processing without executing any operation on them" [5])
* Transformation (e.g. Parsing)
* Filter
* Windowing (e.g. Aggregation, Sorting):
  * Aggregation
  * Ranking
* ~~Join~~ ([merged with Windowing](https://github.com/ale93p/yamb/issues/6#issuecomment-456091723))
* ~~Normalization (?)~~

### Environment
- The tests has been run in a single machine. The version of Storm used is **1.2.2**. 
- The cluster has been set with ZooKeeper, Nimbus and Supervisor co-existing in a single machine.
- Each topology has been run over two workers.
- The base input throughput (Spout generation) is `1000tuples/s`.

### Step 1
The common tasks has been tested one by one in a spout->task topology:
* Identity: `XMLSpout->IdentityBolt`
* Transformation: `XMLSpout->TransformationBolt`
* Filter: `RandomIntegerSpout->FilterBolt`
* Aggregation: `RandomIntegerSpout->AggregationBolt`
* Ranking: `RandomIntegerSpout->AggregationBolt`

### Step 2
A sample topology has been built using the implementation of the common tasks, under the form:
```
         XMLSpout
            |
      Transformation
            |
          Filter
        /        \
Aggregation     Aggregation
        \        /
          Ranking
```

### Step 3
The BusyWaitBolt has been run with different cycles values to get the CPU Load generated.

### Step 4
Given the results ([BusyWait individual](#busywait-benchmark)) of the previous step,
has been implemented a topology using only BusyWait bolts, changing the cycles number
in ordet to simulate the load seen for the topology in [step 2](#step-2).

Between paranthesis the number of thousands of cycles set to simulate the tasks:
```
         XMLSpout
            |
      Transformation
          (400)
            |
          Filter
          (0.75)
        /        \
Aggregation     Aggregation
  (0.80)          (0.80)
        \        /
          Ranking
           (100)
```

### Step 5

The two topologies has been then tested with different input throughputs to confirm the consistency on the CPU load.

## Findings

The CPU Load is represented as **ms of CPU time / 1000 tuples**.

### Common Tasks Benchmarks

| Common tasks individual  | Common tasks topology |
|-------------------|------------------|
|![Common individual]({{ site.baseurl }}{% link img/plots/common_individual_boxplot.png %})| ![Common full topology]({{ site.baseurl }}{% link img/plots/common_full_topo_boxplot.png %})|

Small and not relevant difference when running in full topology.

### BusyWait Benchmark

| BusyWait individual | BusyWait simulated topology |
|-------------------|------------------|
|![Busywait individual]({{ site.baseurl }}{% link img/plots/busywait_individual_boxplot.png %})| ![Busywait full topology]({{ site.baseurl }}{% link img/plots/busywait_full_topo_boxplot.png %})|

In the figure on the left, the number in the x axis represent the number of thousand of cycles with
which the BusyWait bolt was configured for the test.

In the figure on the right we can see that using the parameters previously described,
the simulated tasks replicates the same CPU Load than the real tasks.

### Comparison with different input throughput

| Input Tput (t/s) | Common Tasks Topology | BusyWait Simulated Topology |
|------------------|-----------------------|-----------------------------|
|   10 | ![Common full topology 10]({{ site.baseurl }}{% link img/plots/common_10_full_topo_boxplot.png %})| ![Busywait full topology 10]({{ site.baseurl }}{% link img/plots/busywait_10_full_topo_boxplot.png %})|
|  100 | ![Common full topology 100]({{ site.baseurl }}{% link img/plots/common_100_full_topo_boxplot.png %})| ![Busywait full topology 10]({{ site.baseurl }}{% link img/plots/busywait_100_full_topo_boxplot.png %})|
| 1000 | ![Common full topology 1000]({{ site.baseurl }}{% link img/plots/common_full_topo_boxplot.png %})| ![Busywait full topology 10]({{ site.baseurl }}{% link img/plots/busywait_full_topo_boxplot.png %})|

## Abstraction

A possible abstraction for the CPU load can then be the number of _thousands of cycles_ for the BusyWait. We could also
define three load values, under a literal form, that correspond to predefined cycles values, so to be directly and easily
configured by the user:

| Literal Load | Corresponding K cycles | Motivation |
|--------------|------------------------|------------|
| **lower** | 0,5 | slightly less than _aggregation_ |
| **medium** | 150 | almost _ranking_ |
| **higher** | 500 | slightly more than _transformation_ |


## References

[1] B. Peng et al., **R-Storm: Resource-Aware Scheduling in Storm**, ACM Middleware 2015

[2] A. Shukla et al., **RIoTBench: An IoT benchmark for distributed stream processing systems**, Wiley Concurrency Computation 2017

[3] G. Hesse et al., **Senska - Towards an Enterprise Streaming Benchmark**, Springer TPCTC 2017

[4] S. Chatterjee, C. Morin, **Experimental Study on the Performance and Resource Utilization of Data Streaming Frameworks**, IEEE/ACM CCGrid 2018

[5] M. Cermak et al., **A Performance Benchmark for NetFlow Data Analysis on Distributed Stream Processing Systems**, IEEE/IFIP NOMS 2016
