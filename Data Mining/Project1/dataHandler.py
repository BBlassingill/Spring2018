from sklearn.model_selection import train_test_split
import csv
import numpy as np
import scipy.io as io
import string

def pickDataClass(filename, class_ids) :
	
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

	X = np.array(originalData[1:], dtype='int64')
	Y = np.array(originalData[0:1])

	print(X)
	print(Y)

	X = X.transpose()
	Y = Y.transpose()

	print(X)
	print(Y)

	splitRatio = test_instances/number_per_class

	X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size = splitRatio)

	return X_train, y_train, X_test, y_test

def	storeTrainXTrainY(X_train, y_train, X_test, y_test, filetype, testName) :
	
	train_data = np.column_stack((X_train, y_train)).transpose()
	train_data = np.flip(train_data, 0)
	
	test_data = np.column_stack((X_test, y_test)).transpose()
	test_data = np.flip(test_data, 0)


	if(filetype == "matlab"):
		io.savemat("trainData-"+ testName+ ".mat", mdict={"trainData": train_data.astype(int).tolist()})
		io.savemat("testData-"+ testName+ ".mat" , mdict={"testData": test_data.astype(int).tolist()})

	else :
		file = open("trainData-"+ testName+ ".txt", "w")
		file2 = open("testData-"+ testName+ ".txt", "w")
		file.writelines(str(train_data.tolist()))
		file.close
		file2.writelines(str(test_data.tolist()))
		file2.close

def letter_2_digit_convert(charString) :
	resultArray = []

	for char in charString:
		char = char.lower()
		resultArray.append(string.ascii_lowercase.index(char) + 1)

	return resultArray

def main() :
	subsetData = pickDataClass('ATNT50/trainDataXY.txt', [1])
	X_train, y_train, X_test, y_test = splitData2TestTrain('ATNT50/trainDataXY.txt', 9, 5)
	# storeTrainXTrainY(X_train, y_train, X_test, y_test, "matlab")
	# letter_2_digit_convert("ACFG")

main()	