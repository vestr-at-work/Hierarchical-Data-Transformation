# Poznámky k transformačnímu jazyku

## Obecně

- líbilo by se mi kdyby složitější transformace mohly (nebo třeba i musely) být dělány přes více jednoduších kroků. pomůže to s přehledností a i s ověřováním správnosti daných transformací.

- design jazyka by měl být modulární a rozšiřitelný nejen námi. user defined moduly?

## JOLT

- deklarativní: chceme taky

- v JSONu: chceme taky

- modulární (pro každý druh transformace existuje vlastní doménově specifický podjazyk): zajímavé, můžeme využít také. skvělé pro rozšíření ale potenciálně horší na prvotní zorientování se v jazyce.

- velmi cool mi přijde, že při deklaracích transformací jde od vstupu k výstupu a ne naopak. to znamená, že nedeklaruje formát výstupu, ale vychází z formátu vstupu(!) a kdykoliv narazí na hodnotu, kterou chce využít na výstupu, tak místo hodnoty samotné zapíše cestu k pozici ve výstupním formátu. to zjednodušuje implementaci a relativně intuitivně se i používá. +společně s wildcards a proměnnými je relativně mocné. mohlo by také pomoct se streamováním? procházet vstup streamovaně a pokaždé jen hodnotu na kterou narazíme transformovat na výstup?

- primárně pracuje pouze s formátem, takže nemá způsob jak např. porovnat název nějakého labelu s konstatním řetězcem a vypsat na výstup pouze pokud se rovná. mohli bychom se inspirovat se základem, ale přidat i funkce na práci s daty?

- https://docs.google.com/presentation/d/1sAiuiFC4Lzz4-064sg1p8EQt2ev0o442MfEbvrpD1ls/edit#slide=id.g9798b391_00

- https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Removr.java

## JSON Transforms

- deklarativní(ish)

- psaný v JSONu a javascriptu

- rekurzivní způsob vyhodnocování: mocný a osvedčený přístup (např. v XSLT). přehledné a umožňuje složité transformace rozdělit do více jednodušších. šlo by zapsat pouze v JSONu.

- využívá JSPath: chceme využívat již existující dotazovací jazyk? vlastně ani nemůžeme, protože Ur není úplně validní JSON. musíme mít vlastní(!) JSON Transforms velmi spoléhá na JSPath (např. s funkcemi jako je filtrování a podobně)

- https://github.com/ColinEberhardt/json-transforms

-----------------

## Návrh

Inspiroval bych se JOLTem a přidal funkce pro práci s hodnotami (predikáty ap.)
  
- hlavní výhody:
  
  - velmi modulární a uživatelsky rozšiřitelné (lze naimplementovat tak, že každá operace bude mít vlastní modul splňující rozhraní "operace" a uživatelé by mohli jen poskytnout vlastní implementaci pro svou vlastní operaci).

  - potenciálně relativně snadná implementace díky psaní transformací od vstupu k výstupu (lze projít vstupní strom a vykonat transformaci až když se narazí na hodnotu).

  - potenciálně streamovatelné (aspoň myslím).
  
- hlavní nevýhody:
  - rozdělení operací na více částí a jejich oddělení do modulů by mohlo být neefektivní (vstupní strom by se musel projít vícekrát a nešlo by mezi moduly spolupracovat).
  
  - vzhledem k tomu, že Ur samotné je ukecané, tak nutit uživatele opisovat vstupní data do více operací může být nepříjemné (ideálně se zamyslet nad nějakým sugar codem).

Transformace bude z Ur do Ur a následně předvod z transformovaného Ur do výstupu.
To znamená, že uživatelé budou muset být seznámeni s Ur a jeho jednotlivými specifiky pro jednotlivé formáty, aby dokázali správně transformovat (mohli bychom pak sepsat návody v dokumentaci pro každý převod z formátu do formátu (jen zmínit úskalí na která si dát pozor a jak se s nimi vypořádat)).
Trochu jde do protikladu s tím, že by Ur byla jen vnitřní reprezentace.

K transformaci je možné připsat komentář pod klíč "@comment" pro vysvětlení transformace.  

