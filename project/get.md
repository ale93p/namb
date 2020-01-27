---
layout: default
title: Get NAMB
parent: Project
nav_order: 1
---

# Get NAMB

## Download

NAMB is open source and freely available on [GitHub](github.com/ale93p/namb). Head to the master branch for the last released version.

## Requirements

* Java 8 and maven environment
* Python 3

## Quick Start

> N.B. We assume a local deployment of a DSP platform is **previously configured and running**.<br/>
> For this quickstart we consider running NAMB on **Apache Flink**.
{: .text-red-000 }

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
    More info on how to ustomize the configuration files see [config](/config) 
  
4. Run the benchmark application:
     ```bash
     python3 namb.py flink
     ```
    Use -h for more options and infos.