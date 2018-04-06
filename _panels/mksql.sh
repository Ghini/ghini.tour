#!/bin/bash

# this the script implements the stream g2w4t.
#
# input: geographic infopanels data.
# output: javascript to update the ghini.web mongodb.
# --
# as a shortcut, only as a shortcut, we are producing the SQL for updating
# the tour database.

echo "begin transaction;" > update.sql
echo "delete from poi;" >> update.sql
echo "update sqlite_sequence set seq=0 where name='poi';" >> update.sql

echo 'conn = new Mongo();' > update.js
echo 'db = conn.getDB("gardens");' >> update.js
echo 'db.infopanels.remove({});' >> update.js

for garden in $(dirname $0)/*/coords.org
do
    garden_id=$(sed -ne 's/^id:[ ]*//p' $garden)
    garden_name=$(sed -ne 's/^name:[ ]*//p' $garden)
    echo "$garden_name"

    # SQL assumes org table starts with the 4 columns
    # | id | title | lat | lon |
    #
    # the ghini.tour database only wants latitude, longitude, and title:
    # text is spoken, is language dependente, and is inferred from sub_id
    # and location_id.
    
    cat $garden | sed -ne "s/|[ 0]*\([0-9]*\) |[ ]*\(.*[^ ]\)[ ]*|[ ]*\(.*[^ ]\)[ ]*|[ ]*\(.*[^ ]\)[ ]*|.*|/insert into poi (sub_id, title, lat, lon, location_id) values (\1, '\2',\3, \4, $garden_id);/p" >> update.sql

    # mongo assumes org table starts with the 5 columns
    # | id | title | lat | lon | minzoom |
    #
    # The ghini.web database wants the complete information: latitude,
    # longitude, title, and minimal zoom for triggering visibility.
    # Description comes from a text file, is in principle language
    # dependent, and we should grab it but for the time being, we skip it.
    
    description=""
    do_this='s/|[ 0]*\([0-9]*\) |[ ]*\(.*[^ ]\)[ ]*|[ ]*\(.*[^ ]\)[ ]*|[ ]*\(.*[^ ]\)[ ]*|[ ]*\([0-9]*[^ ]\)[ ]*|.*/db.infopanels.insert({"garden": "'$garden_name'", "title": "\2", "lat": \3, "lon": \4, "description": "", "zoom": \5, "id_within_garden": \1, "garden_id": '$garden_id'});/p'
    cat $garden | sed -ne "$do_this" >> update.js

done

echo 'commit;' >> update.sql

