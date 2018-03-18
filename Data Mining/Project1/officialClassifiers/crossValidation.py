import numpy as np
from sklearn.cross_validation import cross_val_score
import dataHandler as dh
import CustomKNNClassifier as knn
import CustomCentroidClassifier as centroid
#use data handler to select certain classes - will need to use the letter to number converter
#use data handler to split the data returned from step one into training and test set
#run the cross fold validation on each of the classifiers and save the accuracy results




def main():
	subsetData = dh.pickDataClass('trainDataXY.txt', [1, 2, 3, 4, 5])
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 9, 5)

	# model = knn.CustomKNNClassifier(neighbors=2)

	# scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	# print(scores)

	model = centroid.CustomCentroidClassifier()
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	print(scores)

main()	