#!/bin/bash
LOG_DIR='logs'
DB_FILE='../../graph/graph_database.db'


cd "$(dirname "$0")"

if [ ! -f $DB_FILE ]; then
	sqlite3 $DB_FILE < create_graph_schema.sql
	python adjacency/generate_adj_graph.py $DB_FILE
fi

mkdir -p $LOG_DIR
for i in {0..23}
do
    python transition/generate_transition_graph.py "$i:00" "`expr $i + 1`:00" ../../data/nyc_gov_trip_data/ ../../graph 2> $LOG_DIR"/"$i"00-`expr $i + 1`00.txt"
done
