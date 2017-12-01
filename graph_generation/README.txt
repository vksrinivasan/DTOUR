Database schema
node
    id - primary key
    name (unique, indexed) - e.g. '1st St & 10th Avenue'
    latitude
    longitude


adj_edge
    id - primary key
    source_node_id - references node
    dest_node_id - references node

    (source_node_id, dest_node_id) is indexed


transition_group
    id - primary key
    name (unique, indexed) - e.g. 'yellow_trip_data_2014_02'


transition_period
    id - primary key
    transition_group_id (unique, indexed)  - references transition_group
    month - month that this corresponds to
    year - year that data corresponds to
    taxi_type - e.g. 'yellow', 'green'
	weight - weight assigned to transition graph edges of this transition_period.
    unique on (transition_group_id, month, year, taxi_type)


transition_graph
    id - primary key
    transition_group_id (unique, indexed)  - references transition_group_id
    interval_start - integer corresponding to minutes of start interval, e.g. 780 for 1pm
    interval_end - same as above except for end time

    unique on (transition_group_id, interval_start, interval_end)


transition_edge
    id - primary key
    transition_graph_id (unique, indexed)  - references transition_graph
    source_node_id - references node
    dest_node_id - references node
    weight - integer weight

    (transition_graph_id, source_node_id) is indexed
    (transition_graph_id, dest_node_id) is indexed
	
	
Directory structure.
--graph_generation: Code used to generate graphs from NYC taxi dataset and store them in MySQL
  --create_graph_schema.sql: Schema structure of database.
  --generate_graphs.sh: Glue script that generates calls adjacency graph generation if necessary, then transition graph generation on different month datasets.
	  Once you have the database, user, and 'credentials.txt' file setup, you should be able to simply run this file to generate adjacency and transition graphs.
	  Note that this does take a long while though. If you are running on an AWS EMR cluster, add the '-c' to use YARN cluster management.
  --adjacency: Code used to generate nodes (street intersections) and adjacency edges
    --Manhattan_Adjacency_graph-Final.ipynb: Generates json file that includes street intersections and their adjacencies
    --generate_adj_graph.py: Reads above generated json file and writes it to database
  --transition: Code used to generate transition graphs
    --quantization.py: Code that matches any given GPS coordinate to nearest node.
    --generate_transition_graph.py: Uses PySpark to read taxi data csv files and create transition graphs from them, which it writes to database.
    --graph_aggregation.py: Given transition graphs for different months that are stored in database, aggregates them.
  --mysql_util.py: Code to connect to server.