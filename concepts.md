---
layout: default
title: Concepts
parent: Getting Started
nav_order: 1
---
# Concepts
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

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

## Fundamental Characteristics
A DSP application can be seen as composed by two major components: 
the **data stream** that defines the input flow; 
and the **Dataflow** that describe the application tasks composition.
Both of this components has several fundamental characteristics that defines the behaviour of the entire application.

### Data Stream
* **Data Characteristics**<br/>
The flow of data arriving in input is normally of two kinds: **numerical** or **text**;
and the size of a single data unit can consequently vary.
Especially textual flows, where we can find different formats: _json_, _xml_, _plain text_, and so on.

* **Input Ratio**<br/> 
The ratio at which the data arrive isn't always constant, it may vary depending on the context.
For example, social network usage is higher in mornings and evenings, sensors-networks may have random peak moments.

### Dataflow
* **DAG Depth**<br/>
A DSP application can be representes with a Directed Acyclic Graph (DAG), composed by a sequence of tasks.
This sequence can be seen as a tree, so to give to the application a tree depth.

* **Scalability**<br/>
One main characteristic of these middlewares is to perform tasks in a distributed (and parallel) manner.
For that reason each single task can be parallelized and replicated to scale out the application performances.

* **Connection**<br/>
The flow of data between tasks can follow different kinds of topologies. From linear applications that simply execute
one task after the another, to more complex trees where the data is duplicated between more paths, or different paths
rejoin to a single one.

* **Traffic Balancing**<br/>
When the flow splits in more pathways, or when a single task is parallelized, the balancing of the data over
the several available paths not always is fully balanced. It may happen that some tasks receives more data than
the others, that can be cause by a filtering task or by an hash routing.

* **Message Reliability**<br/>
Most DSP allow the enabling of a reliability mechanism to ensure message processing.

* **Workload**<br/>
Each task by which the DAG is composed has to process the data, based on queries and instruction given at implementation.
Between these different operations there are some that are more costly than others.
For that reason, the processing load is not always balanced all over the applications, and we may find a _bottleneck_ task
that could require more time to execute more complex operations than o``thers.

### Others
* _Dependency_<br/>
A task may have to wait data coming from more than one path, like in the case of Complex Event Processing,
and combine them to generate its output.
_This characteristic is not considered in this first version of the benchmark, as we think the impact it has can be
alternatively simulated by changing the traffic balancing and so the tuple arrival frequency._

* _Statefulness_<br/>
A task may be statuful, keeping stored values to use and compare with future and updated data, normally implemented through windows.
_The state of a task, and how it is implemented, is stricly dependant to the application. For that reason we decide not to implement it in our application, as we wouldn't be able to give a satisfying abstraction of the property._

* _IO Operations_<br/>
A common task in data processing is the interaction with an external database (read or write), these operations will
consume time and resources. However, considered the _real-time_ objective of data streaming applications, it is common
practice to use in-memory databases.
_Thus, considering the used time unimportant, we don't include it in the available parameters._

* _Fault Tolerance_<br/>
Most DSP systems are designed to be fault resistant. In case of a node failure or a single task crash, they implements
mechanisms to automatically restore the previous status, minimizing the damages.
_We don't take it into account because we considere fault tolerance as an external behavious and not application related. 
Our objective is to create a benchamark application, Fault Tolerance is something that has impact over the application but
is not directly controlled by it._