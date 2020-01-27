<p align="center" style="text-align:center">
  <h1 align="center">
  <img alt="namb logo" src="https://i.imgur.com/TzDokzP.png" width="80" align="middle"/>
    namb
  </h1>
</p>

<p align="center" style="text-align:center">
  <a href="https://circleci.com/gh/ale93p/namb">
    <img alt="Circle CI" src="https://circleci.com/gh/ale93p/namb.svg?style=svg&circle-token=61b5a845848493f3a460eae0c42bdc489bc63d28"/>
  </a>
  <img alt="GitHub tag (latest SemVer pre-release)" src="https://img.shields.io/github/v/tag/ale93p/namb?label=release&sort=semver">
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

## Requirements

* Java 8 and maven environment
* Python 3

## Quick Start

> **N.B. We assume a local deployment of a DSP platform is previously configured and running.**<br/>
> **For this quickstart we consider running NAMB on Apache Flink.**

1. Clone the repository in a local folder and move in it

2. Compile the project:
     ```bash
     python3 namb.py build
     ```
   or directly with maven:
     ```bash
     mvn clean install
     ```   
3. Prepare NAMB environment
    + Copy the default NAMB configuration file to the main configuration folder:
     ```bash
     cp conf/defaults/workflow_schema.yml conf/namb.yml
     ```   
    + Copy the platform-specific configurations to the main configuratino folder:
     ```bash
     cp conf/defaults/flink-benchmark.yml conf/flink-benchmark.yml
     ```   
    More info on how to ustomize the configuration files see [doc/configurations](https://ale93p.github.io/namb/config) 
  
4. Run the benchmark application:
     ```bash
     python3 namb.py flink
     ```
    Use -h for more options and infos.
    
## Publications

[[pagliari2019workflow](https://hal.archives-ouvertes.fr/hal-02371215/)] A. Pagliari, F. Huet, G. Urvoy-Keller, "Towards a High-Level Description for Generating Stream Processing Benchmark Applications", IEEE BigData, 3rd IEEE BPOD Workshop, 2019
