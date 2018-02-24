import csv
import random
import math
import operator
from collections import defaultdict


'''Necessary functions:
	loadDataset
	calculateDistance
	prediction
'''
# def loadDataset(filename, split, trainingSet=[] , testSet=[]):
# 	with open(filename, 'r') as csvfile:
# 	    lines = csv.reader(csvfile)
# 	    dataset = list(lines)
# 	    for x in range(len(dataset)-1):
# 	        for y in range(4):
# 	            dataset[x][y] = float(dataset[x][y])
# 	        if random.random() < split:
# 	            trainingSet.append(dataset[x])
# 	        else:
# 	            testSet.append(dataset[x])

# global trainingData_columns
# trainingData_columns = {}
# global testData_columns
# testData_columns = {}

def loadDataset():

	# with open("ATNT50/trainDataXY.txt", "r") as trainingData:
	# 	trainingLines = csv.reader(trainingData)
	# 	columns = zip(*trainingLines)
	trainingData = defaultdict(list)
	with open('ATNT50/trainDataXY.txt', 'r') as f:
		reader = csv.reader(f, delimiter=',')
		for row in reader:
			for i in range(len(row)):
				trainingData[i].append(row[i])
	trainingData_columns = trainingData.values()

	testData = defaultdict(list)
	with open('ATNT50/testDataX.txt', 'r') as f:
		reader = csv.reader(f, delimiter=',')
		for row in reader:
			for i in range(len(row)):
				testData[i].append(row[i])
	testData_columns = testData.values()

	return (trainingData_columns, testData_columns) 				



def euclideanDistance(instance1, instance2, length):
	distance = 0
	for x in range(length):
		distance += pow((instance1[x] - instance2[x]), 2)
	return math.sqrt(distance)

def getNeighbors(trainingSet, testInstance, k):
	distances = []
	length = len(testInstance)-1
	for x in range(len(trainingSet)):
		dist = euclideanDistance(testInstance, trainingSet[x], length)
		distances.append((trainingSet[x], dist))
	distances.sort(key=operator.itemgetter(1))
	neighbors = []
	for x in range(k):
		neighbors.append(distances[x][0])
	return neighbors

def getResponse(neighbors):
	classVotes = {}
	for x in range(len(neighbors)):
		response = neighbors[x][-1]
		if response in classVotes:
			classVotes[response] += 1
		else:
			classVotes[response] = 1
	sortedVotes = sorted(classVotes.items(), key=operator.itemgetter(1), reverse=True)
	return sortedVotes[0][0]

def getAccuracy(testSet, predictions):
	correct = 0             
	for x in range(len(testSet)):
		if testSet[x][-1] == predictions[x]:
			correct += 1
	print("num correct" + str(correct))    
	return (correct/float(len(testSet))) * 100.0  

def main():
	# prepare data
	(trainingData_columns,testData_columns) = loadDataset()
	# global trainingData_columns
	print('Train set: ' + str(trainingData_columns))
	# trainingSet=[]
	# testSet=[]
	# split = 0.67
	# loadDataset('iris.data', split, trainingSet, testSet)
	# print('Train set: ' + repr(len(trainingSet)))
	# print('Test set: ' + repr(len(testSet)))
	# # generate predictions
	# predictions=[]
	# k = 3
	# for x in range(len(testSet)):
	# 	neighbors = getNeighbors(trainingSet, testSet[x], k)
	# 	result = getResponse(neighbors)
	# 	predictions.append(result)
	# 	print('> predicted=' + repr(result) + ', actual=' + repr(testSet[x][-1]))
	# accuracy = getAccuracy(testSet, predictions)
	# print('Accuracy: ' + repr(accuracy) + '%')
	
main()