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
More sophisticated analyzers (equivalent to StandardAnalyzer or SnowballAnalyzer)
can be configure via configuration file elasticsearch.yml or web API. 


Example
-------
Let's say we have this documents
```
"Mozart[artist] was born[lifeEvent] in Salzburg[city;Austria]"
```

If we parse this with StandardAnalyzer equivalent with annotation analysis added to it
we get these tokens - some are omitted due to used StopFilter.
```
                           | [austria]
[artist] | | [lifeevent] | |  [city]
 mozart  | |    born     | | salzburg
```

If we  use StandardAnalyzer the result would be
```
mozart | artist | | born | lifeevent | | salzburg | city | austria
```


Installation
------------
This plugin follows conventions for elasticsearch plugins, thus can be installed
in a standard manner - see http://www.elasticsearch.org/guide/reference/modules/plugins/


Using this plugin
-----------------
To use those custom analyzers/filters you need to either modify `elasticsearch.yml` 
configuration file - see http://www.elasticsearch.org/guide/reference/index-modules/analysis/ or specify
index mapping via elasticsearch API.

The following example configuration contains definitions for analyzers based on behaviour of
StandardAnalyzer and SnowballAnalyzer.

*Please note that standard_annotation and snowball_annotation analyzers use standard tokenizer,
which removes all non-alphanumeric characters and thus makes it impossible to process inline
annotations marked with [,],; (which are used in default behaviour of InlineAnnotationFilter).*

For this purpose we need to use mapping char filter, which remaps those special characters to
their equivalent, which will be accepted by standard tokenizer as part of the token.

```
 index :
     analysis :
         char_filter : 
             annotation_remap : 
                 type : mapping
                 mappings : ["[=>__annotation_start__", "]=>__annotation_end__",";=>__annotation_delimiter__"]
         analyzer :                
             standard_annotation :
                 type : custom
                 tokenizer : standard
                 char_filter : annotation_remap
                 filter : [standard, lowercase, annotation_filter, stop]
             snowball_annotation :
                 type : custom
                 tokenizer : standard
                 char_filter : annotation_remap
                 filter : [standard, lowercase, annotation_filter, stop, snowball]
         filter :
             annotation_filter :
                 type : annotation_filter
                 start : __annotation_start__
                 end : __annotation_end__
                 delimiter : __annotation_delimiter__
```

To test the analyzer you can query the following
    http://localhost:9200/test/_analyze?analyzer=annotation&text="Mozart[city;Salzburg]"

Limitation
----------
Another thing to keep in mind is that you can't use word-delimiting characters inside annotations.
The whole string would be treated as two tokens which would result in unexpected behaviour.

Customization
-------------
The InlineAnnotationFilter can be slightly customized.

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


Elasticsearch version
---------------------
This plugin was successfuly tested on elasticsearch version 0.90.2