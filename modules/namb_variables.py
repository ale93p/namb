import os

STORM_BENCHMARK_VERSION='0.1.0-alpha'

CWD = os.path.dirname(os.path.realpath(__file__)).replace('/modules','')
CONF_PATH = "{}/conf".format(CWD)
STORM_JAR = "{}/benchmarks/storm/target/storm-{}.jar".format(CWD, STORM_BENCHMARK_VERSION)
STORM_CLASS = "fr.unice.namb.storm.BenchmarkApplication"