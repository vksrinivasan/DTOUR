#!/bin/bash
LOG_DIR='logs'
DB_FILE='../../graph/graph_database.db'
DATA_DIR='../../data/nyc_gov_trip_data'
BASE_URL='https://s3.amazonaws.com/nyc-tlc/trip+data'
DOWNLOAD_DIR='../../data/nyc_gov_trip_data'
INTERVAL_LENGTH=60

cd "$(dirname "$0")"

if [ ! -f $DB_FILE ]; then
	sqlite3 $DB_FILE < create_graph_schema.sql
	python adjacency/generate_adj_graph.py $DB_FILE
fi

mkdir -p $LOG_DIR
for year in {2014..2014}
do
	for month in `seq -f "%02g" 1 3`
	do
		DATA_NAME="yellow_tripdata_$year-$month"
		LOG_DATA_FILE="$LOG_DIR/$DATA_NAME.txt"

		DOWNLOAD_URL="$BASE_URL/$DATA_NAME.csv"
		DOWNLOAD_FILE="$DOWNLOAD_DIR/$DATA_NAME.csv"
		
		# echo $DOWNLOAD_URL
		# echo $DOWNLOAD_FILE

		wget -O $DOWNLOAD_FILE $DOWNLOAD_URL
		python -W ignore transition/generate_transition_graph.py $INTERVAL_LENGTH $DOWNLOAD_FILE $DB_FILE 2> $LOG_DATA_FILE
		rm $DOWNLOAD_FILE
	done
done

