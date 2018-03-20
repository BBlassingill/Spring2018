from sklearn.cross_validation import cross_val_score
from sklearn import svm

import numpy as np
import dataHandler as dh
import CustomKNNClassifier as knn
import CustomCentroidClassifier as centroid
import LinearRegression as linear

def taskA():
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

def taskB():
	subsetData = dh.pickDataClass('ATNTFaceImages400.txt', list(range(1, 10)))	
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 10, 5)

	print("\nTask B reported accuracies:")

	model = knn.CustomKNNClassifier(neighbors=2)
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tKNN: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	model = centroid.CustomCentroidClassifier()
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	model = linear.LinearRegression()
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tLinear Regression: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	model = svm.SVC(kernel='linear')
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tSVM: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))


def taskCAndD():
	letter_to_digit_array = dh.letter_2_digit_convert("ABCDEFGHIJ")
	subsetData = dh.pickDataClass('HandWrittenLetters.txt', letter_to_digit_array)
	model = centroid.CustomCentroidClassifier()

	print("\nTask C Centroid Splits")

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 34)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)	
	print("\tCentroid Split 1: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 29)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 2: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 3: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 19)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 4: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 5: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 9)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 6: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 4)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 7: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	letter_to_digit_array = dh.letter_2_digit_convert("QWERTYUIOP")
	subsetData = dh.pickDataClass('HandWrittenLetters.txt', letter_to_digit_array)

	print("\nTask D Centroid Splits")

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 34)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 1: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 29)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 2: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 3: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 19)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 4: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 5: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 9)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 6: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 4)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 7: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))


def taskD():
	letter_to_digit_array = dh.letter_2_digit_convert("QWERTYUIOP")
	subsetData = dh.pickDataClass('HandWrittenLetters.txt', letter_to_digit_array)
	model = centroid.CustomCentroidClassifier()

	print("\nTask D Centroid Splits")

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 34)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 1: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 29)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 2: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 3: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 19)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 4: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 24)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 5: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 9)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 6: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))

	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 4)

	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	scores = np.round(scores, 2)
	print("\tCentroid Split 7: " + str(scores) + " Average: " + str((np.round(np.mean(scores), 2))))
	
def main():

	taskA()
	taskB()
	taskCAndD()

main()	