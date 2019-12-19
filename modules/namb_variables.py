import os
from os.path import join
import xml.etree.ElementTree as xml_parser

CWD = os.path.dirname(os.path.realpath(__file__)).replace('/modules','').replace('\modules','')
NAMB_POM = join(CWD,"pom.xml")
pom = xml_parser.parse(NAMB_POM)

NAMB_VERSION = pom.getroot().find("{http://maven.apache.org/POM/4.0.0}properties").find("{http://maven.apache.org/POM/4.0.0}revision").text

CONF_PATH = join(CWD,"conf")
CONF_PATH_DEFAULTS = join(CONF_PATH,"defaults")

NAMB_CONF = join(CONF_PATH,"namb.yml")

STORM_CONF = join(CONF_PATH,"storm-benchmark.yml")
STORM_JAR = join(CWD,"benchmarks","storm-bench","target","storm-bench-{}.jar".format(NAMB_VERSION))
STORM_CLASS = "fr.unice.namb.storm.BenchmarkApplication"

HERON_CONF = join(CONF_PATH,"heron-benchmark.yml")
HERON_JAR = join(CWD,"benchmarks","heron-bench","target","heron-bench-{}.jar".format(NAMB_VERSION))
HERON_CLASS = "fr.unice.namb.heron.BenchmarkApplication"

FLINK_CONF = join(CONF_PATH,"flink-benchmark.yml")
FLINK_JAR = join(CWD,"benchmarks","flink-bench","target","flink-bench-{}.jar".format(NAMB_VERSION))
