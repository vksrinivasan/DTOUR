import numpy as np
import pandas.io.sql as psql
import MySQLdb as mysql
import googlemaps
import time

from datetime import datetime

outputFileName = r'/home/vyas/GooglePaths/outpaths.log' # you should change this
apiKey = 'AIzaSyBO2w9zV-i-O32am4g5p7I0_eZSHC2gecs' # you should change this

def mileDistance(lat1, long1, lat2, long2):
	x = 69.1*(lat2-lat1)
	y = 53.0*(long2-long1)
	return np.sqrt((x*x)+(y*y))

# Connect to database get list of nodes
conn = mysql.connect(host='dtourdbserve.eastus2.cloudapp.azure.com', user='dtour', passwd='Letmein12345', db='dtour')
sqlString = "select * from node"
df = psql.read_sql(sqlString, conn)

# connect to gmaps api
gmaps = googlemaps.Client(key=apiKey)

# select 1000 random start and end points that are at least .75 miles away from each other. Query google maps api
# and get and store directions
numFound = 0

with open(outputFileName,'w') as file:
	while(numFound < 500):
		sample = df.sample(n=2)
		tempIdx = sample.index.values
		distance = mileDistance(sample.ix[tempIdx[0]].latitude, sample.ix[tempIdx[0]].longitude, sample.ix[tempIdx[1]].latitude,
								sample.ix[tempIdx[1]].longitude)

		if(distance > 1.00):
			numFound += 1
			src = (sample.ix[tempIdx[0]].latitude, sample.ix[tempIdx[0]].longitude)
			dest = (sample.ix[tempIdx[1]].latitude, sample.ix[tempIdx[1]].longitude)
			directions_result = gmaps.directions(src,dest,mode="driving", departure_time=datetime.now())

			# now just parse the directions results
			numSteps = directions_result[0]['legs'][0]['steps'].__len__()

			for i in range(0, numSteps):
				if(i == 0):
					file.write('(' + str(directions_result[0]['legs'][0]['steps'][i]['start_location']['lat']) + ',' +
							str(directions_result[0]['legs'][0]['steps'][i]['start_location']['lng']) + ')\t')

				file.write('(' + str(directions_result[0]['legs'][0]['steps'][i]['end_location']['lat']) + ',' +
						str(directions_result[0]['legs'][0]['steps'][i]['end_location']['lng']) + ')\t')

			file.write('\n')

		#time.sleep(1)

