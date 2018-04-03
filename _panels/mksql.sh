#!/bin/bash

garden_id=$(sed -ne 's/^id:[ ]*//p' $1)

cat $1 | sed -ne "s/| \([0-9]*\) | \(.*[^ ]\)[ ]*| \(.*\) | \(.*\) |/insert into poi (title, description, lat, lon, location_id) values ('\2','\2',\3,\4,$garden_id);/p"
