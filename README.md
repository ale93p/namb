# YAMB

**YAMB** (__Y__*et* __A__*nother* __M__*icro-*__B__*enchmark*) is an *parametrizable* and *automatically generated* benchmark
application for Data Stream Processing Systems (DSP). 
YAMB aims to be a general and standardized benchmark to generate reproducible results.

## Features

* Simple YAML-based configuration for application definition
* Automatic application generation, _no need to manually implement anything_
* Ready-to-use python script to deploy the benchmark application
* Varius platforms supported:

    | platform | version |
    |------|----|
    | [Apache Flink](https://flink.apache.org/) | 1.7.x |
    | [Apache Storm](https://storm.apache.org/) | 1.2.x |
    | [Apache Heron](https://apache.github.io/incubator-heron/) | 0.17.x | 

## Usage

1. Clone the repository in a local folder {YAMB_DIR}

2. [TODO]: a compile script is missing, for the moment you should do it manually:
     ```bash
     cd yamb
     mvn clean install -Dmaven.test.skip=true
     ```
3. Generate and customize the yamb configuration file:
     ```bash
     cp conf/defaults/default.yml conf/yamb.yml
     ```
4. Generate and customize the platform related configuration file:
    ```bash
     cp conf/defaults/{platform}-bench.yml conf/{platform}-bench.yml
     ```
5. Run the benchmarks simply using `yamb.py`:
     ```bash
     python3 yamb.py benchmark-suite
     ```
 Use -h for more options and informations.
