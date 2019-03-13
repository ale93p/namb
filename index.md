---
layout: default
title: Home
nav_order: 1
permalink: /
---

# What is YAMB?

**YAMB** (Yet Another Micro-Benchmark) is a parametrizable and automatically generated benchmark
 application for Data Stream Processing Systems (DSP). YAMB aims to be a general and standardized
 benchmark to generate reproducible results.

## Getting Started

### YAMB Basics

To understand the basic concepts behind YAMB, why and how it was created, head to the [Concepts](/docs/concepts) page.

### Download

YAMB is open source and freely available on [GitHub](github.com/ale93p/yamb). Head to the master branch for the last stable version.

### Usage

1. Clone the repository and move in it

2. Compile the project:
     ```bash
     python3 yamb.py build
     ```
3. Customize the configuration files (check the [configurations doc](/docs/configurations))
4. Run the benchmark on the selected platform. e.g. flink:
     ```bash
     python3 yamb.py flink
     ```
    Use -h for more options.
