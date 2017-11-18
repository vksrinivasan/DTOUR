Generated graphs go here

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