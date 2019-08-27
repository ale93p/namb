---
layout: default
title: What is NAMB?
nav_order: 1
permalink: /
---

# What is NAMB?

**NAMB** (Not only A Micro-Benchmark) is a parametrizable generator of 
benchmark application for Data Stream Processing Systems (DSP).

### Why?
Considering that _common_ industrial benchmarks focus field-specific tasks (such IoT or CEP), they are
(such it should be) hardly reusable in different contexts; also, the _most-used_ micro-benchmark applications
are rarely described in literature, so that to leave their implementation totally dependand by the author
of a benchmark, thus make a comparison between different solutions results very difficult.

### How?
We base our implementation on a definition of several fundamental characteristics common to DSP applications,
and which, in our opinion, have an important impact over the characterization of an application workload.
We then abstract these characteristics in a set of parameters configurable by the user. 

### Features
Given these configurations, YAMB will __automatically__ create a DSP application. 
NAMB main features and properties are:
* **Cross-Platform**: NAMB is a modularized framework able to generate the benchmark for multiple platforms using the same configurations set.
* **Realistic**: more than generating *only micro-benchmarks*, NAMB is able to simulate and reproduce real applications.
* **Ease-of-use**: the configuration given is simple and intuitive, giving the minimum set of parameters that allows the best application definition flexibility.
* **Availability**: the software is made available to everyone through a GitHub repository; everyone is allowed to use it, test it and improve it.