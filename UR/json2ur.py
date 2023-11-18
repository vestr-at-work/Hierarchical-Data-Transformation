#!/usr/bin/env python3

import json

def main():
    if len(sys.argv) < 2:
        sys.stderr.write("First argument is mandatory and has to be path to correctly formated JSON file.\n")
        exit(1)

    json = loadJson(sys.argv[1])
    