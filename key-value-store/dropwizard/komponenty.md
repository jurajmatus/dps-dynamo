* pre každú podčasť vykonávania
 * zápis logu a metriky

# Obstarávanie requestov

## vonkajší interface
* StorageResource
 * GET, PUT, DELETE(, POST?)
 * asynchrónne vykonávanie
 * vygenerovanie request id (pre sledovanie requestu, logovanie, komunikáciu s uzlami, identifikáciu stavového stroja)
 * vygenerovanie úvodného stavu
 * poslanie správy do úvodnej message queue
 * ukončenie - zvyšok roboty riešia workery (message queue listenery), majú AsyncResponse, takže tu sa nebude nič odosielať naspäť
* deje, ktoré musia prebehnúť vo workeroch
 * nájdenie koordinátora a replikujúce uzly (poskytne trieda Topology z druhého komponentu)
 * rozriešenie konfliktov a verzií dát (vector clock)
 * uloženie do / výber z databázy (atomické skrz transakcie)
 * replikácia dát a koordinácia odpovedí (sledovanie počtu a rovnosti dát, odoslanie odpovede najskôr ako sa dá)
  * vysporiadanie sa so zlyhaniami

# Údržba spoľahlivosti na pozadí
* Topology
 * údržba o uzloch a ich pozíciách v chorde
 * interface pre získanie príslušných uzlov pre kľúč
* Obstaranie nového uzla
 * replikácia dát, ktoré má dostať (Merkle trees)
 * odstránenie dát z uzlov, kde časť kľúčov bola prekrytá novým uzlom
* Obstaranie zlyhania uzla
 * prevzatie zodpovednosti za kľúče novým uzlom
 * získanie dát
* Bootstrap nového uzla
