inputFileName = r'Data/googleMapsPaths.txt'
outputFileName = r'Data/googleMapsPathsExpanded.txt'

with open(outputFileName, 'w') as writeFile:
    with open(inputFileName, 'r') as readFile:

        line = readFile.readline()
        indicator = 0

        while line:
            print indicator
            indicator += 1
            ls_temp = []
            line = line.split('\t')
            line_adj = [x.strip('"').strip('(').strip(')') for x in line]
            line_adj = [x.replace('(','').replace(')','').replace('"','').replace('\n','') for x in line_adj]
            line_adj = [x for x in line_adj if x != '' and x != '\n']

            prev_lat = 0.0
            prev_long = 0.0
            for i in range(0,line_adj.__len__()):
                if(i == 0):
                    pair = line_adj[i].split(',')
                    prev_lat = float(pair[0])
                    prev_long = float(pair[1])
                    ls_temp.append(line_adj[i])

                else:
                    pair = line_adj[i].split(',')
                    curr_lat = float(pair[0])
                    curr_long = float(pair[1])

                    incremental_lat = (curr_lat - prev_lat)/10.0
                    incremental_lon = (curr_long - prev_long)/10.0

                    new_lat = prev_lat
                    new_long = prev_long
                    for j in range(0,9):
                        new_lat += incremental_lat
                        new_long += incremental_lon
                        ls_temp.append(str(new_lat) + ',' + str(new_long))

                    ls_temp.append(line_adj[i])


                    prev_lat = curr_lat
                    prev_long = curr_long

            for z in range(0, ls_temp.__len__()):
                writeFile.write(ls_temp[z] + '\t')

            writeFile.write('\n')

            line = readFile.readline()
