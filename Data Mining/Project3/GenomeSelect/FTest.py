import csv
import math
import numpy as np
import dataHandler as dh
import pandas as pd
import CustomKNNClassifier as knn
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
  featureResults = []
  newData = data.T[1:]
  # print(data.T[0:1])
  featureResults.append(list(data.T[0:1][0]))
  # print(featureResults)
  # newData = newData[::-1,::-1]
  # newData = np.flip(np.flip(newData, 0), 1)

  scores = sorted(scores.items(), key=itemgetter(1), reverse = True)
  features = list(islice(scores, numFeatures))
  
  # print("The top " + str(numFeatures) + " features are as follows:")
  for featureNum, fScore in features :
    featureResults.append(list(newData[featureNum-1]))
    # print(newData[featureNum-1])
    # print("Feature number: " + str(featureNum) + " \tF-score: " + str(fScore))
  
  # print(np.asarray(featureResults))  
  return np.asarray(featureResults)
def main() :
	data = getData('GenomeTrainXY.txt')
	scores = computeScore(data)
	#Task A
	topFeatures = selectTopFeatures(data, scores, 100)
# 	print(topFeatures)

	#Task B
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(topFeatures.T, 40, 4)

  # X_train, y_train, X_test, y_test = dh.splitData2TestTrain(topFeatures, 40, 4434)
	#Task C
	model = knn.CustomKNNClassifier(neighbors=2)
	model = model.fit(X_train, y_train)
	predicted_labels = model.predict(X_test)
	print(predicted_labels)
	print()
	print(y_test)

main()					
