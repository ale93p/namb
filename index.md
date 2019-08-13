---
layout: default
title: What is NAMB?
nav_order: 1
permalink: /
---

# What is NAMB?

**NAMB** (Not only A Micro-Benchmark) is a parametrizable and automatically generated 
benchmark application for Data Stream Processing Systems (DSP). NAMB aims to be a general 
and standardized benchmark to generate reproducible results.

## The Idea

### Why?
Considering that _common_ industrial benchmarks focus field-specific tasks (such IoT or CEP), they are
(such it should be) hardly reusable in different contexts; also, the _most-used_ micro-benchmark applications
are rarely described in literature, so that to leave their implementation totally dependand by the author
of a benchmark, thus make a comparison between different solutions results very difficult.

### How?
We base our implementation on a definition of several fundamental characteristics common to DSP applications,
and which, in our opinion, have an important impact over the characterization of an application workload.
We then abstract these characteristics in a set of parameters configurable by the user. 

### Objectives
Given these configurations, YAMB will __automatically__ create a DSP application. 
The challenges the project overcome are:
* **Ease-of-use**: the configuration given shall not be over-complicated, but give the minimum set of parameters that allows the best application definition flexibility.
* **Realistic**: the definition of the parameters has to make the benchmark results, over different systems/infrastructures, comparable **without the need of reproducing it**.
* **Cross-Platform**:
* **Availability**: the software is made available open-source.