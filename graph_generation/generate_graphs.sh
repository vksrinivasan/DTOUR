#!/bin/bash
ROOT_DIR="`dirname "$0"`/.."
ROOT_DIR=`realpath $ROOT_DIR`

LOG_DIR="$ROOT_DIR/graph_generation/logs"
BASE_URL='https://s3.amazonaws.com/nyc-tlc/trip+data'
DOWNLOAD_DIR="$ROOT_DIR/data/nyc_gov_trip_data"
INTERVAL_LENGTH=60

export PYTHONPATH=`realpath "$ROOT_DIR/graph_generation"`

credentials=`cat  $ROOT_DIR/graph_generation/credentials.txt |tr "\n" " "`
host=`echo $credentials | awk '{print $1;}'`
db=`echo $credentials | awk '{print $2;}'`

num_tables=`mysql --login-path='dtour' -D $db <<<"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='dtour';"`
num_tables=`echo $num_tables | egrep -o '[0-9]+'`

if [ "$num_tables" -eq 0 ]; then
	mysql --login-path='dtour' -h $host -D $db < $ROOT_DIR/graph_generation/create_graph_schema.sql
	python $ROOT_DIR/graph_generation/adjacency/generate_adj_graph.py
fi

mkdir -p $LOG_DIR
for year in {2014..2015}
do
	for month in `seq -f "%02g" 1 12`
	do
		DATA_NAME="yellow_tripdata_$year-$month"
		LOG_DATA_FILE="$LOG_DIR/$DATA_NAME.log"

		DOWNLOAD_URL="$BASE_URL/$DATA_NAME.csv"
		DOWNLOAD_FILE="$DOWNLOAD_DIR/$DATA_NAME.csv"
		
		# echo $DOWNLOAD_URL
		# echo $DOWNLOAD_FILE

		wget -O $DOWNLOAD_FILE $DOWNLOAD_URL
		python -W ignore $ROOT_DIR/graph_generation/transition/generate_transition_graph.py $INTERVAL_LENGTH $DOWNLOAD_FILE &> $LOG_DATA_FILE
		rm $DOWNLOAD_FILE
	done
done

# python -W ignore $ROOT_DIR/graph_generation/transition/generate_transition_graph.py $INTERVAL_LENGTH $ROOT_DIR/data/nyc_gov_trip_data/yellow_tripdata_2016-03_min.csv

