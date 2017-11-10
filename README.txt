# DTOUR
Path Optimization for Destination-Oriented Rideshare Drivers


Using MySQL

Servere-side Installation
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


Client-side Installation
1. Run the following commands
sudo apt-get install mysql-client
sudo apt-get install libmysqlclient-dev
sudo pip install MySql-python

2. Set up an automatic password when accessing the host. Be sure to include the quotation marks around the password when prompted.
mysql_config_editor set -G dtour -u dtour -p
>> '<Password Here>'  

3. Now you can access the database through shell with the following command.
mysql --login-path=dtour -D dtour

4. Modify the credentials.txt files to specify hostname and password
They are located in 'graph_generation/credentials.txt' and 'DTOURCode/src/main/resources/credentials.txt'.
Examples are provided as credentials_example.txt in the same directories.

