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

## JSON Transforms

- deklarativní(ish)

- psaný v JSONu a javascriptu

- rekurzivní způsob vyhodnocování: mocný a osvedčený přístup (např. v XSLT). přehledné a umožňuje složité transformace rozdělit do více jednodušších. šlo by zapsat pouze v JSONu.

- využívá JSPath: chceme využívat již existující dotazovací jazyk? vlastně ani nemůžeme, protože Ur není úplně validní JSON. musíme mít vlastní(!) JSON Transforms velmi spoléhá na JSPath (např. s funkcemi jako je filtrování a podobně)

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
