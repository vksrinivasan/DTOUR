Generated graphs go here

Database schema

nodes
    id - primary key
    name - e.g. '1st St & 10th Avenue'
    latitude
    longitude

adj_edges
    id - primary key
    source_node_id - references node
    dest_node_id - references node

transition_graphs
    id - primary key
    dt_start - text representing start date of graph
    dt_end - text representing end date of graph
    interval_start - integer corresponding to minutes of start interval, e.g. 780 for 1pm
    interval_end - same as above except for end time

transition_edges
    id - primary key
    transition_graph_id - references transition_graph
    source_node_id - references node
    dest_node_id - references node