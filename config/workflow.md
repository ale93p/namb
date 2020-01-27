---
layout: default
title: Workflow Schema
parent: Configurations
nav_order: 1
---

# Workflow Schema

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## Data Stream Definition

### Synthetic Datastream

These properties are inside the `synthetic` tag of the `datastream` section: 
```yaml
datastream:
    synthetic:
        --> here <--

workflow:
    ...
```

| Property | Value 
|:----|:----|
| `data:` | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`size`](#size) | integer |
| &nbsp;&nbsp;&nbsp;&nbsp;[`values`](#values) | integer |
| &nbsp;&nbsp;&nbsp;&nbsp;[`distribution`](#distribution-data) | balanced \| unbalanced |
| `flow:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`distribution`](#distribution-flow) | uniform \| burst |
| &nbsp;&nbsp;&nbsp;&nbsp;[`rate`](#rate) | integer |

#### size
{: .no_toc }

```yaml
datastream:
    synthetic:
        data:
            size: 8
```
The size in _Bytes_ of the mesage (as a String value) that will be generated by the source task.

#### values
{: .no_toc }

```yaml
datastream:
    synthetic:
        data:
            values: 5
```
How many unique values make up the data set.

#### distribution (data)
{: .no_toc }

```yaml
datastream:
    synthetic:
        data:
            distribution: uniform
```
In an abstracted way, it represents the probability of a value occurrence:
* `uniform`: each value is inserted in the urn only one time (equal probability)
* `nonuniform`: each values is inserted several time in the urn following the power of 2 (e.g. 1st element added once, 2nd element twice, 3rd element 4 times, and so on)


#### distribution (flow)
{: .no_toc }

```yaml
datastream:
    synthetic:
        flow:
            distribution: uniform
```
It represent the rate distribution over time:
* `uniform`: the rate will be a CBR (Constant Bitrate), constant interval time between messages
* `burst`: the rate will be a CBR for most of the time, intervalled by periods without rate limitation

#### rate
{: .no_toc }

```yaml
datastream:
    synthetic:
        flow:
            rate: 100
```
The number of messages per second that will be generated. The max value is 1000, as `sleep` cannot go under 1ms. If the value is set to 0 there won't be any rate limitation.

### External Generation
These properties are inside the `external` tag of the `datastream` section: 
```yaml
datastream:
    external:
        --> here <--

workflow:
    ...
```

| Property | Value 
|:----|:----|
| `kafka:` | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`server`](#kafka-server) | text |
| &nbsp;&nbsp;&nbsp;&nbsp;[`group`](#group) | text |
| &nbsp;&nbsp;&nbsp;&nbsp;[`topic`](#topic) | text |
| `zookeeper:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`server`](#zookeeper-server) | text |

#### kafka server
{: .no_toc }

```yaml
datastream:
    external:
        kafka:
            server: localhost:9092
```
It speciefies the connection string `address:port` to the kafka server.

#### group
{: .no_toc }

```yaml
datastream:
    external:
        kafka:
            group: test
```
It speciefies the kafka group of which the client will be part of.

#### topic
{: .no_toc }

```yaml
datastream:
    external:
        kafka:
            topic: test
```
It speciefies the kafka topic to which connect the client.

#### kafka server
{: .no_toc }

```yaml
datastream:
    external:
        zookeeper:
            server: localhost:9092
```
It speciefies the connection string `address:port` to the zookeeper server.


## Workflow Definition

These properties are inside the `workflow` tag: 
```yaml
datastream:
    ...
workflow:
    --> here <--
```

| Property | Value | 
|:----|:----|
| [`depth`](#depth) | integer |
| `scalability:` | | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`parallelism`](#parallelism) | integer |
| &nbsp;&nbsp;&nbsp;&nbsp;[`balancing`](#balancing-scalability) | balanced \| increasing \| decreasing \| pyramid | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`variability`](#variability) | double | 
| `connection:` | | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`shape`](#shape) | linear \| diamond \| star | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`routing`](#routing) | balanced \| hash \| broadcast | 
| `workload:` | | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`processing`](#processing) | double | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`balancing`](#balancing-workload) | balanced \| increasing \| decreasing \| pyramid |
| [`reliability`](#reliability)| true \| false | 
| [`filtering`](#filtering)| double | 
| `windowing:` | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`enabled`](#windowing-enabled) | true \| false | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`type`](#windowing-type) | tumbling \| sliding | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`duration`](#windowing-duration) | integer | 
| &nbsp;&nbsp;&nbsp;&nbsp;[`interval`](#windowing-interval) | integer | 

#### depth
{: .no_toc }

```yaml
workflow:
    depth: 4
```
If we represent the topology as a tree of tasks, the depth represent the levels of the tree.

#### parallelism
{: .no_toc }

```yaml
workflow:
    scalability:
        parallelism: 10
```
It represent the total parallelism level of the topology.

N.B.: It should be at least equal to the number of tasks in the topology.
{: .text-red-000}

#### balancing (scalability)
{: .no_toc }

```yaml
workflow:
    scalability:
        balancing: balanced
```
It defines how the parallelism value is distributed all over the tasks:
* `balanced`: equally distributed
* `increasing`: the parallelism level of the tasks will _increase_ from the first to the last
* `decreasing`: the parallelism level of the tasks will _decrease_ from the first to the last
* `pyramid`: the parallelism level will increase until the central task and then decrease

#### variability
{: .no_toc }

```yaml
workflow:
    scalability:
        variability: 0.5
```
A percentage (normalized to 1) of parallelism variability, between components, when the it is not balanced. Hgher the variability, higher it will be the parallelism difference between the first and the last component.

#### shape
{: .no_toc }

```yaml
workflow:
    connection:
        shape: diamond
```
It set the connection shape of the topology:
* `linear`: each task is connected to the previous one (1-1-1)
* `star`: the first three levels of the topology will form a star shape (2-1-2)
* `diamond`: the first three levels of the topology will form a diamond shape (1-2-1)

#### routing
{: .no_toc }

```yaml
workflow:
    connection:
        routing: hash
```
It defines the way the data will be distributed over the tasks:
* `none`: without specifying a routing method, it will be applied the default one for each platform
* `balanced`: each task balances the routing of the messages in a balanced (e.g. round-robin) way to the following tasks
* `hash`: an hash-based function is used to decide the next task to where route the message
* `broadcast`: each message is replicate over all the following tasks 

Currently direct operator connection in Flink, and shuffle grouping in Storm and Heron.
{: .text-red-000 }

#### processing
{: .no_toc }

```yaml
workflow:
    workload:
        processing: 10
```
It represent the CPU load of the tasks. The integer value represent the number of cycles the busywait will perform.

To better understand how the value has been computed check the section relative to the [load generator](/project/implementation/load-generator).

#### balancing (workload)
{: .no_toc }

```yaml
workflow:
    workload:
        balancing: balanced
```

It defines how the workload is distrubuted over the tasks:
* `balanced`: equally distributed
* `increasing`: the cpu load of the tasks will _increase_ from the first to the last, starting from the value given
* `decreasing`: the cpu load of the tasks will _decrease_ from the first to the last, starting from the value given
* `pyramid`: the cpu load will increase until the middle task and then decrease, starting from the value given

#### reliability
{: .no_toc }

```yaml
workflow:
    reliability: true
```
A boolean value that enables (if true) the message reliability mechanism of the platform.

Currently not available in `flink`.
{: .text-red-000 }

#### filtering
{: .no_toc }

```yaml
workflow:
    filtering: 0
```
The value express the percentage (normalized to 1) of filtered data. 
Out of total of _x_ input values, the sink will output _filtering * x_ values.

#### windowing enabled
{: .no_toc}

```yaml
windowing:
    enabled: false
```
A boolean value that enables (if true) the windowing system in the application. The windowing is implemented in the last two tasks of the topology.

#### windowing type
{: .no_toc}

```yaml
windowing:
    type: tumbling
```
The type of the window we want to use:
* `tumbling`: sequence of windows lasting the specified duration, a new window is created after the previous one ends
* `sliding`: series of windows lasting the specified duration, a new window is created at the defined interval distance

#### windowing duration
{: .no_toc}

```yaml
windowing:
    duration: 30
```
It defines how much the window will last in seconds.

#### windowing interval
{: .no_toc}

```yaml
windowing:
    interval: 10
```
It defines the interval between the creation of new windows. It is useful only with sliding windows.