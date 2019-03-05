#! /usr/bin/env python3

import argparse
import subprocess
import configparser
import modules.yamb_variables as vars


class CommandNotFound(Exception):
    def __init__(self, cmd):
        self.error_message = "The command '{}' is not an executable".format(cmd)

    def __str__(self):
        return "{}: {}".format(self.__class__.__name__, self.error_message)


def run_storm(custom_bin_path=None, yamb_conf=vars.YAMB_CONF, storm_conf=vars.STORM_CONF):
    storm_bin = custom_bin_path if custom_bin_path else 'storm'
    storm_command = [storm_bin, 'jar', vars.STORM_JAR, vars.STORM_CLASS, yamb_conf, storm_conf]
    if subprocess.run([storm_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(storm_command)
        return
    else:
        raise CommandNotFound(storm_bin)


def run_heron(custom_bin_path=None, yamb_conf=vars.YAMB_CONF, heron_conf=vars.HERON_CONF):
    heron_bin = custom_bin_path if custom_bin_path else 'heron'
    conf = configparser.ConfigParser()
    conf.read(heron_conf)
    heron_command = [heron_bin, "submit", conf["deployment"], vars.HERON_JAR, vars.HERON_CLASS, yamb_conf]
    if subprocess.run([heron_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(heron_command)
        return
    else:
        raise CommandNotFound(heron_command)


def run_flink(custom_bin_path=None, yamb_conf=vars.YAMB_CONF, heron_conf=vars.FLINK_CONF):
    flink_bin = custom_bin_path if custom_bin_path else 'flink'
    conf = configparser.ConfigParser()
    conf.read(heron_conf)
    heron_command = [flink_bin, "run", vars.FLINK_JAR, yamb_conf]
    if subprocess.run([flink_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(heron_command)
        return
    else:
        raise CommandNotFound(heron_command)


def run(cmd, **kwargs):
    if cmd == 'storm':
        run_storm(kwargs["custom_bin_path"], kwargs["custom_yamb_conf"], kwargs["custom_platform_conf"])

    elif cmd == 'heron':
        run_heron(kwargs["custom_bin_path"], kwargs["custom_yamb_conf"], kwargs["custom_platform_conf"])

        return
    elif cmd == 'flink':
        run_flink(kwargs["custom_bin_path"], kwargs["custom_yamb_conf"], kwargs["custom_platform_conf"])

    else:
        print("Oh my gosh. You shall not be here... Run fool!")


if __name__ == "__main__":
    # main parser
    main_parser = argparse.ArgumentParser(prog="yamb.py")
    main_parser.add_argument("-c", "--conf", dest="yamb_conf", metavar="<yamb_conf>", help="Specify custom YAMB configuration file", default=vars.YAMB_CONF)

    # subparsers definition
    subparsers = main_parser.add_subparsers(dest="command", metavar="Platforms")
    subparsers.required = True

    # storm subparser
    storm_parser = subparsers.add_parser('storm', help='Run Apache Storm benchmark')
    storm_parser.add_argument("-p","--path", dest="exec_path", metavar="<storm_executable>", help="Path to Storm executable", default="storm")
    storm_parser.add_argument("-c", "--conf", dest="platform_conf", metavar="<storm_conf>", help="Specify custom Storm benchmark configuration file", default=vars.STORM_CONF)

    # heron subparser
    heron_parser = subparsers.add_parser('heron', help='Run Apache Heron benchmark')
    heron_parser.add_argument("-p","--path", dest="exec_path", metavar="<heron_executable>", help="Path to Heron executable", default="heron")
    heron_parser.add_argument("-c", "--conf", dest="platform_conf", metavar="<heron_conf>", help="Specify custom Heron benchmark configuration file", default=vars.HERON_CONF)

    # flink subparser
    flink_parser = subparsers.add_parser('flink', help="Run Apache Flink benchmark")
    storm_parser.add_argument("-p","--path", dest="exec_path", metavar="<flink_executable>", help="Path to Flink executable", default="flink")
    storm_parser.add_argument("-c", "--conf", dest="platform_conf", metavar="<flink_conf>", help="Specify custom Flink benchmark configuration file", default=vars.FLINK_CONF)


    args = main_parser.parse_args()

    try:
        run(args.command, **{"custom_bin_path":args.exec_path, "custom_yamb_conf":args.yamb_conf, "custom_platform_conf":args.platform_conf})
    except CommandNotFound as c:
        print(c)

