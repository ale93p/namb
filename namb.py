#! /usr/bin/env python3

import argparse
import subprocess
import configparser
import modules.namb_variables as vars

class CommandNotFound(Exception):
    def __init__(self, cmd):
        self.error_message = "The command '{}' is not an executable".format(cmd)

    def __str__(self):
        return "{}: {}".format(self.__class__.__name__, self.error_message)

def run_storm(custom_bin_path=None, namb_conf=vars.NAMB_CONF, storm_conf=vars.STORM_CONF):
    storm_bin = custom_bin_path if custom_bin_path else 'storm'
    storm_command = [storm_bin, 'jar', vars.STORM_JAR, vars.STORM_CLASS, namb_conf, storm_conf]
    if subprocess.run([storm_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(storm_command)
        return
    else:
        raise CommandNotFound(storm_bin)

def run_heron(custom_bin_path=None, namb_conf=vars.NAMB_CONF, heron_conf=vars.HERON_CONF):
    heron_bin = custom_bin_path if custom_bin_path else 'heron'
    conf = configparser.ConfigParser()
    conf.read(heron_conf)
    heron_command = [heron_bin, "submit", conf["deployment"], vars.HERON_CONF, vars.HERON_CLASS, namb_conf]
    if subprocess.run([heron_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(heron_command)
        return
    else:
        raise CommandNotFound(heron_command)

def run(cmd, **kwargs):
    if cmd == 'storm':
        run_storm(kwargs["custom_bin_path"], kwargs["custom_namb_conf"], kwargs["custom_platform_conf"])
        return
    if cmd == 'heron':
        run_heron(kwargs["custom_bin_path"], kwargs["custom_namb_conf"], kwargs["custom_platform_conf"])

    else:
        print("Oh my gosh. You shall not be here... Run fool!")

if __name__ == "__main__":
    # main parser
    main_parser = argparse.ArgumentParser(prog="namb.py")
    main_parser.add_argument("-c", "--conf", dest="namb_conf", metavar="<namb_conf>", help="Specify custom NAMB configuration file", default=vars.NAMB_CONF)

    # subparsers definition
    subparsers = main_parser.add_subparsers(dest="command", metavar="Platforms")
    subparsers.required = True

    # storm subparser
    storm_parser = subparsers.add_parser('storm', help='Run Apache Storm benchmark')
    storm_parser.add_argument("-p","--path", dest="exec_path", metavar="<storm_executable>", help="Path to Storm executable", default="storm")
    storm_parser.add_argument("-c", "--conf", dest="platform_conf", metavar="<storm_conf>", help="Specify custom Storm benchmark configuration file", default=vars.STORM_CONF)

    heron_parser = subparsers.add_parser('heron', help='Run Apache Heron benchmark')
    heron_parser.add_argument("-p","--path", dest="exec_path", metavar="<heron_executable>", help="Path to Heron executable", default="heron")
    heron_parser.add_argument("-c", "--conf", dest="platform_conf", metavar="<heron_conf>", help="Specify custom Heron benchmark configuration file", default=vars.HERON_CONF)

    args = main_parser.parse_args()

    try:
        run(args.command, **{"custom_bin_path":args.exec_path, "custom_namb_conf":args.namb_conf, "custom_platform_conf":args.platform_conf})
    except CommandNotFound as c:
        print(c)

