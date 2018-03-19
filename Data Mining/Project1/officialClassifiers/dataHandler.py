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
	# print("original data")
	# print(X)
	# print("original data shape")
	# print(X.shape)
	X = X.transpose()
	# print("After transpose")
	# print(X)
	for row in X:
		if row[0] in class_ids:
			dataSubset.append(row)

	X = np.array(dataSubset, dtype='int64')
	# dataSubset = X.transpose()

	# print("final subset")
	# print(dataSubset)
	# print(dataSubset.shape)	

	return X

def splitData2TestTrain(originalData, number_per_class, test_instances) :
	
	X_transposed = originalData.transpose()

	X = np.array(X_transposed[1:], dtype='int64').transpose()
	Y = np.array(X_transposed[0:1], dtype='int64').transpose()

	r,c = Y.shape
	Y = Y.reshape(r,)

	# splitRatio = test_instances/number_per_class

	number_of_training_instances = number_per_class - test_instances

	X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size = test_instances, train_size = number_of_training_instances)

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
