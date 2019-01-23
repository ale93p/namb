#! /usr/bin/env python3

import sys 
import os
import argparse
import plotting as p

plots_dir = './plots'

def main(test_name, args):
    if args.just_show: 
        p.boxplot(test_name)
    else: 
        p.boxplot(test_name, out = plots_dir)

def init():
    if not os.path.exists(plots_dir):
        os.makedirs(plots_dir)

if __name__ == "__main__":
    err = 0
    opts = {}
    valid_names = [directory[0].replace('./','') for directory in os.walk('.') \
        if directory[0] != '.' and directory[0] != plots_dir and 'pycache' not in directory[0]]
    
    parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter, description="Generate plots for tests results")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("test_name", metavar="test_name", type=str, nargs='?', choices=valid_names, help="generates plots for the test name specified")
    group.add_argument("-a", "--all", dest="all_tests", action="store_true", default=False, help="generates plots for all the available tests")
    parser.add_argument("-s", "--show", dest="just_show", action="store_true", default=False, help="only shows the plot instead of saving the pdf")
    

    args = parser.parse_args()
    init()
    
    if args.all_tests:
        for test in valid_names:
            main(test, args)
    else:
        main(args.test_name[0], args)