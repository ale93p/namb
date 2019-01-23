#! /usr/bin/env python3

import sys 
import os
import plotting as p

plots_dir = './plots'

def main(test_name):
    if test_name == "common_individual":
        p.common_individual(out = plots_dir)
    else:
        print("Sorry, no plotting has been implemented for this results yet.")

def print_help(err_code, opts = {}):
    if err_code == 1:
        print("Please chose the test you want to generate the plots for")
    if err_code == 2:
        print("Please chose a valid test name between the available folders")
        print("Available tests:")
        for name in opts["tests_names"]: print("  * " + name)
    print("\nUsage:")
    print("  " + sys.argv[0] + " test_name")    

def init():
    if not os.path.exists(plots_dir):
        os.makedirs(plots_dir)

if __name__ == "__main__":
    err = 0
    opts = {}
    valid_names = [directory[0].replace('./','') for directory in os.walk('.') \
        if directory[0] != '.' and directory[0] != plots_dir and 'pycache' not in directory[0]]
    
    init()

    if len(sys.argv) < 2: err = 1
    elif sys.argv[1] not in valid_names: 
        err = 2
        opts = {"tests_names" : valid_names}
    
    if err != 0: print_help(err, opts)
    else: main(sys.argv[1])