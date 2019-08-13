---
layout: default
title: Getting Started
parent: Documentation
nav_order: 1
---

# Getting Started

## Download

NAMB is open source and freely available on [GitHub](github.com/ale93p/namb). Head to the master branch for the last released version.

## Usage

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