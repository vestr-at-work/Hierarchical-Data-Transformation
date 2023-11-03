# Example transformations

## Input for transformations #1, #2 and #3

```json
{
    "public-transport": {
        "bicycles": [{
            "title": "BMX bike",
            "colors": ["blue", "white"],
            "passenger-count": 1
        },{
            "title": "Tandem bike",
            "colors": ["white"],
            "passenger-count": 2
        },{
            "title": "Cargo e-bike",
            "colors": ["black", "brown", "orange"],
            "passenger-count": 1
        }],
        "trams": [{
            "title": "Historic tram",
            "colors": ["white", "red"],
            "passenger-count": 52
        },{
            "title": "Skoda tram",
            "colors": ["black", "yellow"],
            "passenger-count": 86
        }],
        "busses": [{
            "title": "Smol bus",
            "colors": ["yellow"],
            "passenger-count": 4
        }]
    }
}
```

## Output for transformation #1

> Query: "Get title and group of all vehicles with passenger count greater than 1".

```json
{
    "result": [{
        "title": "Tandem bike",
        "group": "bicycles"
    },{
        "title": "Historic tram",
        "group": "trams"
    },{
        "title": "Skoda tram",
        "group": "trams"
    },{
        "title": "Smol bus",
        "group": "busses"
    }]
}
```

## Output for transformation #2

> Query: "Get number of at least partly white vehicles".

```json
{
    "partly-white-vehicle-count": 3
}
```

### Output for transformation #3

>Query: "Get all the distinct colors of vehicles".

What order should it have?

```json
{
    "colors-of-vehicles": [
        "blue",
        "white",
        "black",
        "brown",
        "orange",
        "red",
        "yellow"
    ]
}
```

----

## Input for transformations #4 and #5

```xml
<?xml version="1.0" encoding="UTF-8"?>
<quests>
    <quest 
        id="a24cb3e"
        multi-part="true"
    >
        <name>Lord's last words</name>
        <short-description>Finding treasure of the fleeing lord.</short-description>
        <long-description>
            One upon a time there was a bad feudal lord ...
        </long-description>
    
        <part num="1">
            <location>
                <latitude>50.3907506N</latitude>
                <longitude>14.5458556E</longitude>
            </location>
            <clue>Last he has been seen gripping on to his chest running into the woods south of the town.</clue>
        </part>
        <part num="2">
            <location>
                <latitude>50.3907506N</latitude>
                <longitude>14.5458556E</longitude>
            </location>
            <clue>Lorem ipsum coloret sudo pathusm.</clue>
        </part>
        <part num="3">
            <location>
                <latitude>50.3907506N</latitude>
                <longitude>14.5458556E</longitude>
            </location>
            <clue>Try to investigate the cave.</clue>
        </part>
    </quest>
    
    <quest 
        id="a24cb4e"
        multi-part="true"
    >
        <name>Quest number 2</name>
        <short-description>Finding quest of the number 2.</short-description>
        <long-description>
            One upon a time there was a quest ...
        </long-description>
    
        <part num="1">
            <location>
                <latitude>50.3907506N</latitude>
                <longitude>14.5458556E</longitude>
            </location>
            <clue>Lorem ipsum coloret sudo pathusm. Idre pute hletro imo.</clue>
        </part>
        <part num="2">
            <location>
                <latitude>50.3907506N</latitude>
                <longitude>14.5458556E</longitude>
            </location>
            <clue>Lorem ipsum coloret sudo pathusm.</clue>
        </part>
    </quest>
</quests>
```

## Output for transformation #4

>Query: "Transform data to json including the attributes as keys in the quests object".

```json
{
    "quests": {
        "a24cb3e": {
            "multi-part": true,
            "name": "Lord's last words",
            "short-description": "Finding treasure of the fleeing lord.",
            "long-description": "Once upon a time there was a bad feudal lord ...",
            "part": [{
                "num": 1,
                "location": {
                    "latitude": "50.3907506N",
                    "longitude": "14.5458556E"
                },
                "clue": "Last he has been spotted holding his chest running into the woods south of the town."
            },{
                "num": 2,
                "location": {
                    "latitude": "50.3907506N",
                    "longitude": "14.5458556E"
                },
                "clue": "Lorem ipsum coloret sudo pathusm."
            },{
                "num": 3,
                "location": {
                    "latitude": "50.3907506N",
                    "longitude": "14.5458556E"
                },
                "clue": "Try to investigate the cave."
            }]
        },
        "a24cb4e": {
            "multi-part": true,
            "name": "Quest number 2",
            "short-description": "Finding quest of the number 2.",
            "long-description": "Once upon a time there was a quest ...",
            "part": [{
                "num": 1,
                "location": {
                    "latitude": "50.3907506N",
                    "longitude": "14.5458556E"
                },
                "clue": "Lorem ipsum coloret sudo pathusm. Idre pute hletro imo."
            },{
                "num": 2,
                "location": {
                    "latitude": "50.3907506N",
                    "longitude": "14.5458556E"
                },
                "clue": "Lorem ipsum coloret sudo pathusm."
            }]
        }
    }
}
```

## Output for transformation #5

