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


def euclideanDistance(testArray, trainingArray):
	distance = 0.0

	newTrainingArray = trainingArray[1:len(trainingArray)]

	for i in range(len(newTrainingArray)):
		distance += pow((float(testArray[i])-float(newTrainingArray[i])),2)
	distance = math.sqrt(distance)
	return distance


def getNeighbors(testSet, trainingSet, k):

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

		numList = [class1, class2, class3, class4, class5]
		maxIndex = numList.index(max(numList))
		arr1.insert(0, maxIndex+1)
		
	return testSet

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
	classified_testset = getNeighbors(testData_columns, trainingData_columns, 7)
	# file = open("result.txt", "w")
	# file.writelines(str(classified_testset))
	# file.close
	# print('Classified Test set: ' + str(classified_testset))
	# euclideanDistance(testData_columns, trainingData_columns);
main()