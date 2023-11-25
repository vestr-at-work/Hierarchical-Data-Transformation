#!/usr/bin/env python3
# Transformation of JSON to Unified represention
#
# Implementation heavily inspired by Petr Škoda 
# for original see https://github.com/skodapetr/hierarchical-data-transformations/blob/main/python/json-to-ur.py

import json
import sys

KEY_TYPE = "@type"
KEY_VALUE = "@value"

def main():
    if len(sys.argv) < 2 | len(sys.argv) > 2:
        sys.stderr.write("single argument with path to input JSON file is mandatory.\n")
        exit(1)
    
    with open (sys.argv[1], encoding="utf-8") as input_stream:
        json_stream = json.load(input_stream)
        ur = get_unified_representation(json_stream)
        print(json.dumps(ur, indent=2))

# recursive
def get_unified_representation(json_stream):
    if isinstance(json_stream, dict):
        keys_and_values = {}
        for key, value in json_stream.items():
            keys_and_values[key] = [get_unified_representation(value)]
        return {KEY_TYPE: ["object"], **keys_and_values}
    elif isinstance(json_stream, list):
        items = {}
        for key, value in enumerate(json_stream):
            items[key] = [get_unified_representation(value)]
        return {KEY_TYPE: ["array"], **items}
    else:
        return {KEY_TYPE: [primitive_type(json_stream)], KEY_VALUE: [str(json_stream)]}
    
def primitive_type(value):
    if isinstance(value, str):
        return "string"
    elif isinstance(value, bool):
        return "boolean"
    elif isinstance(value, int):
        return "number"
    else:
        assert False, "Unsupported type"
        
if __name__ == "__main__":
    main()
