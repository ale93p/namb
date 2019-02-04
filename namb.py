#! /usr/bin/env python3

import argparse
import subprocess
import modules.namb_variables as vars

class CommandNotFound(Exception):
    def __init__(self, cmd):
        self.error_message = "The command '{}' is not an executable".format(cmd)

    def __str__(self):
        return "{}: {}".format(self.__class__.__name__, self.error_message)

def run_storm(custom_bin_path=None):
    storm_bin = custom_bin_path if custom_bin_path else 'storm'
    storm_command = [storm_bin, 'jar', vars.STORM_JAR, vars.STORM_CLASS, vars.CONF_PATH]
    print(storm_command)
    if subprocess.run([storm_bin], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
        subprocess.run(storm_command)
        return
    else:
        raise CommandNotFound(storm_bin)

def run(cmd, custom_bin_path=None):
    if cmd == 'storm':
        run_storm(custom_bin_path)
    else:
        print("Oh my gosh. You shall not be here... Run fool!")

if __name__ == "__main__":
    # main parser
    main_parser = argparse.ArgumentParser(prog="namb.py")

    # subparsers definition
    subparsers = main_parser.add_subparsers(dest="command", metavar="Commands:")
    subparsers.required = True

    # storm subparser
    storm_parser = subparsers.add_parser('storm', help='Run Storm benchmark')
    storm_parser.add_argument("-p","--path", dest="exec_path", help="Specify the executable path for the middleware")

    args = main_parser.parse_args()

    try:
        run(args.command, args.exec_path)
    except CommandNotFound as c:
        print(c)

