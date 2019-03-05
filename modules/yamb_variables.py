import os
import xml.etree.ElementTree as xml_parser

CWD = os.path.dirname(os.path.realpath(__file__)).replace('/modules','')
YAMB_POM = "{}/pom.xml".format(CWD)

pom = xml_parser.parse(yamb_POM)

STORM_BENCHMARK_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}storm.benchmark.version").text
HERON_BENCHMARK_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}heron.benchmark.version").text
FLINK_BENCHMARK_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}flink.benchmark.version").text

CONF_PATH = "{}/conf".format(CWD)

YAMB_CONF = "{}/yamb.yml".format(CONF_PATH)

STORM_CONF = "{}/storm-benchmark.yml".format(CONF_PATH)
STORM_JAR = "{}/benchmarks/storm-bench/target/storm-bench-{}.jar".format(CWD, STORM_BENCHMARK_VERSION)
STORM_CLASS = "fr.unice.yamb.storm.BenchmarkApplication"

HERON_CONF = "{}/heron-benchmark.yml".format(CONF_PATH)
HERON_JAR = "{}/benchmarks/heron-bench/target/heron-bench-{}.jar".format(CWD, HERON_BENCHMARK_VERSION)
HERON_CLASS = "fr.unice.yamb.heron.BenchmarkApplication"

FLINK_CONF = "{}/flink-benchmark.yml".format(CONF_PATH)
FLINK_JAR = "{}/benchmarks/flink-bench/target/flink-bench-{}.jar".format(CWD, FLINK_BENCHMARK_VERSION)