>Query: "Get the coordinates of every path and identify it with distinct quest-part-id and save it in CSV"

```csv
quest-part-id,latitude,longitude
a24cb3e:1,50.3907506N,14.5458556E
a24cb3e:2,50.3907506N,14.5458556E
a24cb3e:3,50.3907506N,14.5458556E
a24cb4e:1,50.3907506N,14.5458556E
a24cb4e:2,50.3907506N,14.5458556E
```

----

## Input for transformation #6

```json
{
    "person": {
        "tel": 444432421
    },
    "department": {
        "tel": 732444111
    }
}
```

## Output for transformation #6

```json
{
    "all-tel-nums": [444432421, 732444111],
    "department": {
        "tel": 732444111,
        "person": {
            "tel": 444432421
        }
    }
}
```

----

## Input for transformation #7

```json
{
    "contact": {
        "tel": "+420 444432421"
    },
    "department": {
        "info": {
            "contact": {
                "tel": "+420 732444111"
            }
        }
    }
}
```

## Output for transformation #7

> "Transform every tel number in very contact."

```json
{
   "contact": {
        "tel": 444432421,
        "preselection": "+420"
    },
    "department": {
        "info": {
            "contact": {
                "tel": 732444111,
                "preselection": "+420"
            }
        }
    }
}
```

----

## Input for transformation #8

```json
{
    "contact": {
        "tel": "+420 444432421"
    },
    "math-department": {
        "info": {
            "contact": {
                "tel": "+420 732444111"
            }
        }
    },
    "computer-science-department": {
        "info": {
            "contact": {
                "tel": "+420 001010111"
            }
        }
    }
}
```

## Output for transformation #8

```json
{
   "lower-level-contacts-count": 2
}
```

----

## Input for transformation #9

```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog>
  <cd>
    <title>Empire Burlesque</title>
    <artist>Bob Dylan</artist>
    <country>USA</country>
    <company>Columbia</company>
    <price>10.90</price>
    <year>1985</year>
  </cd>
  <cd>
    <title>Hide your heart</title>
    <artist>Bonnie Tyler</artist>
    <country>UK</country>
    <company>CBS Records</company>
    <price>9.90</price>
    <year>1988</year>
  </cd>
  <cd>
    <title>Greatest Hits</title>
    <artist>Dolly Parton</artist>
    <country>USA</country>
    <company>RCA</company>
    <price>9.90</price>
    <year>1982</year>
  </cd>
  <cd>
    <title>Still got the blues</title>
    <artist>Gary Moore</artist>
    <country>UK</country>
    <company>Virgin records</company>
    <price>10.20</price>
    <year>1990</year>
  </cd>
  <cd>
    <title>Eros</title>
    <artist>Eros Ramazzotti</artist>
    <country>EU</country>
    <company>BMG</company>
    <price>9.90</price>
    <year>1997</year>
  </cd>
  <cd>
    <title>One night only</title>
    <artist>Bee Gees</artist>
    <country>UK</country>
    <company>Polydor</company>
    <price>10.90</price>
    <year>1998</year>
  </cd>
  <cd>
    <title>Sylvias Mother</title>
    <artist>Dr.Hook</artist>
    <country>UK</country>
    <company>CBS</company>
    <price>8.10</price>
    <year>1973</year>
  </cd>
  <cd>
    <title>Maggie May</title>
    <artist>Rod Stewart</artist>
    <country>UK</country>
    <company>Pickwick</company>
    <price>8.50</price>
    <year>1990</year>
  </cd>
  <cd>
    <title>Romanza</title>
    <artist>Andrea Bocelli</artist>
    <country>EU</country>
    <company>Polydor</company>
    <price>10.80</price>
    <year>1996</year>
  </cd>
</catalog>
```

## Output for transformation #9

> "Transform to html table with title and artist and give pink background to cd's with a price greater than 10."

```xml
<html>
   <body>
      <h2>My CD Collection</h2>
      <table border="1">
         <tr bgcolor="#9acd32">
            <th>Title</th>
            <th>Artist</th>
         </tr>
         <tr>
            <td>Empire Burlesque</td>
            <td bgcolor="#ff00ff">Bob Dylan</td>
         </tr>
         <tr>
            <td>Hide your heart</td>
            <td>Bonnie Tyler</td>
         </tr>
         <tr>
            <td>Greatest Hits</td>
            <td>Dolly Parton</td>
         </tr>
         <tr>
            <td>Still got the blues</td>
            <td bgcolor="#ff00ff">Gary Moore</td>
         </tr>
         <tr>
            <td>Eros</td>
            <td>Eros Ramazzotti</td>
         </tr>
         <tr>
            <td>One night only</td>
            <td bgcolor="#ff00ff">Bee Gees</td>
         </tr>
         <tr>
            <td>Sylvias Mother</td>
            <td>Dr.Hook</td>
         </tr>
         <tr>
            <td>Maggie May</td>
            <td>Rod Stewart</td>
         </tr>
         <tr>
            <td>Romanza</td>
            <td bgcolor="#ff00ff">Andrea Bocelli</td>
         </tr>
      </table>
   </body>
</html>
```
