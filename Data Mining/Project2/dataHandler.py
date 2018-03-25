from sklearn.model_selection import train_test_split
from collections import OrderedDict
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

	for num in class_ids:
		for row in X:
			if row[0] == num:
				dataSubset.append(row)

	X = np.array(dataSubset, dtype='int64')

	return X

def splitData2TestTrain(originalData, number_per_class, test_instances) :

	hashMap = OrderedDict.fromkeys(originalData.T[0:1].flatten(), 0)

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
	
	if(filetype == "matlab"):
		io.savemat("trainData"+ testName+ ".mat", mdict={"trainData": train_data.astype(int).tolist()})
		io.savemat("testData"+ testName+ ".mat" , mdict={"testData": test_data.astype(int).tolist()})

	else :
		file = open("xtrain.txt", "w")
		file2 = open("ytrain.txt", "w")
		file3 = open("xtest.txt", "w")
		file4 = open("ytest.txt", "w")
		file.writelines(str(X_train.tolist()))
		file2.writelines(str(y_train.tolist()))
		file3.writelines(str(X_test.tolist()))
		file4.writelines(str(y_test.tolist()))

		file.close
		file2.close
		file3.close
		file4.close

def letter_2_digit_convert(charString) :
	resultArray = []

	for char in charString:
		char = char.lower()
		resultArray.append(string.ascii_lowercase.index(char) + 1)

	return resultArray
