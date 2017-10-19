# Pass the following args in commandline
# 1: path/to/dataset.csv
# 2: path/to/output.json

from quantization import Quantizer

# Outline
# main function: reads csv
# load Quantizer
# For each line, take the origin/destination and quantize them using Quantizer that uses k quantization bins
# Add that to kxk matrix
# Once we're done calculate transition matrix and write to output.json
