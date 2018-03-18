from sklearn.base import BaseEstimator, ClassifierMixin
import numpy as np

class LinearRegression(BaseEstimator, ClassifierMixin):
	def __init__(self):
		pass			

	def fit(self, Xtrain, Ytrain): 
		self.Xtrain = Xtrain
		self.Ytrain = Ytrain
	
		return self

	def predict(self, Xtest): 
		
		self.A_train = np.ones((self.Xtrain.shape[0], 1))
		self.A_test = np.ones((Xtest.shape[0], 1))

		Xtrain_padding = np.column_stack((self.Xtrain,self.A_train))
		Xtest_padding = np.column_stack((Xtest,self.A_test))

		B_padding = np.dot(np.linalg.pinv(Xtrain_padding), self.Ytrain)
		Ytest_padding = np.dot(B_padding, Xtest_padding.T)

		return np.round(Ytest_padding)
