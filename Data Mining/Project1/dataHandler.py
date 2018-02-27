from sklearn.model_selection import train_test_split
import csv
import numpy as np
import scipy.io as io
import string

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

def	storeTrainXTrainY(X_train, y_train, X_test, y_test, filetype) :
	# file = open("test.txt", "w")
	# file.writelines(str(X_train.toList()))
	# file.close

	# test = np.concatenate((y_train[:, np.newaxis], X_train), axis=1)
	train_data = np.column_stack((X_train, y_train)).transpose()
	train_data = np.flip(train_data, 0)
	
	test_data = np.column_stack((X_test, y_test)).transpose()
	test_data = np.flip(test_data, 0)

	# arr = np.arange(9)
	# arr = arr.reshape((3, 3))
	# print(arr)
	# print()
	# print()

	# print(train_data)

	if(filetype == "matlab"):
		# file = open("trainData.txt", "w")
		# file2 = open("testData.txt", "w")

		mat = np.asmatrix(train_data)
		# print(mat.tolist())

		io.savemat("trainData.mat", mdict={"trainData": mat.tolist()})
		io.savemat("testData.mat", mdict={"testData": test_data.tolist()})

		# print(train_data.type())
		mat = io.loadmat("trainData.mat")
		print(mat.items())
		# for test in mat.values():
		# 	print(test)
		# print(mat.values())
		# print(mat.type())
		# for row in mat:
		# 	print(row)
		# 	for col in mat[row]:
		# 		print(mat[row][col])
		# print(str(mat.tolist()))
	if(filetype == "python"):
		file = open("trainData.py", "w")
		file2 = open("testData.py", "w")
		file.writelines(str(train_data.tolist()))
		file.close
		file2.writelines(str(test_data.tolist()))
		file2.close 
	if(filetype == "text"):
		file = open("trainData.txt", "w")
		file2 = open("testData.txt", "w")
		file.writelines(str(train_data.tolist()))
		file.close
		file2.writelines(str(test_data.tolist()))
		file2.close

	# file.writelines(str(train_data.tolist()))
	# file.close
	# file2.writelines(str(test_data.tolist()))
	# file2.close

def letter_2_digit_convert(charString) :
	resultArray = []

	for char in charString:
		char = char.lower()
		resultArray.append(string.ascii_lowercase.index(char) + 1)

	return resultArray
		
def main() :
	# subsetData = pickDataClass('ATNT50/trainDataXY.txt', [1, 3])
	X_train, y_train, X_test, y_test = splitData2TestTrain('ATNT50/trainDataXY.txt', 9, 5)
	# storeTrainXTrainY(X_train, y_train, X_test, y_test, "matlab")
	letter_2_digit_convert("ACFG")

main()	