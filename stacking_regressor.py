import numpy as np
from sklearn.base import BaseEstimator, RegressorMixin

# Custom StackingRegression
class CustomStackingRegressor(BaseEstimator, RegressorMixin):
    def __init__(self, base_models, final_model):
        self.base_models = base_models
        self.final_model = final_model

    def fit(self, X, y):
        self.base_models_predictions_ = []

        for model in self.base_models:
            model.fit(X, y)
            predictions = model.predict(X)
            self.base_models_predictions_.append(predictions)

        X_stacked = np.column_stack(self.base_models_predictions_)
        self.final_model.fit(X_stacked, y)

    def predict(self, X):
        base_models_predictions = []

        for model in self.base_models:
            predictions = model.predict(X)
            base_models_predictions.append(predictions)

        X_stacked = np.column_stack(base_models_predictions)
        return self.final_model.predict(X_stacked)