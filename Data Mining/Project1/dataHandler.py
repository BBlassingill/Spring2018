from sklearn.model_selection import train_test_split
import csv
import numpy as np

def pickDataClass(filename, class_ids) :
	#class_ids: contains the classes to be picked. ex: (3, 5, 8, 9)
	
	dataSubset = []
	originalData = []

	with open(filename, 'r') as f:
		reader = csv.reader(f, delimiter=',')
		for row in reader:
			originalData.append(row)

	X = np.array(originalData, dtype='int64')
	X = X.transpose()

	for row in X:
		if row[0] in class_ids:
			dataSubset.append(row)

	X = np.array(dataSubset, dtype='int64')
	dataSubset = X.transpose()	

	return dataSubset

def splitData2TestTrain(filename, number_per_class, test_instances) :
	originalData = []
	with open(filename, 'r') as f:
		reader = csv.reader(f, delimiter=',')
		for row in reader:
			originalData.append(row)

	X = np.array(originalData[1:], dtype='int64').transpose()
	Y = np.array(originalData[0:1]).transpose()

	splitRatio = test_instances/number_per_class

	X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size = splitRatio)

	return X_train, y_train, X_test, y_test

# def	storeTrainXTrainY :


# def letter_2_digit_convert(charArray) :


def main() :
	# subsetData = pickDataClass('ATNT50/trainDataXY.txt', [1, 3])
	X_train, y_train, X_test, y_test = splitData2TestTrain('ATNT50/trainDataXY.txt', 9, 5)

main()	