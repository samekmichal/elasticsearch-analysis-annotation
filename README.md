elasticsearch-analysis-annotation
=================================

Analysis plugin for ElasticSearch providing capability for processing inline
annotations in documents.

Description
-----------

Inline annotations are considered to be simple semantic informations inlined in
source text, that are removed from the indexed text and injected as synonyms at
positions of the words they are related to.

This plugin provides analyzer `AnnotationAnalyzer` as well as filter
`InlineAnnotationFilter`.
`AnnotationAnalyzer` is composed of `WhitespaceTokenizer`, `LowerCaseFilter` and
`InlineAnnotationFilter` (with default settings).


Example
-------
Let's say we have this documents
    ```
    "Mozart[artist] was born[lifeEvent] in Salzburg[city;Austria]"
    
    "Beethoven[artist] died in Vienna[city]"
    ```

could result is this token streams (depends on tokenizers and other filters used)
```
                                           | <[austria]>
 <[artist]> |       | <[lifeEvent]> |      |  <[city]>
 <mozart>   | <was> |     <born>    | <in> | <salzburg>
 ```


 ```
 <[artist]>  |               | <[city]>
 <beethoven> | <died> | <in> | <vienna>
 ```



Installation
------------
This plugin follows conventions for elasticsearch plugins, thus can be installed
in standard manner - see http://www.elasticsearch.org/guide/reference/modules/plugins/


Using this plugin
-----------------
To use those custom analyzers/filters you need to modify `elasticsearch.yml` 
configuration file - see http://www.elasticsearch.org/guide/reference/index-modules/analysis/

example configuration:
```
index :
    analysis :
        analyzer :                
            annotation :
                type : annotation
                
            annotation_filter :
                type : custom
                tokenizer : whitespace
                filter : [lowercase,annotation_filter]
```

To test the analyzer you can query the following
    http://localhost:9200/test/_analyze?analyzer=annotation&text="Mozart[city;Salzburg]"


Customization
-------------
Both AnnotationAnalyzer and InlineAnnotationFilter can be slightly customized.

List of supported options
 + `start` - start delimiter for inline annotation
 + `end` - end delimiter for inline annotation
 + `prefix` - string to be prepended to synonym, that is created from inline annotation
 + `suffix` - string to be apended to synonym, that is created from inline annotation
 + `token-type` - token type of synonym
 + `delimiter` - delimiter for multiple inline annotations

Example providing default values
```
index :
    analysis :
        analyzer :                
            annotation :
                type : annotation
                start : [
                end : ]
                prefix : [
                suffix : ]
                token-type: synonym
                delimiter : ;
```
