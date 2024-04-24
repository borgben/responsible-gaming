from time  import sleep
from datetime import datetime 
from json  import dumps
from kafka import KafkaProducer
from uuid import uuid1
import scipy.stats as ss
import numpy as np

shape, scale = 1.,500. 
s = int(np.random.gamma(shape, scale))

producer = KafkaProducer(
                bootstrap_servers=['localhost:9092'],
                value_serializer=lambda x: dumps(x).encode('utf-8')
           )

# Generator for normally distributed numbers 
x = np.arange(0, 200)
xU, xL = x + 0.5, x - 0.5 
prob = ss.norm.cdf(xU, scale = 100) - ss.norm.cdf(xL, scale = 100)
prob_norm = prob / prob.sum()

customer_guid_list = [ uuid1() for _ in range(200)]
while True:
    num_norm = np.random.choice(x, p = prob_norm)
    data = {'CustomerGuid' : str(customer_guid_list[num_norm]),'TotalMoneyAmount':round(np.random.gamma(shape, scale),2),"DateTime":datetime.now().strftime("%Y-%m-%dT%H:%M:%S")}
    producer.send('loss-events', value=data)
    sleep(2)