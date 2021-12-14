# Individual Project Submission
Introduction to Functional Programming and Scala Language

Feygelman Andrey

---

I created a basic API with no authentication system 

I have 3 models: 
1. User(userName, hash, id)
2. Product (id, title, description, posterID)
3. Review(text, score, authorId, productId, id)

## How to start
1. open in Idea
2. add library: postgres driver https://jdbc.postgresql.org/download.html
3. add library: H2
4. Start postgres and put database config in resources/reference.conf
5. Start Server.scala
6. pray that it will work

## Endpoints

### Product endpoints
Method | Endpoint | description
--- | --- | ---
GET | product?pageSize={\_}&limit={\_} | list all products with pagination
GET | product/{id} | get a specific product
POST | product | create a new product
PUT | product/{ignored} | update a product (id is taken from request (Why??))
DELETE | product/{id} | deletes a product


### Review endpoints
Method | Endpoint | description
--- | --- | ---
POST | review | post a new review
GET | review?pageSize={\_}&limit={\_}&productId={prodId} | return review under specific product
GET | review/{id} | return a review with id=id
DELETE | review | deletes a review


### User Endpoints
Method | Endpoint | description
--- | --- | ---
GET | user?pageSize={\_}&limit={\_} | list all users with pagination
GET | user/{id} | get a specific user
POST | user | create a new user
PUT | user/{ignored} | update a user (id is taken from request (Why??))
DELETE | user/{id} | deletes a user

