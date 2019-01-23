#! /usr/bin/env python3

import sys, os
import pandas as pd

def main(test_name):
    print("Chosen test: " + test_name)

def print_help(err_code, opts = {}):
    if err_code == 1:
        print("Please chose the test you want to generate the plots for")
    if err_code == 2:
        print("Please chose a valid test name between the available folders")
        print("Available tests:")
        for name in opts["tests_names"]: print("  * " + name)
    print("\nUsage:")
    print("  " + sys.argv[0] + " test_name")    

if __name__ == "__main__":
    err = 0
    opts = {}
    valid_names = [directory[0].replace('./','') for directory in os.walk('.') if directory[0] != '.']
    if len(sys.argv) < 2: err = 1
    elif sys.argv[1] not in valid_names: 
        err = 2
        opts = {"tests_names" : valid_names}
    
    if err != 0: print_help(err, opts)
    else: main(sys.argv[1])