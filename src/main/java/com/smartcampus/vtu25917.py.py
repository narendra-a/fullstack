import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.feature_selection import SelectKBest, f_regression
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.svm import SVR
import matplotlib.pyplot as plt
import seaborn as sns

np.random.seed(42)
n_samples = 1000

temperature = np.random.uniform(0, 35, n_samples)
humidity = np.random.uniform(30, 90, n_samples)
time_of_day = np.random.uniform(0, 23, n_samples)
household_size = np.random.randint(1, 6, n_samples)
num_appliances = np.random.randint(3, 10, n_samples)
is_weekend = np.random.randint(0, 2, n_samples)

energy_consumption = (
    50 + temperature * 2 + humidity * 0.5 +
    time_of_day * 3 + household_size * 10 +
    num_appliances * 15 + is_weekend * 20 +
    np.random.normal(0, 20, n_samples)
)

energy_consumption[energy_consumption < 0] = 0

data = pd.DataFrame({
    'temperature': temperature,
    'humidity': humidity,
    'time_of_day': time_of_day,
    'household_size': household_size,
    'num_appliances': num_appliances,
    'is_weekend': is_weekend,
    'energy_consumption': energy_consumption
})

X = data.drop('energy_consumption', axis=1)
y = data['energy_consumption']

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)
X_scaled_df = pd.DataFrame(X_scaled, columns=X.columns)

X_train, X_test, y_train, y_test = train_test_split(
    X_scaled, y, test_size=0.2, random_state=42
)

wcss = []
for i in range(1, 11):
    kmeans = KMeans(n_clusters=i, random_state=42, n_init=10)
    kmeans.fit(X_scaled)
    wcss.append(kmeans.inertia_)

plt.plot(range(1, 11), wcss, marker='o')
plt.show()

kmeans = KMeans(n_clusters=4, random_state=42, n_init=10)
clusters = kmeans.fit_predict(X_scaled)

pca = PCA(n_components=2)
X_pca = pca.fit_transform(X_scaled)

plt.scatter(X_pca[:, 0], X_pca[:, 1], c=clusters)
plt.show()

pca_full = PCA()
pca_full.fit(X_scaled)

plt.plot(np.cumsum(pca_full.explained_variance_ratio_))
plt.show()

n_components = np.where(
    np.cumsum(pca_full.explained_variance_ratio_) >= 0.95
)[0][0] + 1

pca = PCA(n_components=n_components)
X_pca = pca.fit_transform(X_scaled)

selector = SelectKBest(score_func=f_regression, k=4)
X_filter = selector.fit_transform(X_scaled, y)

linear_model = LinearRegression()
linear_model.fit(X_train, y_train)
y_pred_lr = linear_model.predict(X_test)

print(mean_absolute_error(y_test, y_pred_lr))
print(mean_squared_error(y_test, y_pred_lr))
print(r2_score(y_test, y_pred_lr))

rf_model = RandomForestRegressor(n_estimators=100, random_state=42)
rf_model.fit(X_train, y_train)
y_pred_rf = rf_model.predict(X_test)

print(mean_absolute_error(y_test, y_pred_rf))
print(mean_squared_error(y_test, y_pred_rf))
print(r2_score(y_test, y_pred_rf))

svm_model = SVR()
svm_model.fit(X_train, y_train)
y_pred_svm = svm_model.predict(X_test)

print(mean_absolute_error(y_test, y_pred_svm))
print(mean_squared_error(y_test, y_pred_svm))
print(r2_score(y_test, y_pred_svm))

mlp = MLPRegressor(max_iter=1000, random_state=42)

param_grid = {
    'hidden_layer_sizes': [(50,), (100,), (50, 50)],
    'activation': ['relu', 'tanh'],
    'alpha': [0.0001, 0.001]
}

grid = GridSearchCV(mlp, param_grid, cv=3, scoring='r2', n_jobs=-1)
grid.fit(X_train, y_train)

best_model = grid.best_estimator_
y_pred_mlp = best_model.predict(X_test)

print(mean_absolute_error(y_test, y_pred_mlp))
print(mean_squared_error(y_test, y_pred_mlp))
print(r2_score(y_test, y_pred_mlp))