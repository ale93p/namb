# Results plotting

Here are all the tests results. It's also available a python script to generate the plots.

### Available results

Tests from [#9](https://github.com/ale93p/yamb/issues/9):
* **common_individual**: contains the results of each common tasks executed singularly, in a spout->bolt manner.
* **common_full_topo**: contains the results of the common tasks implemented as a full connected topology.

### Plots generation

To generate the plots for the tests in this folder, use the **python3** script `generate_plots.py`. The plots in pdf format will be stored in the folder `./plots`.

```
usage: generate_plots.py [-h] [-a] [-s] [-f {pdf,png,jpg}] [test_name]

Generate plots for tests results

positional arguments:
  test_name             generates plots for the test name specified (default:
                        None)

optional arguments:
  -h, --help            show this help message and exit
  -a, --all             generates plots for all the available tests (default:
                        False)
  -s, --show            only shows the plot instead of saving the pdf
                        (default: False)
  -f {pdf,png,jpg}, --format {pdf,png,jpg}
                        output image format (default: pdf)
```
