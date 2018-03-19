from sklearn.base import BaseEstimator, ClassifierMixin
from collections import Counter
import math
import operator
import numpy as np


class CustomCentroidClassifier(BaseEstimator, ClassifierMixin):
	def __init__(self):
		pass

	def _calculateAverages(self) :
		trainingAverages = []
		testAverages = []

		hashMap = dict.fromkeys(self.y_train, ())

		index = 0
		for label in self.y_train:
			hashMap[label] += (index,)
			index+=1

		for key in hashMap:
			list_of_indeces = hashMap[key]

			avgVector = self._getAverageForClass(list_of_indeces)
			trainingAverages.append((key, avgVector))		

		return trainingAverages
		

	def _getAverageForClass(self, list_of_indeces):
		vectors = []
		r,c = self.X_train.shape

		for index in list_of_indeces:
			vectors.append(self.X_train[index])

		index = 0
		resultingVector = []

		for x in range(c):
			sum = 0
			count = 0
			
			for vector in vectors:
				sum += vector[x]
				count+=1

			avg = sum/count
			resultingVector.append(avg)

		return resultingVector			
		

	def _euclideanDistance(self, testVector, trainingVector):
		distance = 0.0

		for i in range(len(testVector)):
			distance += pow((float(testVector[i]) - float(trainingVector[i])), 2)
		distance = math.sqrt(distance)

		return distance	


	def fit(self, X_train, y_train): 

		self.X_train = X_train;
		self.y_train = y_train;

		return self

	def predict(self, X_test): #return the label for each test vector
		
		trainingAverages = self._calculateAverages()

		#now that we have the average vector for each class, we need to compare each test vector to it to see which one it's closest to

		resultVector = []

		for testVector in X_test:
			distance = []

			for trainingVector in trainingAverages:
				eu_distance = self._euclideanDistance(testVector, trainingVector[1])
				distance.append((trainingVector[0], eu_distance))
				distance.sort(key = operator.itemgetter(1))

			resultVector.append(distance[0][0])

		return resultVector