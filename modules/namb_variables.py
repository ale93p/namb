import os

STORM_BENCHMARK_VERSION='0.1.0'
HERON_BENCHMARK_VERSION='0.1.0-SNAPSHOT'

CWD = os.path.dirname(os.path.realpath(__file__)).replace('/modules','')
CONF_PATH = "{}/conf".format(CWD)

NAMB_CONF = "{}/namb.yml".format(CONF_PATH)

STORM_CONF = "{}/storm-benchmark.yml".format(CONF_PATH)
STORM_JAR = "{}/benchmarks/storm-bench/target/storm-{}.jar".format(CWD, STORM_BENCHMARK_VERSION)
STORM_CLASS = "fr.unice.namb.storm.BenchmarkApplication"

HERON_CONF = "{}/heron-benchmark.yml".format(CONF_PATH)
HERON_JAR = "{}/benchmarks/heron-bench/target/heron-{}.jar".format(CWD, HERON_BENCHMARK_VERSION)
HERON_CLASS = "fr.unice.namb.heron.BenchmarkApplication"