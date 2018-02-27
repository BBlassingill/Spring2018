from sklearn import svm
import csv
import numpy as np

trainingData = []
with open('ATNT50/trainDataXY.txt', 'r') as f:
	reader = csv.reader(f, delimiter=',')
	for row in reader:
		trainingData.append(row)

testData = []
with open('ATNT50/testDataX.txt', 'r') as f:
	reader = csv.reader(f, delimiter=',')
	for row in reader:
		testData.append(row)

Xtrain = np.array(trainingData[1:], dtype='int64')
Ytrain = np.array(trainingData[0:1], dtype='int64')

Xtrain = Xtrain.transpose()
Ytrain = Ytrain.transpose()
Xtest = np.array(testData, dtype='int64')
Xtest = Xtest.transpose()

X = Xtrain
y = Ytrain
x_test = Xtest

print(x_test)


model = svm.SVC(kernel='linear', C=1, gamma=1) 
model.fit(X, y)
model.score(X, y)
predicted= model.predict(x_test)

print(predicted)