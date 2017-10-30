#!/bin/bash
LOG_DIR='logs'
DB_FILE='../../graph/graph_database.db'
DATA_DIR='../../data/nyc_gov_trip_data'
BASE_URL='https://s3.amazonaws.com/nyc-tlc/trip+data'
DOWNLOAD_DIR='../../data/nyc_gov_trip_data'

cd "$(dirname "$0")"

if [ ! -f $DB_FILE ]; then
	sqlite3 $DB_FILE < create_graph_schema.sql
	python adjacency/generate_adj_graph.py $DB_FILE
fi

mkdir -p $LOG_DIR
for year in {2014..2015}
do
	for month in `seq -f "%02g" 1 12`
	do
		DATA_NAME="yellow_tripdata_$year-$month"
		LOG_DATA_DIR="$LOG_DIR/$DATA_NAME"
		mkdir -p $LOG_DATA_DIR

		DOWNLOAD_URL="$BASE_URL/$DATA_NAME.csv"
		DOWNLOAD_FILE="$DOWNLOAD_DIR/$DATA_NAME.csv"
		
		# echo $DOWNLOAD_URL
		# echo $DOWNLOAD_FILE

		wget -O $DOWNLOAD_FILE $DOWNLOAD_URL
		for hour in `seq -f "%02g" 0 23`
		do
		    python -W ignore transition/generate_transition_graph.py "$hour:00" "`expr $hour + 1`:00" $DOWNLOAD_FILE $DB_FILE 2> $LOG_DATA_DIR"/"$hour"00-`expr $hour + 1`00.txt"
		done
		rm $DOWNLOAD_FILE
	done
done

