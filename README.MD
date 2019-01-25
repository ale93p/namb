# NAMB

## Description

**NAMB** (__N__*ot* __A__ __M__*icro-*__B__*enchmark*) is an *parametrizable* and *automatically generated* benchmark application for Data Stream Processing Systems (DSP).

## The Idea

NAMB aims to be a general benchmark to have reproducible results for Data Stream Processing Systems.
Considering that _common_ industrial benchmarks focus field-specific tasks (such IoT or CEP), they are
(such it should be) hardly reusable in different contexts; also, the _most-used_ micro-benchmark applications
are rarely described in literature, so that to leave their implementation totally dependand by the author
of a benchmark, thus make a comparison between different results very difficult.

We base our implementation on a definition of several fundamental characteristics common to DSP applications,
and which, in our opinion, have an important impact over the characterization of an application workload.
We then abstract these characteristics in a set of parameters configurable by the user. 

Given these configurations, NAMB will __deterministically__ create a DSP application. NAMB challenges are:
* **Automation**: the application will be automatically created without the need of the user to __touch__ the specific middleware APIs
* **Ease-of-use**: the configuration given shall not be over-complicated, but give the minimum set of parameters that allows the best application definition flexibility 
* **Determinism**: given the same set of configurations the generated application will always be the same, no use of random functions
* **Standardization**: the definition of the parameters has to make the benchmark results, over different systems/infrastructures, comparable **without the need of reproducing it**

## DSP Fundamental Characteristics

### Data Stream
#### Data Characteristics
#### Input Ratio

### Dataflow
#### DAG Depth
#### Scalability
#### Connection
#### Dependency
#### Traffic Balancing
#### Message Reliability
#### Workload