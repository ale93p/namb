<p align="center">
    <h1> NAMB </h1>
</p>

[![CircleCI](https://circleci.com/gh/ale93p/namb.svg?style=svg&circle-token=61b5a845848493f3a460eae0c42bdc489bc63d28)](https://circleci.com/gh/ale93p/namb)
![GitHub tag (latest by date)](https://img.shields.io/github/release/ale93p/namb)


**NAMB** (__N__*ot* only __A__ __M__*icro-*__B__*enchmark*) is a *parametrizable* and *automatically generated* benchmark
application for Data Stream Processing Systems (DSP). 
NAMB aims to be a general and standardized benchmark to generate reproducible results.

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
