---
layout: default
title: Configurations
nav_order: 3
has_children: true
permalink: /config
---

# Configurations
{: .no_toc }

To run a benchmark, NAMB uses two different configuration files: 
* `namb.yml` containing the parameters that model and describe the application
* and the platform-specific configuration, under the name `{platform}-benchmark.yml`

Both files, unless if specified, should be inside the main configuration folder: `$NAMB_HOME/conf/`. The naming should follow the one specified above.

> _The default configuration files can be found [here](https://github.com/ale93p/namb/conf/defaults)_.
> _Examples used in past works can be found in the [examples folder](https://github.com/ale93p/namb/conf/examples)_

The application description, defined in `namb.yml`, can follow two main model schemas: the generic and quick to customize **Workflow Schema**, and the task-specific **Pipeline Schema**. Use the specific pages of the schemas to see which are the available parameters.