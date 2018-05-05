import csv
import numpy as np
import pandas as pd
from collections import defaultdict
from collections import Counter
from itertools import islice

def getData(filename) :
	data = []

	with open(filename, 'r') as f:
		reader = csv.reader(f, delimiter=",")
		for row in reader:
			data.append(row)

	X = np.array(data, dtype='float64')
	X = X.transpose()

	return X

def computeVariance(data) :
	classLabels = set((data[:,0].astype('int64')))
	classLabelAssignments = data[:,0].astype('int64')
	counts = Counter(classLabelAssignments)
	# print(dict(test))

	myDict = createDict(data, counts)
	# print()
	# print(data[::-1,::-1].T)
	# print()
	# data = data.T[1:]
	# print(data[::-1,::-1])
	

def createDict(data, counts) :
	myDict  = {k: [] for k in set(data.T[0:1].astype('int64').flatten())}
	newData = data.T[1:]
	newData = newData[::-1,::-1]
	newData = np.flip(np.flip(newData, 0), 1)

	sliceList = list(counts.values())

	slices = []
	for row in newData:
		it = iter(row)
		slices.append([list(islice(it, 0, i)) for i in sliceList])
	print(slices[0])

	numClasses = len(counts)

	for vector in slices:
		for x in range(numClasses):
			myDict[x+1].append(vector[x])

	return myDict

def main() :
	data = getData('GenomeTrainXY.txt')
	print(data)
	computeVariance(data)

main()					