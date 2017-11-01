#!/bin/bash
LOG_DIR='logs'
BASE_URL='https://s3.amazonaws.com/nyc-tlc/trip+data'
DOWNLOAD_DIR='../data/nyc_gov_trip_data'
INTERVAL_LENGTH=60

cd "$(dirname "$0")"
export PYTHONPATH=`realpath ../`

credentials=$(cat  credentials.txt |tr "\n" " ")
host=`echo $credentials | awk '{print $1;}'`
db=`echo $credentials | awk '{print $2;}'`
user=`echo $credentials | awk '{print $3;}'`

num_tables=`mysql -h $host -D $db -u $user <<<"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='dtour';"`
num_tables=`echo $num_tables | egrep -o '[0-9]+'`

if [ "$num_tables" -eq 0 ]; then
	mysql -h $host -D $db -u $user  < create_graph_schema.sql
	python adjacency/generate_adj_graph.py
fi

mkdir -p $LOG_DIR
for year in {2014..2015}
do
	for month in `seq -f "%02g" 1 12`
	do
		DATA_NAME="yellow_tripdata_$year-$month"
		LOG_DATA_FILE="$LOG_DIR/$DATA_NAME.txt"

		DOWNLOAD_URL="$BASE_URL/$DATA_NAME.csv"
		DOWNLOAD_FILE="$DOWNLOAD_DIR/$DATA_NAME.csv"
		
		# echo $DOWNLOAD_URL
		# echo $DOWNLOAD_FILE

		# wget -O $DOWNLOAD_FILE $DOWNLOAD_URL
		python -W ignore transition/generate_transition_graph.py $INTERVAL_LENGTH $DOWNLOAD_FILE 2> $LOG_DATA_FILE
		# rm $DOWNLOAD_FILE
	done
done

