#!/usr/bin/env python3
# Transformation of XML to Unified representation
#
import xml.etree.ElementTree as ET
import json
import sys

KEY_TYPE = "@type"
KEY_VALUE = "@value"
KEY_ATTRIBUTES = "@attributes"

def main():
    if len(sys.argv) < 2 | len(sys.argv) > 2:
        sys.stderr.write("single argument with path to input XML file is mandatory.\n")
        exit(1)
    
    tree = ET.parse(sys.argv[1])
    root = tree.getroot()
    ur = get_unified_representation(root, True)
    print(json.dumps(ur, indent=2))
    
def get_child_tags_occurances_and_count(element):
    tag_counts = {}
    count = 0
    for child in element:
        count += 1
        if child.tag in tag_counts:
            tag_counts[child.tag] += 1
        else:
            tag_counts[child.tag] = 1
    return tag_counts, count
    
    
def get_attributes_obj(element):
    if element.attrib == {}:
        return None
    key_and_values = {}
    for key, value in element.attrib.items():
        key_and_values[key] = [{KEY_TYPE: [primitive_type(value)], KEY_VALUE: [str(value)]}]
    
    return {KEY_ATTRIBUTES: [key_and_values]}
    
def get_tag_count(occurances):
    tag_count = {}
    for tag in occurances:
        tag_count[tag] = 0
    return tag_count
    
def get_unified_representation(element, isroot):
    tag_occurances, child_count = get_child_tags_occurances_and_count(element)
    attributes_obj = get_attributes_obj(element)
    
    if child_count <= 0:
        if attributes_obj != None:
            return {**attributes_obj, KEY_TYPE: [primitive_type(element.text)], KEY_VALUE: [str(element.text)]}
        return {KEY_TYPE: [primitive_type(element.text)], KEY_VALUE: [str(element.text)]}
    
    items = {}
    tag_count = get_tag_count(tag_occurances)
    for child in element:
        tag = child.tag
        if tag_occurances[child.tag] > 1:
            tag = "@" + str(tag_count[child.tag]) + ":" + child.tag
            tag_count[child.tag] += 1
        items[tag] = [get_unified_representation(child, False)]
        
    result = {}
    if attributes_obj != None:
        result = {**attributes_obj, KEY_TYPE: ["object"], **items}
    else:  
        result = {KEY_TYPE: ["object"], **items}
        
    if not isroot:
        return result
        
    return {element.tag: [result]}
        
    
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
