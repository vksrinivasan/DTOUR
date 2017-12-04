DTOUR
Path Optimization for Destination-Oriented Rideshare Drivers

DESCRIPTION:
The main code of this project is organized as follows:

--DTOURCode/: 
	Backend code; given a start and end coordinate, returns list of best paths to take.
	The front-end calls the "main" class in the DTOUR jar with latitudes and longitudes.
	The "main" class calls the "mainHarness" class where the we run the K-Star Algorithm. 
	This consists of calls to the "AStar" class to run A* until the destination node is explored. 
	Then the code creates the connected path graph using side-track edge heaps by way of the 'TreeHeap' class's "createHtCollection" method. 
	Finally Dijkstra's algorithm is run on the path graph by way of the "DijkstraAlgorithm" class.
	Paths output by Dijkstra's are returned to the front end.  
	'Graphdigitizer' class is used to convert latitudes and longitudes into node numbers.
	'MySQLdatasorce' is used to connection to MySQL database.
	
--Experiments/: 
	Code used to run the trip simulation experiments.
	
--data/:
	Our scripts download the New York City Taxi datasets and save them in this directory.

--graph_generation/:
	Code used to generate graphs from NYC taxi dataset and store them in MySQL
  	--README.txt: Description of database schema and further details of each file.
 	--adjacency/: Code used to generate nodes (street intersections) and adjacency edges
 	--transition/: Code used to generate transition graphs and edges

--web/:
	This folder contains all the code for the frontend. You can find usage instructions in the INSTALLATION section below.


INSTALLATION:
Frontend:
	The front end has the following requirements:
		1)  Node.js(version 8.9.1 or above) installed
		2)  Java 1.8 installed
		3)  hosted on a HTTPS server

	The frontend is hosted at "https://dva1-alhaamid.c9users.io/" until 31st Dec. Note that for testing purposes only, we disabled the current location feature in the demo web app at this url since one can not test the application for New York city while the map is centered in Atlanta.
 
	Our frontend needs HTTPS and Java 1.8 for location settings so its hosted on Cloud9. If you wish to serve it on your own Node.js workspace on Cloud9, follow these instructions:
		1)	Copy the web/ folder in your workspace on Cloud9.
		2)	In the web/ folder, run this command to install all the dependencies: "npm install"
		3)	In the web/ folder, run this command: "node server.js"
		4)	To access the frontend, go to this url: https://<your_workspace_name>-<your_cloud9_username>.c9users.io/
	
Backend:
	The backend is coupled in a jar file that the frontend uses to compute paths. Behind the scenes, the jar file contacts the database hosted on Azure for graph computations. Other details are as follows:
	This jar file is also in 'web/DTOurCode-all-1.0.jar'
Jar Creation:
	Jar consists of all the classes in DTour package and the following dependencies
		  'mysql:mysql-connector-java:5.1.44'
		  'org.apache.commons:commons-dbcp2:2.1.1'
		  'org.apache.commons:commons-pool:2.4.3'
		  'de.biomedical-imaging.edu.wlu.cs.levy.cg:kdtree:1.0.3'

	Java Source and target compatibility-1.8
  
  Steps To create Jar-
  		 1)  Install Gradle
		 2)  In the package DTOURCode, run gradle clean
		 3)  Run gradle fatJar
  "FatJar" Gradle Task is written in Gradle build.Gradle.
	
Graph Generation:
	MySQL Server-side Installation - In order to run MySQL server
		1) Install mysql server:
		sudo apt-get install mysql-server 
		# You can specify a root user password if you want
		2) Open /etc/mysql/mysql.conf.d/mysqld.cnf
		Change 'bind-address = 127.0.0.1' to 'bind-address = 0.0.0.0'
		Restart
		3) Restart MySQL with following commands
		systemctl restart mysql.service
		4)  Create database, user, and set permissions
		mysql -u root -p << EOF
			create database dtour;
			create user 'dtour' identified by '<insert password here>';
			grant all on dtour.* to 'dtour';
		EOF


	MySQL Client-side Installation - In order to access MySQL server from a client
		1) Run the following commands
		sudo apt-get install mysql-client
		sudo apt-get install libmysqlclient-dev
		sudo pip install MySql-python
		2) Set up an automatic password when accessing the host. Be sure to include the quotation marks around the password when prompted.
		mysql_config_editor set -G dtour -u dtour -p
		>> '<Password Here>'  
		3) Now you can access the database through shell with the following command.
		mysql --login-path=dtour -D dtour
		4) Add the 'credentials.txt' files to specify hostname and password
		They are located in 'graph_generation/credentials.txt' and 'DTOURCode/src/main/resources/credentials.txt'.
		Examples are provided as 'credentials_example.txt' in the same directories.
		
	Then run generate_graphs.sh to add adjacency edges and add transition_graphs for months betweeen Jan 2014 and Dec 2015 (Will take a while).
	If you want to access our database, which has transition graphs already constructed, copy the following into the 'credentials.txt' files.
		dtourdbserve.eastus2.cloudapp.azure.com
		dtour
		dtour
		Letmein12345
	These lines correspond to hostname, database name, username, and password respectively (in case you want to manually query and examine our database).

	
EXECUTION: 
	You can access the frontend of our application at "https://dva1-alhaamid.c9users.io/". If you have 
	spun up your own web server on which the packaged backend jar code sits, you should be able to execute the 
	code from your respective web page. Note that this assumes you have a separate database server running 
	that the jar database credentials point to.
