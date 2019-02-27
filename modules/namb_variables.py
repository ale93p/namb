import os
import xml.etree.ElementTree as xml_parser

CWD = os.path.dirname(os.path.realpath(__file__)).replace('/modules','')
NAMB_POM = "{}/pom.xml".format(CWD)

pom = xml_parser.parse(NAMB_POM)

STORM_BENCHMARK_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}storm.benchmark.version").text
HERON_BENCHMARK_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}heron.benchmark.version").text


CONF_PATH = "{}/conf".format(CWD)

NAMB_CONF = "{}/namb.yml".format(CONF_PATH)

STORM_CONF = "{}/storm-benchmark.yml".format(CONF_PATH)
STORM_JAR = "{}/benchmarks/storm-bench/target/storm-bench-{}.jar".format(CWD, STORM_BENCHMARK_VERSION)
STORM_CLASS = "fr.unice.namb.storm.BenchmarkApplication"

HERON_CONF = "{}/heron-benchmark.yml".format(CONF_PATH)
HERON_JAR = "{}/benchmarks/heron-bench/target/heron-bench-{}.jar".format(CWD, HERON_BENCHMARK_VERSION)
HERON_CLASS = "fr.unice.namb.heron.BenchmarkApplication"