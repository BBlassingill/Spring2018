import numpy as np
from sklearn.cross_validation import cross_val_score
import dataHandler as dh
import CustomKNNClassifier as knn
import CustomCentroidClassifier as centroid
import LinearRegression as linear
#use data handler to select certain classes - will need to use the letter to number converter
#use data handler to split the data returned from step one into training and test set
#run the cross fold validation on each of the classifiers and save the accuracy results




def main():
	subsetData = dh.pickDataClass('trainDataXY.txt', [1, 2, 3, 4, 5])
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 9, 5)


	# model = knn.CustomKNNClassifier(neighbors=2)

	# scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	# print(scores)

	# model = centroid.CustomCentroidClassifier()
	# scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	# print(scores)


	#####################################

	model = linear.LinearRegression()
	scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
	print(scores)
	# c = y_train.shape
	# test = y_train.reshape(c,1)
	# print(y_train)
	# print(test)
	# Xtrain = X_train.transpose()
	# Ytrain = y_train
	# Xtest = X_test.transpose()
	# Ytest = y_test

	# print(X_train.shape)
	# print(y_train.shape)

	# print(Xtrain.shape)
	# print(y_train.shape)
	# print(y_train.transpose().shape)

	# model = linear.LinearRegression()
	# scores = cross_val_score(model, Xtrain, Ytrain, cv=5, scoring="accuracy")
	# print(score)


	# A_train = np.ones((X_train.shape[0], 1))
	# A_test = np.ones((X_test.shape[0], 1))

	# # print(A_train)
	# # print("\n")
	# # print(A_test)
	# # print("*******")

	# Xtrain_padding = np.column_stack((X_train,A_train))
	# Xtest_padding = np.column_stack((X_test,A_test))

	# # print(Xtrain_padding)
	# # print("\n")
	# # print(Xtest_padding)
	# # print("******")

	# # #computing the regression coefficients
	# B_padding = np.dot(np.linalg.pinv(Xtrain_padding), y_train)   # (XX')^{-1} X  * Y'  #Ytrain : indicator matrix
	# Ytest_padding = np.dot(B_padding, Xtest_padding.T)
	# Ytest_padding_argmax = np.argmax(Ytest_padding)
	# # print(B_padding)
	# print("printing Ytest_padding")
	# print(y_test)
	# print(np.round(Ytest_padding))
	# # print("about to print the max arguments")
	# # print(Ytest_padding_argmax)
	# err_test_padding = y_test - Ytest_padding_argmax
	# err_test_padding = err_test_padding.astype(np.int64)
	# print(err_test_padding)
	# print(np.nonzero(err_test_padding)[0].size)
	# print(1-np.nonzero(err_test_padding)[0].size)
	# print(1-np.nonzero(err_test_padding)[0].size/len(err_test_padding))
	# print(y_test)
	# # err_test_padding = y_test - Ytest_padding_argmax (ORIGINAL LINE OF CODE FROM NOTES)
	# err_test_padding = y_test - Ytest_padding[Ytest_padding_argmax-1]
	# print("error test padding")
	# print(err_test_padding)
	# # print(err_test_padding)

	# TestingAccuracy_padding = (1-np.nonzero(err_test_padding)[0].size/len(err_test_padding))*100
	# print("np.nonzero(err_test_padding[0]")
	# print(np.nonzero(err_test_padding)[0])
	# print("1 - np.nonzero(err_test_padding)[0]")
	# print(1-np.nonzero(err_test_padding)[0])
	# print("\n1 - np.nonzero(err_test_padding[0].size")
	# print(1-(np.nonzero(err_test_padding)[0].size))
	# print("\nlen of err_test_padding")
	# print(len(err_test_padding))
	# print("the division")
	# print(1-(np.nonzero(err_test_padding)[0].size)/len(err_test_padding))
	# print(TestingAccuracy_padding)
	# Ytest_padding = np.dot(B_padding.T,Xtest_padding)
	# Ytest_padding_argmax = np.argmax(Ytest_padding,axis=0)+1
	# err_test_padding = Ytest - Ytest_padding_argmax
	# TestingAccuracy_padding = (1-np.nonzero(err_test_padding)[0].size/len(err_test_padding))*100
	# print(TestingAccuracy_padding)



main()	