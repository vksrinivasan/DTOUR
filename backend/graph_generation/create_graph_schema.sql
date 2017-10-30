PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS nodes(id INTEGER PRIMARY KEY, name TEXT UNIQUE, 
	latitude REAL, longitude REAL); 

CREATE TABLE IF NOT EXISTS adj_edges(id INTEGER PRIMARY KEY, 
	source_node_id INTEGER, dest_node_id INTEGER,
	FOREIGN KEY(source_node_id) REFERENCES nodes(id),
	FOREIGN KEY(dest_node_id) REFERENCES nodes(id));

CREATE TABLE IF NOT EXISTS transition_graphs(id INTEGER PRIMARY KEY, 
	dt_start TEXT, dt_end TEXT, taxi_type TEXT,
	interval_start INTEGER, interval_end INTEGER);

CREATE TABLE IF NOT EXISTS transition_edges(id INTEGER PRIMARY KEY,
	transition_graph_id INTEGER, source_node_id INTEGER, dest_node_id INTEGER, 
	FOREIGN KEY(transition_graph_id) REFERENCES transition_graphs(id),
	FOREIGN KEY(source_node_id) REFERENCES nodes(id),
	FOREIGN KEY(dest_node_id) REFERENCES nodes(id));
CREATE INDEX IF NOT EXISTS transition_edge_index on transition_edges(transition_graph_id);