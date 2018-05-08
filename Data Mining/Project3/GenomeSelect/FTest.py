import csv
import math
import numpy as np
import dataHandler as dh
import pandas as pd
import CustomKNNClassifier as knn
import CustomCentroidClassifier as centroid
import LinearRegression as linear

from sklearn import svm

import itertools 
from collections import defaultdict
from collections import Counter
from itertools import islice
from operator import itemgetter
from sklearn.linear_model import LinearRegression
from numpy.linalg import eig
from sklearn.preprocessing import StandardScaler


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

  featureNums =[]
  
  # print("The top " + str(numFeatures) + " features are as follows:")
  for featureNum, fScore in features :
    featureResults.append(list(newData[featureNum-1]))
    featureNums.append(featureNum)
    # print(newData[featureNum-1])
    # print("Feature number: " + str(featureNum) + " \tF-score: " + str(fScore))
  
  # print(np.asarray(featureResults))  
  return np.asarray(featureResults), featureNums

def selectTopFeaturesOfTestData(X_train, featureNums, filename) :
	data = []

	with open(filename, 'r') as f:
		reader = csv.reader(f, delimiter=",")
		for row in reader:
			data.append(row)

	X = np.array(data, dtype='float64')
	# print(featureNums)
	newList = []

	for index in featureNums :
		newList.append(X[index-1])

	newX = np.array(newList)
	
	return newX	

	# for row1 in X_train :
	# 	# for row2 in X :
	# 	# 	if (row1 == row2) :
	# 	# 		newList.append(row2)
	# 	# print(row1)
	# 	if (row1 in X_train) :
	# 		newList.append(row1) 

	# wtf = np.in1d(X_train, X)
	# print(X_train)
	# print()
	# print(newList)
	# print(wtf)
	# print()
	# print(X.flatten())
	# print()
	

def main() :
	data = getData('GenomeTrainXY.txt')
	scores = computeScore(data)
	#Task A
	topFeatures, featureNums = selectTopFeatures(data, scores, 100)

	X_train = topFeatures[1:]
	y_train = topFeatures[0]


	X_test = selectTopFeaturesOfTestData(X_train, featureNums, 'GenomeTestX.txt')

	model = knn.CustomKNNClassifier(neighbors=3)
	model = model.fit(X_train.T, y_train.T)
	predicted_labels = model.predict(X_test.T)
	print(predicted_labels)

	model = centroid.CustomCentroidClassifier()
	model = model.fit(X_train.T, y_train.T)
	predicted_labels = model.predict(X_test.T)
	print(predicted_labels)

	model = svm.SVC(kernel='linear')
	model = model.fit(X_train.T, y_train.T)
	predicted_labels = model.predict(X_test.T)
	print(predicted_labels)

	print("my linear regression")
	model = linear.LinearRegression()
	model = model.fit(X_train.T, y_train.T)
	predicted_labels = model.predict(X_test.T)
	print(predicted_labels)

	print("model linear regression")
	model = LinearRegression()
	model = model.fit(X_train.T, y_train.T)
	predicted_labels = model.predict(X_test.T)
	roundedLabels = [round(x) for x in predicted_labels]
	print(roundedLabels)

	# x = np.array([[8, -20], [0, -1], [10, -19], [10, -20], [2, 0]])
	# print(x)
	# print()
	# # Compute mean and coviariance matrix for D
	# M = np.mean(x, axis=0)
	# print(M)
	# print()
	# c1 = x - M
	# print(c1)
	# print()
	# c = np.cov(c1)
	# print(c)
	# print()

	# values, vectors = eig(c)
	# print(values)
	# print(vectors)
	# Compute eigenvalues of covariance matrix
	# What is the intrinsic dimensionality of this dataset?
	# Compute the first principal component

	#PCA with real data: https://plot.ly/ipython-notebooks/principal-component-analysis/

	#Load the data set
	df = pd.read_csv(
    	filepath_or_buffer='https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data', 
    	header=None, 
    	sep=',')

	df.columns=['sepal_len', 'sepal_wid', 'petal_len', 'petal_wid', 'class']
	df.dropna(how="all", inplace=True) # drops the empty line at file-end

	# Split data into X_train and y_train
	X = df.ix[:,0:4].values
	y = df.ix[:,4].values

	#Standardize data - may not need to do this step on computer quiz
	X_std = StandardScaler().fit_transform(X)

	#The eigenvectors (principal components) determine the directions of the new feature space, and the eigenvalues determine their magnitude. In other words, the eigenvalues explain the variance of the data along the new feature axes.

	#Compute covariance matrix
	cov_mat = np.cov(X_std.T)

	#Compute eigenvalues and eigenvectors
	eig_vals, eig_vecs = eig(cov_mat)


	#Compute instrinsic dimensionality - I guess it's the number of positive eigenvalues?

	#Compute the first principal component
	#The eigenvectors with the lowest eigenvalues bear the least information about the distribution of the data; those are the ones can be dropped.
	#In order to do so, the common approach is to rank the eigenvalues from highest to lowest in order choose the top k eigenvectors.

	# Make a list of (eigenvalue, eigenvector) tuples
	eig_pairs = [(np.abs(eig_vals[i]), eig_vecs[:,i]) for i in range(len(eig_vals))]

	# Sort the (eigenvalue, eigenvector) tuples from high to low
	eig_pairs.sort()
	eig_pairs.reverse()

	print("The first principal component is: " + str(eig_pairs[0][1]))
main()					
