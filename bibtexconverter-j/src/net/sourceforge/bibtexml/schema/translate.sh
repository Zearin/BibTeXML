#! /bin/bash
java -jar trang.jar bibtexml-groups2.rnc bibtexml-groups2.rng
for fields in arbitrary user core
do 
    for datatypes in strict lax
    do
        for structure in container flat
        do
            name=bibtexml-$fields-$datatypes-$structure
            echo $name
            java net.sf.saxon.Transform \
                bibtexml-groups2.rng schema.xsl \
                structure=${structure} \
                datatypes=$datatypes \
                fields=$fields \
                > $name.rng
            java -jar trang.jar $name.rng $name.rnc
        done
     done
done
