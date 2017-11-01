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
	create user 'dtour' identified by 'dtour_1!2@3#';
	grant all on dtour.* to 'dtour';
EOF


Client-side Installation
1. Run the following commands
sudo apt-get install mysql-client
sudo apt-get install libmysqlclient-dev
sudo pip install MySql-python

2. It would be useful to specify credentials specific to the database.
I have yet to test out https://dba.stackexchange.com/questions/3889/is-it-possible-to-have-passwords-configured-per-database-or-per-host-in-my-cnf

