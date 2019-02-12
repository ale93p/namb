#! /usr/bin/env python3

import argparse
import subprocess
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

def run(cmd, **kwargs):
    if cmd == 'storm':
        run_storm(kwargs["custom_bin_path"], kwargs["custom_namb_conf"], kwargs["custom_storm_conf"])
    else:
        print("Oh my gosh. You shall not be here... Run fool!")

if __name__ == "__main__":
    # main parser
    main_parser = argparse.ArgumentParser(prog="namb.py")
    main_parser.add_argument("-c", "--conf", dest="namb_conf", help="Specify custom NAMB configuration file", default=vars.NAMB_CONF)

    # subparsers definition
    subparsers = main_parser.add_subparsers(dest="command", metavar="Commands")
    subparsers.required = True

    # storm subparser
    storm_parser = subparsers.add_parser('storm', help='Run Storm benchmark')
    storm_parser.add_argument("-p","--path", dest="exec_path", help="Specify the executable path for the middleware", default="storm")
    storm_parser.add_argument("-c", "--conf", dest="storm_conf", help="Specify custom Storm benchmark configuration file", default=vars.STORM_CONF)

    args = main_parser.parse_args()

    try:
        run(args.command, **{"custom_bin_path":args.exec_path, "custom_namb_conf":args.namb_conf, "custom_storm_conf":args.storm_conf})
    except CommandNotFound as c:
        print(c)

