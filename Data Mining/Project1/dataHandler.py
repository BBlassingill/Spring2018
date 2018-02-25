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
	X = X.transpose()
	print(X)			

	return dataSubset

# def splitData2TestTrain(filename, number_per_class, test_instances) :


# def	storeTrainXTrainY :


# def letter_2_digit_convert(charArray) :


def main() :
	pickDataClass('ATNT50/trainDataXY.txt', [1, 3])
main()	