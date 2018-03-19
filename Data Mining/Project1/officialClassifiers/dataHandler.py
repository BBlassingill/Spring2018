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

	return X

def splitData2TestTrain(originalData, number_per_class, test_instances) :

	num_of_classes = int(originalData.shape[0]/number_per_class)
	hashMap = dict.fromkeys(list(range(1, num_of_classes+1)), 0)

	X_train = []
	y_train = []
	X_test = []
	y_test = []

	for row in originalData:
		label = row[0]

		if hashMap[label] >= test_instances :
			X_train.append(row)

		else :
			hashMap[label] += 1
			X_test.append(row)


	X_train_original = np.array(X_train)
	X_test_original = np.array(X_test)

	x_train_transpose = X_train_original.transpose()
	x_test_transpose = X_test_original.transpose()

	X_train = x_train_transpose[1:].transpose()
	y_train = x_train_transpose[0:1].transpose()
	X_test = x_test_transpose[1:].transpose()
	y_test = x_test_transpose[0:1].transpose() 

	y_train = y_train.reshape(y_train.shape[0])
	y_test = y_test.reshape(y_test.shape[0])

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
