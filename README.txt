DTOUR
Path Optimization for Destination-Oriented Rideshare Drivers

DESCRIPTION

The main code of this project is organized as follows:

--graph_generation: Code used to generate graphs from NYC taxi dataset and store them in MySQL
----create_graph_schema.sql: Schema structure of database.
----generate_graphs.sh: Glue script that generates calls adjacency graph generation if necessary, then transition graph generation on different month datasets.
----DB_SCHEMA_README.txt: Description of database schema.
----adjacency: Code used to generate adjacency graphs
------Manhattan_Adjacency_graph-Final.ipynb: Generates json file that includes street intersections and their adjacencies
------generate_adj_graph.py: Reads above generated json file and writes it to database
----transition: Code used to generate transition graphs
------quantization.py: Code that matches any given GPS coordinate to nearest node.
------generate_transition_graph.py: Uses PySpark to read taxi data csv files and create transition graphs from them, which it writes to database.
------graph_aggregation.py: Given transition graphs for different months that are stored in database, aggregates them.


INSTALLATION:
There are two portions of our application: frontend and backend.
Frontend:
	The frontend is hosted at "https://dva1-alhaamid.c9users.io/". Our frontend needs HTTPS and Java 1.8 for location settings so its hosted on Cloud9. If you wish to serve it on your own Node.js workspace on Cloud9, follow these instructions:
	1)	Copy the web/ folder in your workspace on Cloud9.
	2)	In the web/ folder, run this command: "node server.js"
	3)	To access the frontend, go to this url: https://<your_workspace_name>-<your_cloud9_username>.c9users.io/
	
Backend:
	The backend is coupled in a jar file that the frontend uses to compute paths. Behind the scenes, the jar file contacts the database hosted on Azure for graph computations.



MySQL Servere-side Installation - In order to run MySQL server
1. Install mysql server:
sudo apt-get install mysql-server 
# You can specify a root user password if you want

2. Open /etc/mysql/mysql.conf.d/mysqld.cnf
Change 'bind-address = 127.0.0.1' to 'bind-address = 0.0.0.0'
Restart

3. Restart MySQL with following commands
systemctl restart mysql.service

4.  Create database, user, and set permissions
mysql -u root -p << EOF
	create database dtour;
	create user 'dtour' identified by '<insert password here>';
	grant all on dtour.* to 'dtour';
EOF


MySQL Client-side Installation - In order to access MySQL server from a client
1. Run the following commands
sudo apt-get install mysql-client
sudo apt-get install libmysqlclient-dev
sudo pip install MySql-python

2. Set up an automatic password when accessing the host. Be sure to include the quotation marks around the password when prompted.
mysql_config_editor set -G dtour -u dtour -p
>> '<Password Here>'  

3. Now you can access the database through shell with the following command.
mysql --login-path=dtour -D dtour

4. Add the credentials.txt files to specify hostname and password
They are located in 'graph_generation/credentials.txt' and 'DTOURCode/src/main/resources/credentials.txt'.
Examples are provided as 'credentials_example.txt' in the same directories.


Graph generation
Run generate_graphs.sh to add adjacency edges and add transition_graphs for months betweeen Jan 2014 and Dec 2015 (Will take a while).
