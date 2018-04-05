#!/bin/bash

echo "begin transaction;"
echo "delete from poi;"
echo "update sqlite_sequence set seq=0 where name='poi';"

for garden in $(dirname $0)/*/coords.org
do
    garden_id=$(sed -ne 's/^id:[ ]*//p' $garden)

    cat $garden | sed -ne "s/| \([0-9]*\) | \(.*[^ ]\)[ ]*| .* | \(.*\) | \(.*\) |/insert into poi (sub_id, title, lat, lon, location_id) values (\1, '\2',\3, \4, $garden_id);/p"
done

echo 'commit;'
