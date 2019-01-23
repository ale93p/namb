import glob
import os
import re
import pandas as pd
import matplotlib.pyplot as plt

def boxplot(root, out = None):

    cpu_to_plot = {}
    re_pattern = '([0-9]*)_([a-z]+?)_(bolt|[0-9]*_)'
    for csv in [f for f in sorted(glob.glob(os.path.join(root, "*.csv")), 
                                    key=lambda k: re.search(re_pattern, k).group(2))]:
        task_name = re.search(re_pattern, csv).group(2)
        df = pd.read_csv(csv, sep=',')
        cpu_time = df["tot"] \
            .diff() \
            .divide(1000000) \
            .iloc[1:] \
            .tolist() 
        cpu_to_plot[task_name] = cpu_time
    

    labels, data = cpu_to_plot.keys(), cpu_to_plot.values()
    plt.boxplot(data)
    plt.xticks(range(1, len(labels) + 1), labels)
    plt.ylabel("CPU Load (ms)")
    plt.title(root.replace("_", " "))
    if out: plt.savefig(os.path.join(out, "{}_boxplot.pdf".format(root)))
    else: plt.show()
