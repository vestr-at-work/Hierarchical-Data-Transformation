#!/usr/bin/env python3
# Transformation of CSV to Unified representation
#
import csv
import json
import sys

KEY_TYPE = "@type"
KEY_VALUE = "@value"
KEY_ROWS = "@rows"

def main():
    if len(sys.argv) < 2 | len(sys.argv) > 2:
        sys.stderr.write("single argument with path to input CSV file is mandatory.\n")
        exit(1)
    
    with open (sys.argv[1], encoding="utf-8") as input_stream:
        csv_reader = csv.reader(input_stream)
        ur = get_unified_representation(csv_reader)
        print(json.dumps(ur, indent=2))
        
def get_unified_representation(csv_reader):
    fields = next(csv_reader)
    rows = {}
    
    index = 0
    for row in csv_reader:
        row_object = {}
        row_index = 0
        for item in row:
            row_object[fields[row_index]] = [{KEY_TYPE: [primitive_type(item)], KEY_VALUE: [str(item)]}]
            row_index += 1
        rows[index] = [{KEY_TYPE: ["object"], **row_object}]
        index += 1
    
    return {KEY_TYPE: ["object"], KEY_ROWS: [rows]}
    
def is_int(value):
    if value is None:
        return False
    try:
        float(value)
        return True
    except:
        return False
    
def primitive_type(value):
    if (value == "true") | (value == "false"):
        return "boolean"
    elif is_int(value):
        return "number"
    else:
        return "string"
        
if __name__ == "__main__":
    main()
