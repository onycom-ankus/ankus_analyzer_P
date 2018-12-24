#!/usr/bin/env python
# coding: utf-8

# In[16]:


import scipy
import numpy
import matplotlib
import pandas
import sklearn
import pydot
import h5py
import theano
import keras


# In[17]:


from keras.utils import np_utils
from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Activation


# In[18]:


(X_train_, Y_train), (X_test_, Y_test) = mnist.load_data()
X_train = X_train_.reshape(60000, 784).astype('float32')/255.0
X_test = X_test_.reshape(10000, 784).astype('float32')/255.0
Y_train = np_utils.to_categorical(Y_train)
Y_test = np_utils.to_categorical(Y_test)


# In[19]:


model = Sequential()
model.add(Dense(units=64, input_dim=28*28, activation='relu'))
model.add(Dense(units=10, activation='softmax'))
model.compile(loss='categorical_crossentropy',optimizer='sgd', metrics=['accuracy'])
model.fit(X_train, Y_train, epochs=5, batch_size=32)


# In[7]:


loss_and_metrics = model.evaluate(X_test, Y_test, batch_size=32)


# In[8]:


print('loss_and_metrics:' + str(loss_and_metrics))


# In[9]:


from IPython.display import SVG
from keras.utils.vis_utils import model_to_dot


# In[11]:


get_ipython().magic(u'matplotlib inline')


# In[ ]:




