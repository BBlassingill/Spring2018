from sklearn import cluster
from sklearn.utils.linear_assignment_ import linear_assignment
from sklearn.decomposition import PCA
from sklearn.metrics import confusion_matrix, accuracy_score
import matplotlib.pyplot as plt
from matplotlib import colors as mcolors
import numpy as np
import dataHandler as dh


def task1():
	subsetData = dh.pickDataClass('ATNTFaceImages400.txt', range(1, 11))
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 10, 0)

	k_means = cluster.KMeans(n_clusters=10)

	accuracies = []

	for x in range(0, 5):
		k_means = k_means.fit(X_train)
		accuracies.append(obtainConfusionMatrixAndAccuracy(y_train, k_means.labels_))

	print("The average accurary for Task 1 is: " + str(np.round(np.mean(accuracies)*100, 2)) + "%")

	X_new = reduceTo2Dimensions(X_train)
	k_means = k_means.fit(X_new)
	plot(X_new, k_means, "Task 1")

def task2():
	subsetData = dh.pickDataClass('ATNTFaceImages400.txt', range(1, 41))
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 10, 0)

	k_means = cluster.KMeans(n_clusters=40)

	accuracies = []

	for x in range(0, 5):
		k_means = k_means.fit(X_train)
		accuracies.append(obtainConfusionMatrixAndAccuracy(y_train, k_means.labels_))

	print("The average accurary for Task 2 is: " + str(np.round(np.mean(accuracies)*100, 2)) + "%")


	X_new = reduceTo2Dimensions(X_train)
	k_means = k_means.fit(X_new)
	plot(X_new, k_means, "Task 2")	

def task3():
	subsetData = dh.pickDataClass('HandWrittenLetters.txt', range(1, 27))
	X_train, y_train, X_test, y_test = dh.splitData2TestTrain(subsetData, 39, 0)

	k_means = cluster.KMeans(n_clusters=26)

	accuracies = []

	for x in range(0, 5):
		k_means = k_means.fit(X_train)
		accuracies.append(obtainConfusionMatrixAndAccuracy(y_train, k_means.labels_))

	print("The average accurary for Task 3 is: " + str(np.round(np.mean(accuracies)*100, 2)) + "%")

	X_new = reduceTo2Dimensions(X_train)
	k_means = k_means.fit(X_new)
	plot(X_new, k_means, "Task 3")	

def obtainConfusionMatrixAndAccuracy(y_train, labels):
	matrix = confusion_matrix(y_train, labels)
	matrix = matrix.T

	ind = linear_assignment(-matrix)
	C_opt = matrix[:,ind[:,1]]
	acc_opt = np.trace(C_opt)/np.sum(C_opt)

	return acc_opt


def reduceTo2Dimensions(data):
	Model = PCA(n_components = 2)
	return Model.fit_transform(data)

def plot(data, k_means, task):
	colors = ['red', 'gold', 'skyblue', 'plum', 'lime', 'aqua','aquamarine', 'black', 'burlywood', 'brown', 'coral', 'darkgreen', 'lavender', 'crimson', 'hotpink', 'darkgreen', 'darkturquoise', 
	'fuchsia', 'darkviolet', 'lightslategray', 'lightsteelblue', 'moccasin', 'mediumspringgreen','navy', 'mistyrose', 'olive', 'orange', 'yellowgreen', 'peru', 'tomato', 'violet', 'thistle', 'steelblue', 
	'sienna', 'seagreen', 'silver', 'salmon', 'rosybrown', 'mediumvioletred', 'mediumorchid']
	
	my_dpi = 96
	plt.figure(figsize=(800/my_dpi, 800/my_dpi), dpi=my_dpi)

	plt.xlabel('X')
	plt.ylabel('Y')
	plt.title('K-Means Clustering for '+ task + '\n')

	f = lambda x: colors[int(x)]
	cluster_assignments = list(map(f, k_means.labels_))

	plt.scatter(data[:,0], data[:,1], color=cluster_assignments, s=20)
	centers = k_means.cluster_centers_
	plt.scatter(centers[:, 0], centers[:, 1], c='black', s=150, alpha=0.5);
	
	plt.savefig(task + " Plot")

def main():
	task1()
	task2()
	task3()
main()	