Pro XML vstup:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<person>
    <name xml:lang="en">Ailish</name>
    <age>18</age>
    <items>
        <item>Bread</item>
        <item>Milk</item>
    </items>
    <properties>
        <inteligence>100</inteligence>
        <knowledge>100</knowledge>
    </properties>
</person>
```

s Ur reprezentací:

```json
{
    "@version": ["1.0"],
    "@encoding": ["UTF-8"],
    "person": [{
        "@type": ["object"],
        "name": [{
            "@attributes": [{
                "xml:lang": [{
                    "@type": ["string"],
                    "@value": ["en"]
                }]
            }],
            "@type": ["string"],
            "@value": ["Ailish"]
        }],
        "age": [{
            "@type": ["number"],
            "@value": ["18"]
        }],
        "items": [{
            "type": ["object"],
            "@0:item": [{
                "@type": ["string"],
                "@value": ["Bread"]
            }],
            "@1:item": [{
                "@type": ["string"],
                "@value": ["Milk"]
            }]
        }],
        "properties": [{
            "@type": ["object"],
            "inteligence": [{
                "@type": ["number"],
                "@value": ["100"]
            }],
            "knowledge": [{
                "@type": ["number"],
                "@value": ["100"]
            }]
        }]
    }]
}
```

Můžeme napsat transformaci, která nám vybere jméno a věk u osoby a jejich hodnoty vloží do objektu s přejmenovanými klíči.

Transformace:

```json
{
    "@operations": [
        {
            "@operation": "take-values",
            "@comment": "Take the name and age of a person.",
            "@specs": {
                "person": [{
                    "name": [{
                        "@attributes": [{
                            "xml:lang": []
                        }]
                    }],
                    "age": []
                }]
            }
        },
        {
            "@operation": "shift-values",
            "@comment": "Work with the name and age of a person and shift its position to output object called 'human' with a 'given-name' and 'years-on-earth'.",
            "@specs": {
                "person": [{
                    "name": [{
                        "@attributes": [{
                            "xml:lang": [{
                                "@path": ["human.given-name.xml:lang"]
                            }]
                        }],
                        "@path": ["human.given-name"]
                    }],
                    "age": [{
                        "@path": ["human.years-on-earth"]
                    }]
                }]
            }
        }
    ]
}
```

JOLT podporuje Shift hodnot, nastavení Defaultních hodnot, Remove hodnot, Cardinality hodnot, Sort a přímo java kód.

Podporoval bych Shift, Default hodnoty, Remove/Take hodnot, Sort a asi i Cardinality. Místo javy bych podporoval predikáty v Remove/Take (a Shift?) operacích.

Výše je vidět příklad Take a Shift.

JOLT používá wildcards pomocí "*" a potom v cestě adresované pomocí "&1", "&2", atd. (kanonicky &(0,1) - aka jdi o 0 pater výš ve stromu a vezmi první wildcard v klíči) pro využití v hodnotě výstupu. 
A adresované  pomocí "$(0,1)" atd. pro využití v klíči výstupu.

> Co mít, místo symbolů, které se člověk naučit, pojmenované proměnné pro lepší překlednost? např: @var:personType a potom odkaz pomocí &var:personType nebo tak něco?

Take:

"[]" v hodnotě ze vstupu pro daný leaf klíč kopíruje hodnotu ze vstupu na výstup.
> (Možný sugar potom je, že by "[{}]" v hodnotě bralo celý objekt daného klíče.)

Když by se hodnota měla zkopírovat na výstup jen při splnění nějaké podmínky, tak by v leaf klíči byl array s jedním objektem s klíčem "@predicate" a logickým výrazem.
Např (upravená část z příkladu výše):

```json
{
    "@operation": "take-values",
    "@comment": "Take the name and age of a person.",
    "@specs": {
        "person": [{
            "name": [{
                "@attributes": [{
                    "xml:lang": [{
						"@predicate": ["@value == 'en'"]
					}]
                }]
            }],
            "age": [{
				"@predicate": ["..name.@attributes.xml:lang.@value == 'en'"]
			}]
        }]
    }
}
``` 

> Pokud bychom chtěli podporovat streamování, tak určitě nemůžeme podporovat predikáty, které testují hodnotu jinou, než @value.

> Jak řešit symboly "@", které se využívají v UR a ne v transformaci? Asi ignorovat
