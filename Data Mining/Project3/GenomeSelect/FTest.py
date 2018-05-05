import csv
import math
import numpy as np
import pandas as pd
from collections import defaultdict
from collections import Counter
from itertools import islice
from operator import itemgetter


def getData(filename) :
	data = []

	with open(filename, 'r') as f:
		reader = csv.reader(f, delimiter=",")
		for row in reader:
			data.append(row)

	X = np.array(data, dtype='float64')
	X = X.transpose()

	return X

def computeScore(data) :
	classLabels = set((data[:,0].astype('int64')))
	classLabelAssignments = data[:,0].astype('int64')
	counts = Counter(classLabelAssignments)
	featureNum = 1
	featureScores = {}
	slices = createSlices(data, counts)
	
	for feature in slices:
	  averages = []
	  variances = []
	  flat_list = [item for sublist in feature for item in sublist]
	  avgOfFeature = np.mean(flat_list)
	  lengthDict = {}
	  count = 0
	  
	  for slice in feature:
	    avg = calculateAverage(slice)
	    var = calculateVariance(slice)
	    averages.append(avg)
	    variances.append(var)
	    lengthDict[count] = len(slice)
	    count = count + 1

	  f_score = calculateFScore(avgOfFeature, averages, variances, lengthDict, len(flat_list))
	  featureScores[featureNum] = f_score#, feature#
	  featureNum = featureNum + 1
	 # print(f_score)
	  
	return featureScores
# 	print(featureScores)

def calculateFScore(avgOfFeature, averages, variances, lengthDict, lenOfFeature) :
  firstTerm = 0.0
  secondTerm = 0.0
  pow = 0.0
  index = 0

  for avg in averages:
    pow = pow + lengthDict[index]*math.pow((avg - avgOfFeature), 2)
    index = index + 1
    
  firstTerm = pow / (len(averages) - 1)

  for key in lengthDict :
    secondTerm = secondTerm + (lengthDict[key]-1)*variances[key]

  result = (firstTerm/(secondTerm/(lenOfFeature-(len(lengthDict)))))
  return result
  # print("final result: " + str(result))
  
	
def calculateAverage(slice) :
  return np.mean(slice)
  
def calculateVariance(slice) :
  # calculate by hand: https://stackoverflow.com/questions/35583302/how-can-i-calculate-the-variance-of-a-list-in-python
  return np.var(slice, ddof=1)
  
def createSlices(data, counts) :
	myDict  = {k: [] for k in set(data.T[0:1].astype('int64').flatten())}
	newData = data.T[1:]
	newData = newData[::-1,::-1]
	newData = np.flip(np.flip(newData, 0), 1)

	sliceList = list(counts.values())

	slices = []
	for row in newData:
		it = iter(row)
		slices.append([list(islice(it, 0, i)) for i in sliceList])

	return slices
	
def selectTopFeatures(data,scores,numFeatures) :
  features = []
  print(scores)
  scores = sorted(scores.items(), key=itemgetter(1), reverse = True)

  # print("printing sorted dictionary")

  
  # n_items = list(islice(scores, 2))
  # print(n_items)
  features = list(islice(scores, numFeatures))
  # print(scores)
def main() :
	data = getData('GenomeTrainXY.txt')
# 	print(data)
	scores = computeScore(data)
	selectTopFeatures(data, scores, 100)

main()					
