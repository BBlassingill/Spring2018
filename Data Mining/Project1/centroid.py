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

def euclideanDistance(testAvg, trainingAverage):
	distance = 0.0

	distance = pow(float(testAvg) - float(trainingAverage), 2)
	distance = math.sqrt(distance)
	return distance	

def calculateAverages(testData_columns, trainingData_columns):
	testAverages = []
	trainingAverages = []

	for col in trainingData_columns:
		sum = 0.0
		count = 0

		newCol = col[1:len(col)]
		for num in newCol:
			sum += int(num)
			count += 1

		average = sum/count
		tup = (col[0], average)
		trainingAverages.append(tup)

	for col in testData_columns:
		sum = 0.0
		count = 0

		for num in col:
			sum += int(num)
			count += 1
		average = sum/count
		testAverages.append(average)

	return (testAverages, trainingAverages)

def calculateCentroid(testData_columns, testAverages, trainingAverages) :

	finalResult = []
	count = 0
	for avg in testAverages :
		distance = []

		for pair in trainingAverages :
			eu_distance = euclideanDistance(avg, pair[1])
			distance.append((pair[0], eu_distance))	

		distance.sort(key = operator.itemgetter(1))
		
		testData_columns[count].insert(0, distance[0][0])
		count += 1

	return testData_columns
def main():
	(trainingData_columns, testData_columns) = loadDataset()
	(testAverages, trainingAverages) = calculateAverages(testData_columns, trainingData_columns)
	finalResult = calculateCentroid(list(testData_columns), testAverages, trainingAverages)
main()	