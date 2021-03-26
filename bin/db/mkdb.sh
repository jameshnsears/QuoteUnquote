#!/usr/bin/env bash

# sudo apt install sqlite3

DATABASE=../../app/src/main/assets/quotations.db.prod

rm -f $DATABASE

sqlite3 $DATABASE < tables.sql

sqlite3 <<EOF
.open $DATABASE
.mode csv
.import quotations.db.prod.csv quotations
EOF

# index's completel screw up various devices!
#sqlite3 $DATABASE < indexes.sql