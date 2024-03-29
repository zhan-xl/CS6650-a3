# CS6650-a3

## Database Design
### Database
In this assignment, I chose to use MongoDB. It is based on a NoSQL architecture, and the free tier has decent functionalities.
### Database Modeling
The only data model for this assignment is the lift ride. According to the API design document, it consists of:
* _id: ObjectID
* skierID: Integer
* dayID: String
* time: Integer
* liftID: Integer
* resortID: Integer
* seasonID: String
### Multithreading
The message queue consumer supports multithreading on both RabbitMQ and MongoDB connections. When the MessageConsumer class is initiated, it creates a connection pool to RabbitMQ and MongoDB. You can specify the size of the pool by modifying the environmental variable NUM_OF_CONSUMER. The Runnable consumer inside the MessageConsumer class listens to RabbitMQ and inserts the message to the database. I also added a monitoring thread that runs parallel to the listeners. The monitoring thread prints out the current throughput and memory usage.

After some experiments with different sizes of the connection pool, I found that the bottleneck of the post-load test is the insert performance to the database. According to MongoDB, "MongoDB allows multiple clients to read and write the same data. To ensure consistency, MongoDB uses locking and concurrency control to prevent clients from modifying the same data simultaneously. Writes to a single document occur either in full or not at all, and clients always see consistent data." As a result, the insertion performance would not increase for more than 2 concurrent threads. The throughput is around 50 writes per second, which becomes the limiting factor in the future.
## Server Design
### Query APIs
I have implemented 4 additional get methods under the SkierServlet based on the assignment requirement. I used query parameters to flag which question we would like to ask. For example, the following API:

http://34.219.76.180:8080/Server-1.0-XiaolinZhan/skiers?action=get-days&resortID=1&skierID=6258&seasonID=2024&dayID=1

would give the answer of how many days the skier has skied this season.
* "For skier N, how many days have they skied this season?" --> action=get-days
* "For skier N, what are the vertical totals for each ski day?" (calculate vertical as liftID*10) --> action=get-vertical
* "For skier N, show me the lifts they rode on each ski day" --> action=get-lifts
* "How many unique skiers visited resort X on day N?" --> action=get-skiers
### Major Updates
Besides adding the APIs above, I also added an RMQ channel pool to improve messaging performance.

In order to balance the queue length, I also added throttling control on the post API. The idea is under light load, assuming the queue size is zero or relatively small, the server would send messages to the queue as fast as possible. So in this state, the client would not notice the throttling control at all. After the queue grows to a certain size, I set this as a parameter called lower_bound, the server will start to reject some of the post requests. As the queue gets bigger, the rejection rate would increase with the queue size until the queue grows to an upper_bound in size, the server will reject all incoming post requests to preserve the RMQ server. When the server rejects the call, it will send a 500 HTTP status code along with a message indicating the server is busy. Once the queue is consumed by the consumer from the database side, the server will start to take requests gradually. The idea is to maintain the queue size relatively stable, despite the load from clients.

The lower_bound and upper_bound can be chosen to achieve different server behaviors. I set them to 1000 and 10,000 in the assignment.
## Client Design
### Circuit Breaker
In response to the server throttling control, I have added a circuit breaker in the client. When a post request is rejected due to a large queue size, the client would wait a certain time until making another attempt. The wait time would increase exponentially each time the same request is declined.
## Result
### Light Load
Under light load, as long as the queue size is less than the lower-bound, the server should not reject any post request from client. During this test, the client only produce on thread to make request.
![Alt text](https://github.com/zhan-xl/CS6650-a3/blob/e088b6ff3c35f2cf5f26008a2eb7631f98e4bec1/pics/client-1.png)
![Alt text](https://github.com/zhan-xl/CS6650-a3/blob/e088b6ff3c35f2cf5f26008a2eb7631f98e4bec1/pics/rmq-1.png)

The throughput is just around 60 per second and the message in queue is zero. The maximun response time is 149 ms which means the circuit breaker is not trigged during this test.
### Increased Load
Now let's try two threads in the client. 
