---
layout: default
title: Platform-Specific Configurations
parent: Configurations
nav_order: 3
---

# Platform-Specific Configurations

Each [supported DSP platform](/project/platforms) has a specific configuration file to define application independent parameters.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

### Global Configurations

#### deployment
{: .no_toc }

```yaml
deployment: local | remote
```
It defines if the deployed platform is in local mode on in a remote cluster.

#### debugFrequency
{: .no_toc }

```yaml
debugFrequency: 0.0005
```
It specify the sample percentage of tuples to log, which is then translated into ouptut frequency. E.g. `0.0005` means that it will print _1 every 2000_ tuples (1/0.0005 = 2000).

### Storm Specifics
It currently support the configuration of:

#### workers
{: .no_toc }

```yaml
workers: 3
```
Number of workers the application will be deployed on.

#### maxSpoutPending
{: .no_toc }

```yaml
maxSpoutPending: 2000
```
The `max_spout_pending` value used in Storm to enable backpressure at the source.