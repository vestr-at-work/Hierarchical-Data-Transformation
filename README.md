# Transformace hierarchických dat

> TOTO JE DOKUMENTACE KE STARŠÍ VERZI. PRINCIPY ZŮSTÁVAJÍ, ALE DETAILY SE MĚNILY.
> AKTUÁLNÍ VERZE DOKUMENTACE JE SOUČÁSTÍ [BAKALÁŘSKÉ PRÁCE](Transformace_hierarchických_dat_BP.pdf) VIZ KAPITOLA 5.

## Návrh transformačního nástroje

Návrh stojí na dvou stěžejních konceptech.  
Prvním je Unifikovaná reprezentace (zkráceně Ur) hierarchických dat, umožňující sjednocení vstupních a výstupních formátů i se zachováním jejich specifik.
Zjednodušeně si lze Ur představit jako JSON s výhradně řetězcovými hodnotami.
Druhým konceptem je pak transformace pracující s Unifikovanou reprezentací na svém vstupu i výstupu.
Pro celou transformaci tedy nejdříve data ve vstupním formátu převedeme do Ur, vykonáme transformaci a následně výstup exportujeme z Ur do zvoleného formátu.

## Unifikovaná reprezentace

K sjednocení hierarchických datových formátů představujeme tzv. Unifikovanou reprezentaci neboli Ur.
Jak už bylo řečeno výše, jednoduše si lze Ur představit jako JSON pouze s řetězcovými hodnotami.
Hlavními částmi Unifikované reprezentace jsou entity, pole a hodnoty. 
Hodnoty jsou tedy vždy řetězcového typu. 
Pole se využívají k uchování potenciálně více hodnot pro daný klíč/index.
Entity v Ur reprezentují jak objekty, tak i pole. 
Objekty jsou kolekce dvojic klíč a hodnota. 
Podobně tak pole jsou reprezetované jako kolekce dvojic index a hodnota. 


Veškerá funkcionalita hierarchických formátů bohužel jen takto jednoduše reprezentovat nejde a proto i Unifikovaná reprezentace je ve skutečnosti specifická pro jednotlivé formáty a jejich potřeby. 
Uveďme nyní příklad Unifikované reprezentace pro formát JSON.

### JSON Ur

Pro následující JSON

```json
{
    "library": {
        "name": "Open Library",
        "books": [{
            "attributes": {
                "condition": "good"
            }
            "book-title": "Příliš hlučná samota",
            "author": "Bohumil Hrabal",
            "page-count": 98
        }, {
            "attributes": {
                "condition": "torn"
            }
            "book-title": "Nesmrtelnost",
            "author": "Milan Kundera",
            "page-count": 352
        }]
    }
}
```

vypadá Unifikovaná reprezentace následovně.

```json
{
    "@type": ["object"],
    "library": [{
        "@type": ["object"],
        "name": [{
            "@type": ["string"],
            "@value": ["Open Library"]
        }],
        "books": [{
            "@type": ["array"],
            "0": [{
                "@type": ["object"],
                "attributes": [{
                    "@type": ["object"],
                    "condition": [{
                        "@type": ["string"],
                        "@value": ["good"]
                    }]   
                }],
                "book-title": [{
                    "@type": ["string"],
                    "@value": ["Příliš hlučná samota"]
                }],
                "author": [{
                    "@type": ["string"],
                    "@value": ["Bohumil Hrabal"]
                }],
                "page-count": [{
                    "@type": ["number"],
                    "@value": ["98"]
                }]
            }], 
            "1": [{
                "@type": ["object"],
                "attributes": [{
                    "@type": ["object"],
                    "condition": [{
                        "@type": ["string"],
                        "@value": ["torn"]
                    }]   
                }],
                "book-title": [{
                    "@type": ["string"],
                    "@value": ["Nesmrtelnost"]
                }],
                "author": [{
                    "@type": ["string"],
                    "@value": ["Milan Kundera"]
                }],
                "page-count": [{
                    "@type": ["number"],
                    "@value": ["352"]
                }]
            }]  
        }]
    }]
} 
```

Jako první si můžeme všimnout, že všechny hodnoty jsou uzvařené do pole a mají jen jednu hodnotu. 
Dále je vidět, že první klíč každé entity je speciální klíč ```@type```, který určuje typ entity. 
Zároveň jím určujeme i typ uložený v řetězci jednoduché hodnoty.
Samotnou hodnotu potom ukládáme pod speciálním klíčem ```@value```.
Řídící klíče budeme v Ur uvozovat symbolem ```@```, přičemž inspiraci čerpáme z RDF serializace JSON-LD.
Nakonec si všimněme, že JSON pole jsou také entity (jen typu ```array```) a jednotlivé entity pole jsou schované pod klíči s jejich indexy v původním poli.

## Transformační jazyk

Jak už bylo řečeno výše náš navrhovaný transformační nástroj je těsně provázaný s Unifikovanou reprezentací.
Pracuje totiž s Ur na svém vstupu i výstupu, což nám umožní určitou míru sjednocení transformačních operací.
Píšeme pouze o určité míře sjednocení neboť Unifikovaná reprezentace je specifická pro konkrétní formát.
Z toho vyplívá, že i zápis transformací bude muset být závislý na vstupních a výstupních formátech. 
Přesněji pouze na jejich Unifikovaných reprezentacích.

Zápis transformace samotný je validní JSON dokument, takže v tomto smyslu je transformační jazyk deklarativní.
Dokument obsahuje výčet transformačních operací, které se postupně aplikují na vstup.
Každá operace využívá svůj vlastní doménově specifický jazyk.
Podporované jsou zatím operace pro posunutí hodnoty nebo podstromu (shift), odstranění hodnoty (remove), filtrování hodnot pomocí predikátu (filter) a nastavení defaultní hodnoty (default).

