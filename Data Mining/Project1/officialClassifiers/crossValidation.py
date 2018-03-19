from sklearn.cross_validation import cross_val_score
from sklearn import svm

import numpy as np
import dataHandler as dh
import CustomKNNClassifier as knn
import CustomCentroidClassifier as centroid
import LinearRegression as linear
#use data handler to select certain classes - will need to use the letter to number converter
#use data handler to split the data returned from step one into training and test set
#run the cross fold validation on each of the classifiers and save the accuracy results

def main():
	subsetData = dh.pickDataClass('trainDataXY.txt', [1, 2, 3, 4, 5])
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 9, 2)

	##################################### KNN

	# model = knn.CustomKNNClassifier(neighbors=2)
	# scores = cross_val_score(model, X_train, y_train, cv=2, scoring="accuracy")
	# print(scores)

	##################################### Centroid

	# model = centroid.CustomCentroidClassifier()
	# scores = cross_val_score(model, X_train, y_train, cv=2, scoring="accuracy")
	# print(scores)


	##################################### Linear Regression

	# model = linear.LinearRegression()
	# scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	# print(scores)

	##################################### SVM
	# model = svm.SVC(kernel='linear')
	# scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	# print(scores)


	#####Task A
	letter_to_digit_array = dh.letter_2_digit_convert("ABCDE")
	subsetData = dh.pickDataClass('HandWrittenLetters.txt', letter_to_digit_array)
	
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 9)
	print("Task A predicted labels:")
	
	model = knn.CustomKNNClassifier(neighbors=2)
	model = model.fit(X_train, y_train)
	predicted_labels = model.predict(X_test)
	print("\tKNN: " + str(predicted_labels))

	model = centroid.CustomCentroidClassifier()
	model = model.fit(X_train, y_train)
	predicted_labels = model.predict(X_test)
	print("\tCentroid: " + str(predicted_labels))

	model = linear.LinearRegression()
	model = model.fit(X_train, y_train)
	predicted_labels = model.predict(X_test)
	print("\tLinear Regression: " + str(predicted_labels))

	model = svm.SVC(kernel='linear')
	model = model.fit(X_train, y_train)
	predicted_labels = model.predict(X_test)
	print("\tSVM: " + str(predicted_labels))  
	
	##Task B

	##Task C

	##Task D
main()	