

#!/bin/bash
cd "$(dirname "$0")"



mkdir -p 'logs'
for i in {0..23}
do
    python generate_graph.py "$i:00" "`expr $i + 1`:00" ../../data/nyc_gov_trip_data/ ../../graph 2> "logs/"$i"00-`expr $i + 1`00.txt"
done
