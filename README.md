elasticsearch-analysis-annotation
=================================

Analysis plugin for ElasticSearch providing capability for processing inline annotations in documents.


Inline annotations are considered to be simple semantic informations inlined in source text, that are removed from the indexed text and injected as synonyms at positions of the words they are related to.

For example:
  "Mozart[artist] was born[lifeEveng] in Salzburg[city;Austria"
  "Ludwig van Beethoven[artist] died in Vienna[city]"

This plugin gives you the ability to search for those inlined annotations, which are treated as synonyms.
