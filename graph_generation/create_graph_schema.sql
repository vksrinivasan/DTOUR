-- PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS node(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(60) UNIQUE, 
	latitude REAL, longitude REAL); 

CREATE TABLE IF NOT EXISTS adj_edge(id INTEGER PRIMARY KEY AUTO_INCREMENT, 
	src_node_id INTEGER, dest_node_id INTEGER,
	FOREIGN KEY(src_node_id) REFERENCES node(id),
	FOREIGN KEY(dest_node_id) REFERENCES node(id));
CREATE INDEX adj_src_dest_index on adj_edge(src_node_id, dest_node_id);
CREATE INDEX adj_dest_index on adj_edge(dest_node_id);

CREATE TABLE IF NOT EXISTS transition_group(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(60) UNIQUE);

CREATE TABLE IF NOT EXISTS transition_period(id INTEGER PRIMARY KEY AUTO_INCREMENT, 
	transition_group_id INTEGER, month INTEGER, year INTEGER, weight REAL DEFAULT 1, taxi_type VARCHAR(20),
	FOREIGN KEY(transition_group_id) REFERENCES transition_group(id),
	UNIQUE(transition_group_id, month, year, taxi_type));

CREATE TABLE IF NOT EXISTS transition_graph(id INTEGER PRIMARY KEY AUTO_INCREMENT, 
	transition_group_id INTEGER, interval_start INTEGER, interval_end INTEGER,
	FOREIGN KEY(transition_group_id) REFERENCES transition_group(id),
	UNIQUE(transition_group_id, interval_start, interval_end));

CREATE TABLE IF NOT EXISTS transition_edge(id INTEGER PRIMARY KEY AUTO_INCREMENT,
	transition_graph_id INTEGER, src_node_id INTEGER, 
	dest_node_id INTEGER, weight REAL,
	FOREIGN KEY(transition_graph_id) REFERENCES transition_graph(id),
	FOREIGN KEY(src_node_id) REFERENCES node(id),
	FOREIGN KEY(dest_node_id) REFERENCES node(id));
CREATE INDEX transition_src_index on transition_edge(transition_graph_id, src_node_id);
CREATE INDEX transition_dest_index on transition_edge(transition_graph_id, dest_node_id);