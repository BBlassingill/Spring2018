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


def euclideanDistance(trainingArray, testArray):
	distance = 0.0

	for i in range(len(trainingArray)-1):
 		distance += pow((float(trainingArray[i])-float(testArray[i])),2)
 		distance = math.sqrt(distance)
	return distance


def getNeighbors(trainingSet, testSet, k):
	# distances = []
	# length = len(testInstance)-1
	# for x in range(len(trainingSet)):
	# 	dist = euclideanDistance(testInstance, trainingSet[x], length)
	# 	distances.append((trainingSet[x], dist))
	# distances.sort(key=operator.itemgetter(1))
	# neighbors = []
	# for x in range(k):
	# 	neighbors.append(distances[x][0])
	# return neighbors
	

	for arr1 in testSet:
		distance = []
		knn = []

		class1 = 0
		class2 = 0
		class3 = 0
		class4 = 0
		class5 = 0

		for arr2 in trainingSet:
			eu_distance = euclideanDistance(arr1, arr2)
			distance.append((arr2[0], eu_distance))
			distance.sort(key = operator.itemgetter(1)) #sorting by the distance for each attribute

			knn = distance[:k] #returns everything from 0th position to the number of neighbors specified in the list

			for neighbor in knn:
				if (neighbor[0]) == '1':
					class1 += 1
				if (neighbor[0]) == '2':
					class2 += 1
				if (neighbor[0]) == '3':
					class3 += 1
				if (neighbor[0]) == '4':
					class4 += 1
				if (neighbor[0]) == '5':
					class5 += 1			

	# return (knn, testSet)
		numList = [class1, class2, class3, class4, class5]
		# print(str(numList))
		maxIndex = numList.index(max(numList))

		# if (maxIndex == 0):
		# 	arr1.insert(0, maxIndex)
		# if (maxIndex == 0):
		# 	arr1.insert(0, maxIndex)
		# if (maxIndex == 0):
		# 	arr1.insert(0, maxIndex)
		# if (maxIndex == 0):
		# 	arr1.insert(0, maxIndex)
		# if (maxIndex == 0):
		# 	arr1.insert(0, maxIndex)				
		# print(str(testIndex))
		arr1.insert(0, maxIndex+1)
		# print(str(arr1))
	return testSet

# def getResponse(neighbors):
# 	classVotes = {}
# 	for x in range(len(neighbors)):
# 		response = neighbors[x][-1]
# 		if response in classVotes:
# 			classVotes[response] += 1
# 		else:
# 			classVotes[response] = 1
# 	sortedVotes = sorted(classVotes.items(), key=operator.itemgetter(1), reverse=True)
# 	return sortedVotes[0][0]

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
	classified_testset = getNeighbors(trainingData_columns, testData_columns, 7)
	# global trainingData_columns
	file = open("result.txt", "w")
	file.writelines(str(classified_testset))
	file.close
	# print('Classified Test set: ' + str(classified_testset))
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