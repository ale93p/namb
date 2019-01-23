# Tests Section

Where testing is made

## CPU

### Objective

Find the CPU load from different tasks, so to define a load abstraction in the configurations.

### Methodology

Defined different common CPU-intensive DSP tasks categories[1-5]:

* Identity (i.e. "Input data processing without executing any operation on them" [5])
* Transformation (e.g. Parsing)
* Filter
* Windowing (e.g. Aggregation, Sorting):
  * Aggregation
  * Ranking
* ~~Join~~ ([merged with Windowing](https://github.com/ale93p/namb/issues/6#issuecomment-456091723))
* ~~Normalization (?)~~

These task have then been imolemented in a testing Storm topology and run in local mode to analyze their CPU consumption.

The tests has been run in a single machine. The version of Storm used is **1.2.2**. 
The cluster has been set with ZooKeeper, Nimbus and Supervisor co-existing in a single machine.
Each topology has been run over two workers:
* individual tasks benchmarks has been run in a spout->bolt manner.
* full topology tasks has been run as:
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

### Findings

Yet to do...

### Abstraction

Yet to do...

### References

[1] B. Peng et al., **R-Storm: Resource-Aware Scheduling in Storm**, ACM Middleware 2015

[2] A. Shukla et al., **RIoTBench: An IoT benchmark for distributed stream processing systems**, Wiley Concurrency Computation 2017

[3] G. Hesse et al., **Senska - Towards an Enterprise Streaming Benchmark**, Springer TPCTC 2017

[4] S. Chatterjee, C. Morin, **Experimental Study on the Performance and Resource Utilization of Data Streaming Frameworks**, IEEE/ACM CCGrid 2018

[5] M. Cermak et al., **A Performance Benchmark for NetFlow Data Analysis on Distributed Stream Processing Systems**, IEEE/IFIP NOMS 2016
