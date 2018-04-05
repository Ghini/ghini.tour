#!/bin/bash
for i in en es de
do
    mkdir -p /tmp/$i
    l=$i-$(echo $i | tr a-z A-Z)
    if [[ $l == en-EN ]]
    then
        l=en-GB
    fi
    echo -n "language $l "
    for f in $i/*.txt
    do
        pico2wave -l $l -w /tmp/$i/$(basename $f .txt).wav "$(tail -n+2 $f | tr '\n\r' ' ')"
        sox /tmp/$i/$(basename $f .txt).wav /tmp/$i/$(basename $f .txt)-slow.wav speed 0.9 speed 100c
        lame --quiet -b32 -m m /tmp/$i/$(basename $f .txt)-slow.wav /tmp/$i/$(basename $f .txt).mp3
        rm /tmp/$i/$(basename $f .txt).wav
        echo -n .
    done
    echo " done"
done
