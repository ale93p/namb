import os

CWD = os.getcwd()
CONF_PATH = CWD + "/conf"
STORM_JAR = CWD + "/benchmarks/storm/target/storm-1.0-SNAPSHOT.jar"
STORM_CLASS = "fr.unice.namb.storm.BenchmarkApplication"