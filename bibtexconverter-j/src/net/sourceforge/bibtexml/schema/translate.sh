#! /bin/bash
java -jar "$TRANG" bibtexml-generic.rnc bibtexml-generic.rng
for fields in arbitrary user core
do
    for datatypes in strict loose
    do
        for structure in nested inline flat
        do
            name=bibtexml-$fields-$datatypes-$structure
            echo $name
            java -jar "$SAXON" \
                bibtexml-generic.rng schema.xsl \
                structure=${structure} \
                datatypes=$datatypes \
                fields=$fields \
                > $name.rng
            java -jar "$TRANG" $name.rng $name.rnc
        done
     done
done
