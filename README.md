<h1 align="center"> namb </h1>

<p align="center" style="text-align:center">
  <a href="https://circleci.com/gh/ale93p/namb">
    <img alt="Circle CI" src="https://circleci.com/gh/ale93p/namb.svg?style=svg&circle-token=61b5a845848493f3a460eae0c42bdc489bc63d28"/>
  </a>
  <img alt="GitHub release (latest by date including pre-releases)" src="https://img.shields.io/github/v/release/ale93p/namb?include_prereleases">
</p>




     
<p align="center">
    <b>NAMB</b> (<b>N</b><i>ot only</i> <b>A M</b><i>icro-</i><b>B</b><i>enchmark</i>) is a <i>parametrizable</i> and <i>automatically generated</i> benchmark
application for Data Stream Processing Systems (DSP). 
</p>

## Features

* Simple YAML-based configuration for parameters definition
* Automatic application generation, _no need to manually touch the platform APIs_
* Ready-to-use python script to deploy the benchmark application
* Supported platforms:

    | platform | version |
    |------|----|
    | [Apache Flink](https://flink.apache.org/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.apache.flink/flink-java/1.7?style=flat-square) |
    | [Apache Storm](https://storm.apache.org/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.apache.storm/storm-core/1.2?style=flat-square) |
    | [Apache Heron](https://apache.github.io/incubator-heron/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/com.twitter.heron/heron-api/0.17.svg?style=flat-square) | 

## Usage

1. Clone the repository and move in it

2. Compile the project:
     ```bash
     python3 namb.py build
     ```
3. Customize the configuration files (see: [doc/configurations](https://ale93p.github.io/namb/docs/configurations.html)): 
5. Run the benchmark on your platform. e.g. flink:
     ```bash
     python3 namb.py flink
     ```
    Use -h for more options and information.
