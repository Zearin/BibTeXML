#! /bin/bash
java -jar trang.jar bibtexml-generic.rnc bibtexml-generic.rng
for fields in arbitrary user core
do 
    for datatypes in strict loose
    do
        for structure in container flat
        do
            name=bibtexml-$fields-$datatypes-$structure
            echo $name
            java net.sf.saxon.Transform \
                bibtexml-generic.rng schema.xsl \
                structure=${structure} \
                datatypes=$datatypes \
                fields=$fields \
                > $name.rng
            java -jar trang.jar $name.rng $name.rnc
        done
     done
done
