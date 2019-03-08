# YAMB

[![CircleCI](https://circleci.com/gh/ale93p/yamb.svg?style=shield&circle-token=61b5a845848493f3a460eae0c42bdc489bc63d28)](https://circleci.com/gh/ale93p/yamb)
![GitHub tag (latest by date)](https://img.shields.io/github/tag-date/ale93p/yamb.svg?label=latest)


**YAMB** (__Y__*et* __A__*nother* __M__*icro-*__B__*enchmark*) is a *parametrizable* and *automatically generated* benchmark
application for Data Stream Processing Systems (DSP). 
YAMB aims to be a general and standardized benchmark to generate reproducible results.

## Features

* Simple YAML-based configuration for parameters definition
* Automatic application generation, _no need to manually touch the platform APIs_
* Ready-to-use python script to deploy the benchmark application
* Supported platforms:

    | platform | version |
    |------|----|
    | [Apache Flink](https://flink.apache.org/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.apache.flink/flink-java/1.7.svg?style=flat-square) |
    | [Apache Storm](https://storm.apache.org/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.apache.storm/storm-core/1.2.svg?style=flat-square) |
    | [Apache Heron](https://apache.github.io/incubator-heron/) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/com.twitter.heron/heron-api/0.17.svg?style=flat-square) | 

## Usage

1. Clone the repository in a local folder {YAMB_DIR}

2. Compile the project:
     ```bash
     python3 yamb.py build
     ```
3. Customize the configuration files (see: [doc/configurations](#)): 
5. Run the benchmark on your platform. e.g. flink:
     ```bash
     python3 yamb.py flink
     ```
    Use -h for more options and informations or visit [doc/benchmarks](#).

## Acknowledgements

--
