import csv
import random
import math
import operator
from collections import defaultdict

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

def calculateAverages(testData_columns, trainingData_columns):
	testAverages = []
	trainingAverages = []
	# averages.update(label, average)
	# newTrainingArray = trainingArray[1:len(trainingArray)]
	# print(str(trainingData_columns))
	for col in trainingData_columns:
		sum = 0.0
		count = 0

		newCol = col[1:len(col)]
		for num in newCol:
			sum += int(num)
			count += 1
		average = sum/count
		# print(str(average))
		tup = (col[0], average)
		trainingAverages.append(tup)

	for col in testData_columns:
		sum = 0.0
		count = 0

		# newCol = col[1:len(col)]
		for num in col:
			sum += int(num)
			count += 1
		average = sum/count
		# print(str(average))
		# tup = (col[0], average)
		testAverages.append(average)

	return (testAverages, trainingAverages)

def calculateCentroid(testAverages, trainingAverages) :
	for avg in testAverages :
		#calculate euclidean distance

def main():
	# prepare data
	(trainingData_columns, testData_columns) = loadDataset()
	# findNearestCentroid(trainingData_columns, testData_columns)
	(testAverages, trainingAverages) = calculateAverages(testData_columns, trainingData_columns)
	calculateCentroid(testAverages, trainingAverages)
	# print(str(testAverages))
	# (testAverages, trainingAverages) = calculateCentroid(testData_columns, averages)
main()	