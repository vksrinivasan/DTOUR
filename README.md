# DTOUR
Path Optimization for Destination-Oriented Rideshare Drivers

<img src="https://github.com/vksrinivasan/DTOUR/blob/master/DTOURCode/src/main/resources/DTOURScreenshot_Res.png"></img>



## Code Description
The main code of this project is organized as follows:

- DTOURCode/: 
	Backend code; given a start and end coordinate, returns list of best paths to take.
	The front-end calls the "main" class in the DTOUR jar with latitudes and longitudes.
	The "main" class calls the "mainHarness" class where the we run the K-Star Algorithm. 
	This consists of calls to the "AStar" class to run A* until the destination node is explored. 
	Then the code creates the connected path graph using side-track edge heaps by way of the 'TreeHeap' class's "createHtCollection" method. 
	Finally Dijkstra's algorithm is run on the path graph by way of the "DijkstraAlgorithm" class.
	Paths output by Dijkstra's are returned to the front end.  
	'Graphdigitizer' class is used to convert latitudes and longitudes into node numbers.
	'MySQLdatasorce' is used to connection to MySQL database.
	
- Experiments/: 
	Code used to run the trip simulation experiments.
	
- data/:
	Our scripts download the New York City Taxi datasets and save them in this directory.

- graph_generation/:
	Code used to generate graphs from NYC taxi dataset and store them in MySQL
  	--README.txt: Description of database schema and further details of each file.
 	--adjacency/: Code used to generate nodes (street intersections) and adjacency edges
 	--transition/: Code used to generate transition graphs and edges

- web/:
	This folder contains all the code for the frontend. You can find usage instructions in the INSTALLATION section below.


## Installation
### Frontend

Requirements
- Node.js(version 8.9.1 or above) installed
- Java 1.8 installed
- hosted on a HTTPS server
 
Our frontend needs HTTPS and Java 1.8 for location settings so its hosted on Cloud9. If you wish to serve it on your own Node.js workspace on Cloud9, follow these instructions:
- Copy the web/ folder in your workspace on Cloud9
- In the web/ folder, run this command to install all the dependencies: "npm install"
- In the web/ folder, run this command: "node server.js"
- To access the frontend, go to this url: https://<your_workspace_name>-<your_cloud9_username>.c9users.io/
	
### Backend

The backend is coupled in a jar file that the frontend uses to compute paths. Behind the scenes, the jar file contacts the database hosted on Azure for graph computations. This jar file is also in 'web/DTOurCode-all-1.0.jar'.

Jar consists of all the classes in DTour package and the following dependencies
- 'mysql:mysql-connector-java:5.1.44'
- 'org.apache.commons:commons-dbcp2:2.1.1'
- 'org.apache.commons:commons-pool:2.4.3'
- 'de.biomedical-imaging.edu.wlu.cs.levy.cg:kdtree:1.0.3'

Java Source and target compatibility-1.8
  
To Create Jar
- Install Gradle
- In the package DTOURCode, run gradle clean
- Run gradle fatJar. "fatJar" Gradle Task is written in Gradle build.Gradle.

### Data
Raw Data Sourced From NYC Transit Authority

#### Graph Generation
MySQL Server-side Installation
- Install mysql server: sudo apt-get install mysql-server 
- Open /etc/mysql/mysql.conf.d/mysqld.cnf
- Change 'bind-address = 127.0.0.1' to 'bind-address = 0.0.0.0'
- Restart MySQL with following commands systemctl restart mysql.service
- Create database, user, and set permissions:
			
			mysql -u root -p << EOF
			create database dtour;
			create user 'dtour' identified by '<insert password here>';
			grant all on dtour.* to 'dtour';
			EOF

MySQL Client-side Installation - In order to access MySQL server from a client
- Run the following commands
		
			sudo apt-get install mysql-client
			sudo apt-get install libmysqlclient-dev
			sudo pip install MySql-python
			
- Set up an automatic password when accessing the host. Be sure to include the quotation marks around the password when prompted:
			mysql_config_editor set -G dtour -u dtour -p
			>> '<Password Here>'
			
- Now you can access the database through shell with the following command:
			
			mysql --login-path=dtour -D dtour
			
- Add the 'credentials.txt' files to specify hostname and password. They are located in 'graph_generation/credentials.txt' and 'DTOURCode/src/main/resources/credentials.txt'.
		
Then run generate_graphs.sh to add adjacency edges and add transition_graphs for months betweeen Jan 2014 and Dec 2015 (Will take a while).
