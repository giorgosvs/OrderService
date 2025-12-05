## About The Project
This kata focuses on developing a component for managing orders on an e-commerce platform. Due to unforeseen circumstances, our team of Software Engineers is currently unavailable, leaving the implementation partially completed. This exercise aims to assess your technical skills, problem-solving abilities, and attention to detail in handling software development best practices.

#### Project Objectives
1. **Create new Orders**
   - **Endpoint:** `POST /order-service/create`
   - **Functionality:** This endpoint creates a new order for a specified customer. It will:
     - Accept a request containing order details.
     - Populate the order with data from our database.
     - Persist the new order in the database.
     - Return the created order to the client
2. **Modify an Order**
   - **Endpoint:** `POST /order-service/modify`
   - **Functionality:** This endpoint needs to be developed from scratch. Below are the technical requirements:
     - Retrieve a saved order by its ID.
     - Decorate the order with `processingProductId`.
     - Decorate the order with `orderType`.
     - Process the order.
     - Validate the order.
     - Persist the updated order in the database.
3. **Submit the Order**
   - **Endpoint:** `POST /order-service/submit`
   - **Functionality:** This endpoint submits the current Order. Depending on some conditions, this order could end in different states. The code for this feature was made by a trainee and wasn't reviewed by any experienced developer. You should take a deeper look and refactor it.

### Tasks to do
1. **Implement a new endpoint to modify the order.** This should allow us to add new products.
   * POST endpoint ( /order-service/modify )
     * **Input:** `ModifyOrderRequest`
     * **Output:** The modified `Order` object
     * **Specific behaviour:**
       * **Order Processing:**
         * If the `orderType` is `ADD`:
           * Retrieve the corresponding product from the database.
           * Decorate the Order adding the `Product` to the `addingProducts`.
           * Calculate the `finalPrice` based on the sum of all the products in the order. (No extra conditions)
       * **Order Validation:**
         * Set order status to `INVALID` if:
           * The product status is NOT_AVAILABLE.
           * The product release date is in the future.
           * The product expiry date is in the past.
         * Set the order status to `VALID` otherwise.
     * **Testing:** All the classes that involves this journey should be tested.
2. **Refactor `Submit` endpoint** and all the classes that it involves.
   * You should try to refactor the code for this endpoint, you can create new classes, or whatever you need to get the clearest code. Take a look on the current code to know which are the requirements. 
3. **(Optional) Implement the exception handling:** The actual one for our component is not good, if you have time, consider implementing a new way to handle them.

## How to Set up the project
This project was initially made with IntelliJ, it will be easier if you use it, but feel free to use any other IDE tht you feel comfortable with.

### How to build
1. Use `mvn clean install` to build and install all the required dependencies and run tests.
2. (Optional) Use `mvn clean install -PskipTests` to build and install all the required dependencies but skip the tests.


### How to run / debug
1. Use `docker-compose build up` to start the database.
2. Use `mvn springboot:run` to run your application.
3. (Optional) Use `mvn springboot:run -pDebug` to run your application in DEBUG mode.

### How to test
1. Use `mvn clean test` to launch the unit tests.

### FAQ
* Does the database have any username/password?
  * The username is `sa`, no password is required.
* Do I need to test all the classes?
  * Just add testing to the `Modify` journey involved classes, `Create` and `Submit` journeys must not be tested, feel free to add `Submit` journey tests to ensure your refactor doesn't brake anything, but it is not required for the kata.
* Can I add any external dependency?
  * No, you should not modify the pom.
* Can I modify any file?
  * Sure, feel free to reorganize, rewrite and add as many packages, classes or files you need to get your tasks done.
* Is KataTest.java a unit test class to be used as reference?
  * No, it is not. It is a behaviour test that checks that our main requirement for this feature has been achieved.