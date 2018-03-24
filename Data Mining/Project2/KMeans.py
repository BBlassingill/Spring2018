from sklearn import cluster, datasets
from sklearn.utils.linear_assignment_ import linear_assignment
from sklearn.datasets.samples_generator import make_blobs
from sklearn.decomposition import PCA
from sklearn.metrics import confusion_matrix, accuracy_score
import matplotlib.pyplot as plt
import numpy as np



# iris = datasets.load_iris()
# X_iris = iris.data
# y_iris = iris.target

# k_means = cluster.KMeans(n_clusters=3)
# k_means = k_means.fit(X_iris)

# print(k_means.labels_)
# print(y_iris[:10])

# accuracy = accuracy_score(y_iris, k_means.labels_)
# print(accuracy)

##################################################
# Working but not very good accuracy


iris = datasets.load_iris()
X_iris = iris.data
y_iris = iris.target



Model = PCA(n_components = 2)
X_new = Model.fit_transform(X_iris)

# print("\nnew X")
# print(X_new)

k_means = cluster.KMeans(n_clusters=3)
k_means = k_means.fit(X_new)

print(k_means.labels_)
# print(y_iris)
# print(y_iris[::10])

colors = ['r', 'g', 'b']
f = lambda x: colors[int(x)]

cluster_assignments = list(map(f, k_means.labels_))
# print("Predicted labels")
# print(k_means.predict(X_new))
# print(k_means.labels_[::10])
# print(cluster_assignments)

#plot
my_dpi = 96
plt.figure(figsize=(800/my_dpi, 800/my_dpi), dpi=my_dpi)

plt.xlabel('X')
plt.ylabel('Y')
plt.title('K-Means Clustering\n')


# print(iris.data[::10])
# print(iris.target[::10])


plt.scatter(X_new[:,0], X_new[:,1], color=cluster_assignments, s=20)
# plt.show()


#Computer Confusion Matrix
print("The y labels")
print(y_iris)

print("The predicted labels")
print(k_means.labels_)

matrix = confusion_matrix(y_iris, k_means.labels_)

print(matrix)

matrix = matrix.T
print(matrix)

ind = linear_assignment(-matrix)

print(ind)
C_opt = matrix[:,ind[:,1]]

print(C_opt)

acc_opt = np.trace(C_opt)/np.sum(C_opt)

print(acc_opt)
