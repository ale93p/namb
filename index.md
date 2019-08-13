---
layout: default
title: Getting Started
nav_order: 1
permalink: /
---

# What is NAMB?

**NAMB** (Not only A Micro-Benchmark) is a parametrizable and automatically generated 
benchmark application for Data Stream Processing Systems (DSP). NAMB aims to be a general 
and standardized benchmark to generate reproducible results.

## Getting Started

### NAMB Basics

To understand the basic concepts behind NAMB, why and how it was created, head to the [Concepts](/docs/concepts) page.

### Download

NAMB is open source and freely available on [GitHub](github.com/ale93p/namb). Head to the master branch for the last released version.

### Usage

1. Clone the repository and move in it

2. Compile the project:
     ```bash
     python3 namb.py build
     ```
3. Customize the configuration files (check the [configurations doc](/docs/configurations))
4. Run the benchmark on the selected platform. e.g. flink:
     ```bash
     python3 namb.py flink
     ```
    Use -h for more options.