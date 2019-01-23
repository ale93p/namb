import glob
import os
import re
import pandas as pd
import matplotlib.pyplot as plt

def common_individual(out = None):
    root = "./common_individual/"
    cpu_to_plot = {}
    for csv in [f for f in glob.glob(root + "*.csv")]:
        task_name = re.search('_([a-z]+?)_bolt', csv).group(1)
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
    plt.title("Individual tasks benchmark")
    if(out): plt.savefig(os.path.join(out, "common_individual_" + task_name + ".pdf"))
    else: plt.show()