### Příklad

```json
{
    "operations": [
        {
            "operation": "shift",
            "comment": "Hodnota v /person/name bude na výstupu na pozici /human/called a to bude jediná hodnota na výstupu",
            "specs": [
                {
                    "input-path": "/person/name",
                    "output-path": "/human/called"
                }
            ]
        }
    ]
}
```

### Předvedení operací

Tato sekce obsahuje jednoduché představení jednotlivých operací ve formě tutorialu.

Všechny ukázky operací počítají s jednoduchým vstupem.

```json
{
    "person": {
        "name": "Ailish"
    }
}
```

#### Shift operace

Tato operace se využívá pro posun konkrétních hodnot nebo celých podstromů na výstup. Skládá se z pole objektů, které mají klíče "input-path" a klíč "output-path". "input-path" obsahuje UrPointer k hodnotě na vstupu a "output-path" obsahuje buďto cestu nebo pole cest ve výstupním objektu do kterých se posune vstupní entita. Nezmíněné entity se na výstup nekopírují.

Základní shift operace může vypadat následovně.

```json
{
    "operation": "shift",
    "comment": "Posuň hodnotu name z objektu person do objektu human",
    "specs": [
        {
            "input-path": "/person/name",
            "output-path": "/human/name"
        }
    ]
}
```

Výstup operace:

```json
{
    "human": {
        "name": "Ailish"
    }
}
```

---

Pro vložení entit na konec pole se v cestě využívá symbol "[]".

```json
{
    "operation": "shift",
    "comment": "Posuň hodnotu name z objektu person do pole names v objektu human",
    "specs": [
        {
            "input-path": "/person/name",
            "output-path": "/human/names/[]"
        }
    ]
}
```

Výstup operace:

```json
{
    "human": {
        "names": ["Ailish"]
    }
}
```

#### Remove operace

Tato operace slouží k odstranění klíčů a jejich hodnot (ať už literálů, či celých objektů). Zapisuje se jako pole objektů s klíčem "path" držící cestu ke klíči k odstranění ve vstupní entitě. Výstupem je tedy vstupní entita bez všech klíčů (a jejich hodnot) zmíněných v operaci. Když se odstraní poslední klíč z objektu, tak na výstupu objekt zůstává (jen je prázdný).

Základní remove operace může vypadat třeba takto.

```json
{
    "operation": "remove",
    "comment": "Odstraň klíč name v objektu person",
    "specs": [
        {
            "path": "/person/name"
        }
    ]
}
```

Výstup je prázdný JSON objekt.

```json
{}
```

#### Filter operace

Tato operace slouží k jednoduchému filtrování hodnot. Operace se zapisuje jako pole objektů s klíči "path" a "predicate". Klíč "path" uchovává cestu ke filtrovanému klíči ve vstupní entitě a klíč "predicate" uchovává podmínku (predikát), kterou musí hodnota filtrovaného klíče splňovat, aby se ocitla na výstupu. Výstup operace je tedy entita s všemi klíči a hodnotami, které splňují podmínky predikátů a nebo nebyly v operaci nijak zmíněny.

Predikáty jsou dvojího typu. Buďto se vztahují k samotné hodnotě a nebo k jejímu typu. Značí se (ne náhodou) stejně jako v Unifikované reprezentaci "@value" respektive "@type". V zápisu predikátu po nich vždy následuje znaménko >, >=, <, <=, == (pouze některá jsou validní u určitých typů, např. typ boolean podporuje jen ==). Na pravé straně znaménka je potom samotná hodnota vůči které porovnáváme.

> V tuto chvíli funguje pouze porovnání typů a hodnot na řetězcovou rovnost a nerovnost.

Základní filter operace může vypadat například takto.

```json
{
    "operation": "filter",
    "comment": "Filtrování literálu podle hodnoty",
    "specs": [
        {
            "path": "/person/name",
            "predicate": "@value == Ailish"
        }
    ]
}
```

Predikát se nemusí vyskytovat jen u literálu. Porovnávat hodnotu u jiných než literálních hodnot, ale povoleno není.

```json
{
    "operation": "filter",
    "comment": "Filtrování s porovnáním typu entity u neliterálu",
    "specs": [
        {
            "path": "/person",
            "predicate": "@type == object"
        }
    ]
}
```

#### Default operace

Tato operace slouží k nastavení výchozích hodnot pro nové klíče. Pokud ve vstupní entitě už hodnota pro tento klíč existuje nic se nenastavuje. Operace je pole objektů s klíči "path" a "value". "path" obsahuje cestu ke klíči, kterému chce uživatel nastavit hodnotu "value". Výstupem operace je tedy vstupní entita rozšířená o výchozí hodnoty nastavené při operaci.

Základní default operace může vypadat třeba takto.

```json
{
    "operation": "default",
    "comment": "Nastav výchozí hodnotu pro klíč name v objektu person",
    "specs": [
        {
            "path": "/person/name",
            "value": "Bilish"
        }
    ]
}
```

Výstup operace je stejný jako vstup neboť klíč už existuje.

```json
{
    "person": {
        "name": "Ailish"
    }
}
```

## Jak nástroj spustit

Nestihl jsem zprovoznit závislosti pro spuštění pomocí JAR souboru, tak se v tuto chvíli nástroj spouští pomocí Maven příkazu ```mvn exec:java -Dexec.args="testinput.json testtransformation.json"```.
Soubory ```testinput.json``` a ```testtransformation.json``` jsou jen testovací vstupní data a testovací zápis jedoduché transformace. Ty mohou být nahrazeny libovolným jiným souborem. Výstup se vypisuje na standartní výstup.

## Jak vygenerovat dokumentaci

Stačí spustit příkaz ```mvn javadoc:javadoc```.
