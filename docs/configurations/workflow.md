---
layout: default
title: Workflow Schema
parent: Configurations
nav_order: 2
---

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## Application configurations

### Dataflow

This properies are inside the `dataflow` tag: 
```yaml
dataflow:
    --> here <--
datastream:
    ...
```

| Property | Value | [Characteristics](/docs/concepts.html#fundamental-characteristics) |
|:----|:----|:----|
| [`depth`](#depth) | integer | DAG Depth |
| `scalability:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`parallelism`](#parallelism) | integer | Scalability |
| &nbsp;&nbsp;&nbsp;&nbsp;[`balancing`](#balancing-scalability) | balanced \| increasing \| decreasing \| pyramid | Scalability |
| &nbsp;&nbsp;&nbsp;&nbsp;[`variability`](#variability) | double | Scalability |
| `connection:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`shape`](#shape) | linear \| diamond \| star | Connection |
| &nbsp;&nbsp;&nbsp;&nbsp;[`routing`](#routing) | balanced \| hash \| broadcast | Traffic Balancing |
| [`reliable`](#reliable)| true \| false | Message Reliability |
| `windowing:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`enabled`](#windowing-enabled) | true \| false | Windowing |
| &nbsp;&nbsp;&nbsp;&nbsp;[`type`](#windowing-type) | tumbling \| sliding | Windowing |
| &nbsp;&nbsp;&nbsp;&nbsp;[`duration`](#windowing-duration) | integer | Windowing |
| &nbsp;&nbsp;&nbsp;&nbsp;[`interval`](#windowing-interval) | integer | Windowing |
| `workload:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`processing`](#processing) | double | Workload |
| &nbsp;&nbsp;&nbsp;&nbsp;[`balancing`](#balancing-workload) | balanced \| increasing \| decreasing \| pyramid | Workload |

#### depth
{: .no_toc }

```yaml
dataflow:
    depth: 4
```
If we represent the topology as a tree of tasks, the depth represent the levels of the tree.

#### parallelism
{: .no_toc }

```yaml
dataflow:
    scalability:
        parallelism: 10
```
It represent the total parallelism level of the topology.

N.B.: It should be at least equal to the number of tasks in the topology.
{: .text-red-000}

#### balancing (scalability)
{: .no_toc }

```yaml
dataflow:
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
dataflow:
    scalability:
        variability: 0.5
```
A percentage (normalized to 1) of parallelism variability, between components, when the it is not balanced. Hgher the variability, higher it will be the parallelism difference between the first and the last component.

#### shape
{: .no_toc }

```yaml
dataflow:
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
dataflow:
    connection:
        routing: hash
```
It defines the way the data will be distributed over the tasks:
* `balanced`: each task balances the routing of the messages in a balanced (e.g. round-robin) way to the following tasks
* `hash`: an hash-based function is used to decide the next task to where route the message
* `broadcast`: each message is replicate over all the following tasks 

#### reliable
{: .no_toc }

```yaml
dataflow:
    reliable: true
```
A boolean value that enables (if true) the message reliability mechanism of the platform.

Currently not available in `flink`.
{: .text-red-000 }

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

#### processing
{: .no_toc }

```yaml
dataflow:
    workload:
        processing: 10
```
It represent the CPU load of the tasks. The integer value represent the number of cycles the busywait will perform, some examples of values could be:

<table>
<tr><td> lower </td><td> 0.5 </td><td> equivalent of an aggregation task </td></tr>
<tr><td> medium </td><td> 150 </td><td> equivalent of a ranking task </td></tr>
<tr><td> higher </td><td> 500 </td><td> equivalent of a transformation task </td></tr>
</table>

To better understand how the value has been computed check the section relative to the [cpu load abstraction](/docs/implementation/cpu-load).

#### balancing (workload)
{: .no_toc }

```yaml
dataflow:
    workload:
        balancing: balanced
```

It defines how the workload is distrubuted over the tasks:
* `balanced`: equally distributed
* `increasing`: the cpu load of the tasks will _increase_ from the first to the last, starting from the value given
* `decreasing`: the cpu load of the tasks will _decrease_ from the first to the last, starting from the value given
* `pyramid`: the cpu load will increase until the middle task and then decrease, starting from the value given


### Synthetic Datastream

This properies are inside the `synthetic` tag of the `datastream` section: 
```yaml
dataflow:
    ...
datastream:
    synthetic:
        --> here <--
```

| Property | Value | [Characteristics](/docs/concepts.html#fundamental-characteristics) |
|:----|:----|:----|
| `data:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`size`](#size) | integer | Data Characteristics |
| &nbsp;&nbsp;&nbsp;&nbsp;[`values`](#values) | integer | Data Characteristics, Traffic Balancing |
| &nbsp;&nbsp;&nbsp;&nbsp;[`balancing`](#balancing-data) | balanced \| unbalanced | Data Characteristics, Traffic Balancing |
| `flow:` | | |
| &nbsp;&nbsp;&nbsp;&nbsp;[`distribution`](#distribution) | uniform \| burst \| saw-tooth \| normal \| bimodal | Input Ratio |
| &nbsp;&nbsp;&nbsp;&nbsp;[`rate`](#rate) | integer | Input Ratio |

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

#### balancing (data)
{: .no_toc }

```yaml
datastream:
    synthetic:
        data:
            balancing: unbalanced
```
In an abstracted way, it represents the probability of a value occurrence:
* `balanced`: each value is inserted in the urn only one time (equal probability)
* `unbalanced`: each values is inserted several time in the urn following the power of 2 (e.g. 1st element added once, 2nd element twice, 3rd element 4 times, and so on)


#### distribution
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

## Platform-specific configurations

TODO