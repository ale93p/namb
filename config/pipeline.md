---
layout: default
title: Pipeline Schema
parent: Configurations
nav_order: 2
---

# Pipeline Schema

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## Common Properties

The tasks definitions are listed inside the two keys `pipeline` and `tasks`:

```yaml
pipeline:
    tasks:
        --> here <--
```

| Property | Value 
|:----|:----|
| [`name`](#name) | text |
| [`parallelism`](#parallelism) | integer |
| [`reliability`](#reliability) | boolean |

#### name
{: .no_toc }

```yaml
pipeline:
    tasks:
    - name: task
```
It define the name of the implemented task

#### parallelism
{: .no_toc }

```yaml
pipeline:
    tasks:
    - name: task
      parallelism: 0
```
It represent the parallelism level of the task

#### reliability
{: .no_toc }

```yaml
pipeline:
  tasks:
    - name: task
      reliability: true
```
A boolean value that enables (if true) the message reliability mechanism of the platform in the current task.

Currently not available in `flink`.
{: .text-red-000 }

## Source Properties

The sources are the first tasks listed under the `tasks`tag. The properties of the data stream that will be injected in the application are identical as the ones described in the Workflow Schema: 
* the source can [generate syntethic data](config/workflow#synthetic-datastream)
* or connect to an [external kafka](config/workflow#external-generation) producer

## Processing Task Properties

At the same manner, the tasks will be defined as a **ordered** list.

```yaml
pipeline:
    tasks:
        - name: source
          ...

        - --> here <--
```

| Property | Value |
|:----|:----|
| [`routing`](#routing) | balanced \| hash \| broadcast |
| [`processing`](#processing) | double |
| [`filtering`](#filtering) | double |
| [`resizedata`](#resizedata) | integer |
| [`parents`](#parents) | list |

N.B. Windowing is currenctly supported by the configuration parser, but not yet implemented in the application builder.
{: .text-red-000}

#### routing
{: .no_toc }

```yaml
- name: task
  routing: hash
```
Defines the type of connection of the current task to the previous one:
* `none`: without specifying a routing method, it will be applied the default one for each platform
* `balanced`: the task balances the routing of the messages in a balanced (e.g. round-robin) way to the following task instances
* `hash`: an hash-based function is used to decide the next task instance to where route the message
* `broadcast`: each message is replicate over all the following task instances

Currently direct operator connection in Flink, and shuffle grouping in Storm and Heron.
{: .text-red-000 }

#### processing
{: .no_toc }

```yaml
- name: task
  processing: 10
```
It represent the CPU load of the task. The integer value represent the number of cycles the busywait will perform.

To better understand how the value has been computed check the section relative to the [load generator](/project/implementation/load-generator).

#### filtering
{: .no_toc }

```yaml
- name: task
  filtering: 0
```
The value express the percentage (normalized to 1) of filtered data. 
Out of total of _x_ input values, the sink will output _filtering * x_ values.

#### resizedata
{: .no_toc }

```yaml
- name: task
  resizedata: 15
```
It defines the size in Bytes of the emitted data from the task, different from the input one. It represent a data transformation.

N.B. To not excessively overload the task, in the current NAMB *v0.6.0*, it is only possible to reduce datasize.
{: .text-red-000}

#### parents
{: .no_toc }

```yaml
- name: children
  parents:
  - parent_one
  - parent_two
```
This tag will list all the parents of the current child. Defining which tasks are connected to the current one; i.e. from which tasks the data will come from in the current task.