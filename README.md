# AndroidDigitRecognition
Android application for digit recognition using custom neural network
================
This application uses a fully connected neural network with one hidden layer described in the [first chapter](http://neuralnetworksanddeeplearning.com/chap1.html)
of Michael Nielsen's [free online book "Neural Networks and Deep Learning"](http://neuralnetworksanddeeplearning.com/). Together with input and
output layers the network consitutes three layers. First layer with 784 neurons, second layer with 30 neurons, and the third layer with 10 neurons.

The application is fully functional, and does not depend on external services. I have trained my own neural network and put it's weights and biases into the assets folder. The pre-trained networks recognition rate is 95 percent.
If you like you can train your own network and use it after replacing the weights and biases in the assets folder. The recognition rate decreases somewhat significantly down to 50 percent when working with digits coming from phone camera. This happens 
because the digit image is not perfectly centered, illuminated by light etc.

## Application demo
[![Video](http://img.youtube.com/vi/TqNrVK2btLA/0.jpg)](https://www.youtube.com/watch?v=TqNrVK2btLA)
