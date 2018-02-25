import numpy as np
from collections import defaultdict
import csv

N_train = 45
N_test = 5
A_train = np.ones((1,N_train))    # N_train : number of training instance
A_test = np.ones((1,N_test))      # N_test  : number of test instance
# print(str(A_train))
#Xtrain is the training data?


trainingData = []
with open('ATNT50/trainDataXY.txt', 'r') as f:
	reader = csv.reader(f, delimiter=',')
	for row in reader:
		trainingData.append(row)

Xtrain = np.array(trainingData[1:len(trainingData)], dtype='int64')
# print(str(Xtrain))

testData = []
with open('ATNT50/testDataX.txt', 'r') as f:
	reader = csv.reader(f, delimiter=',')
	for row in reader:
		floatRow = []
		for i in row:
			floatRow.append(float(i))

		testData.append(floatRow)

Xtest = np.array(testData, dtype='int64')

#THIS RESULTED IN AN ARRAY WITH 40+ rows
# trainingData = defaultdict(list)
# with open('ATNT50/trainDataXY.txt', 'r') as f:
# 	reader = csv.reader(f, delimiter=',')
# 	for row in reader:
# 		for i in range(len(row)):
# 			trainingData[i].append(row[i])
# trainingData_columns = trainingData.values()

# Xtrain = []
# for col in trainingData_columns:
# 	Xtrain.append(col[1:len(col)])
# testArray = np.array(Xtrain)
# print(str(testArray))

Ytrain = [1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5] #class labels for the training data
Ytrain = np.array(Ytrain, dtype='int64')

Ytest = [1,4,5,2,3]
Ytest = np.array(Ytest, dtype='int64')

Xtrain_padding = np.row_stack((Xtrain,A_train))
Xtest_padding = np.row_stack((Xtest,A_test))
Xtrain_padding.dtype = 'int64'
Xtest_padding.dtype = 'int64'

# print(str(Xtrain_padding.shape))
# print(str(Ytrain.T))

B_padding = np.dot(np.linalg.pinv(Xtrain_padding.T), Ytrain.T)   # (XX')^{-1} X  * Y'  #Ytrain : indicator matrix
Ytest_padding = np.dot(B_padding.T,Xtest_padding)
Ytest_padding_argmax = np.argmax(Ytest_padding,axis=0)+1
err_test_padding = Ytest - Ytest_padding_argmax
TestingAccuracy_padding = (1-np.nonzero(err_test_padding)[0].size/len(err_test_padding))*100

print(str(TestingAccuracy_padding))