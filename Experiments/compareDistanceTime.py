from datetime import datetime
import googlemaps

inputFileName = r'Data/pathOut.txt'
outputFileNameDistance_ours = r'Data/DistanceOurs.txt'
outputFileNameDistance_google = r'Data/DistanceGoogle.txt'
outputFileNameTime_ours = r'Data/TimeOurs.txt'
outputFileNameTime_google = r'Data/TimeGoogle.txt'
apiKey = 'AIzaSyBO2w9zV-i-O32am4g5p7I0_eZSHC2gecs'

with open(outputFileNameDistance_ours, 'w') as ourDistance:
    with open(outputFileNameDistance_google, 'w') as googleDistance:
        with open(outputFileNameTime_ours, 'w') as ourTime:
            with open(outputFileNameTime_google, 'w') as googleTime:
                with open(inputFileName, 'r') as readFile:
                    line = readFile.readline()
                    indicator = 0

                    gmaps = googlemaps.Client(key=apiKey)

                    while line:
                        print indicator
                        indicator += 1
                        line = line.split('\t')
                        line = line[1:]
                        line = [x for x in line if x != '\n']
                        line = [x.strip('(').strip(')') for x in line]
                        ls_lat_long = [x.split(',') for x in line]
                        for coordPair in ls_lat_long:
                            coordPair[0] = float(coordPair[0])
                            coordPair[1] = float(coordPair[1])

                        tuple_of_tuples = tuple(tuple(x) for x in ls_lat_long)

                        # get the directions_result for the google maps path
                        directions_result = gmaps.directions(tuple_of_tuples[0], tuple_of_tuples[-1], mode="driving", departure_time=datetime.now())
                        temp_google_distance = directions_result[0]['legs'][0]['distance']['value']
                        temp_google_time = directions_result[0]['legs'][0]['duration']['value']

                        if(tuple_of_tuples.__len__() == 2):
                            temp_our_distance = temp_google_distance
                            temp_our_time = temp_google_time
                        elif(tuple_of_tuples.__len__() == 3):
                            directions_result_us = gmaps.directions(tuple_of_tuples[0], tuple_of_tuples[-1],
                                                                    waypoints=tuple_of_tuples[1], mode="driving",
                                                                    departure_time=datetime.now())
                            temp_our_distance = 0.0
                            temp_our_time = 0.0
                            for leg in directions_result_us[0]['legs']:
                                temp_our_distance += leg['distance']['value']
                                temp_our_time += leg['duration']['value']
                        else:
                            directions_result_us = gmaps.directions(tuple_of_tuples[0], tuple_of_tuples[-1],
                                                                    waypoints=ls_lat_long, mode="driving",
                                                                    departure_time=datetime.now())
                            temp_our_distance = 0.0
                            temp_our_time = 0.0
                            for leg in directions_result_us[0]['legs']:
                                temp_our_distance += leg['distance']['value']
                                temp_our_time += leg['duration']['value']

                        ourDistance.write(str(temp_our_distance) + "\n")
                        googleDistance.write(str(temp_google_distance) + "\n")
                        ourTime.write(str(temp_our_time) + "\n")
                        googleTime.write(str(temp_google_time) + "\n")

                        line = readFile.readline()
