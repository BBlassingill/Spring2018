from sklearn.base import BaseEstimator, ClassifierMixin
from collections import Counter
import math
import operator


class KNNClassifier(BaseEstimator, ClassifierMixin):
	def __init__(self, neighbors=0):
		self.neighbors = neighbors


	def _euclideanDistance(self, testVector, trainingVector):
		distance = 0.0

		for i in range(len(testVector)):
			distance += pow((float(testVector[i]) - float(trainingVector[i])), 2)
		distance = math.sqrt(distance)

		return distance

	def _findClassLabel(self, nearestNeighborDistances):
		nearestNeighbors = []
		for distance in nearestNeighborDistances:
			nearestNeighbors.append(distance[0]) #should append only the class label that's associated with that distance

		data = Counter(nearestNeighbors)
		mostCommonLabel = max(nearestNeighbors, key=data.get)

		return mostCommonLabel		


	def fit(self, X_train, y_train): 

		self.X_train = X_train;
		self.y_train = y_train;

		return self

	def predict(self, X_test): #return the label for each test vector
		
		currentIndex = 0
		number_of_class_labels = X_test.shape[0]
		
		knn = []
		for testVector in X_test:
			distance = []
			
			index = 0
			for trainVector in self.X_train:
				eu_distance = self._euclideanDistance(testVector, trainVector)
				distance.append((self.y_train[index], eu_distance))
				distance.sort(key = operator.itemgetter(1))
				index += 1

			nearestNeighborDistances = distance[:self.neighbors]
			
			classLabel = self._findClassLabel(nearestNeighborDistances)
			knn.append(classLabel)
			
		return knn