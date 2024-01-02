# Response bodies bug in OpenAPI Spring generator
## How to reproduce
## ❗ _To reproduce, checkout the `bug-showcase` branch_ 
It's as simple as:
- ```bash
   git clone https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo.git
  ```

- make sure you are on the `bug-showcase` branch
- ```bash
   mvn clean compile
  ```

- run
- go to [http://localhost:8080](http://localhost:8080)

  That's it! Made it simple for y'all 😉

## What the bug is

The bug is in how OpenAPI Spring generator generates **Swagger annotation** in [`api.nustache`](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/resources/JavaSpring/api.mustache)

Let's say we have a simple [Dog API](https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo/blob/bug-showcase/src/main/resources/openapi.yaml) defined in this OpenAPI spec file:

```yaml
openapi: 3.0.3
info:
  title: Simple test API
  description: Simple test API
  version: 1.0.0
  contact:
    name: Gleb Ivashyn
    url: https://github.com/GlobeDaBoarder?tab=repositories
    email: glebivashyn@gmail.com
servers:
  - url: 'https://localhost:8080'
paths:
  /dogs/ok-endpoints/:
    get:
      summary: Get all dogs
      operationId: getAllDogs
      tags:
        - dogs
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Dog'
  /dogs/ok-endpoints/{id}:
    delete:
      summary: Delete dog by id
      operationId: deleteDogById
      tags:
        - dogs
      parameters:
        - name: id
          in: path
          description: Dog id
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '204':
          description: No Content
        '404':
          description: Not Found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '400':
          description: Bad Request
  /dogs/broken-endpoint/:
    get:
      summary: Broken
      operationId: broken
      deprecated: true # Done purely for visibility purposes in Swagger UI
      tags:
        - dogs
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Dog'
        '204':
          description: No Content
        '400':
          description: Bad Request
components:
  schemas:
    ....
```

Notice how in our delete methods we have status codes `204` and `400` and how they only have a description _**without a response body**_:
``` yaml
... 
responses:
  '204':
    description: No Content
  '404':
    description: Not Found
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/Error'
  '400':
    description: Bad Request
...
```

and 

``` yaml
...
responses:
  '200':
    description: OK
    content:
      application/json:
        schema:
          type: array
          items:
            $ref: '#/components/schemas/Dog'
  '204':
    description: No Content
  '400':
    description: Bad Request
...
```
Logically, we would expect it to be this way in our Swagger UI as well, but surprisingly it isn't that simple. In one case it is like that, but in another one responses `204` and `400` have 
a response body that we did not specify.

### Case 1: proper response codes with no response bodies:

If we launch the application and go to localhost to view Swagger UI, we will be met with three endpoints.
Let's take a look at the `/dogs/ok-endpoints/` **DELETE** endpoint:
![image](https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo/assets/74022878/534d31a7-7608-4d70-9dd3-533be810f31a)


Here, everything looks just like we expected. `204` and `400` have no response body. Good! There is however a different case.

### Case 2: `204` and `400` have a response body for some reason... 

Now, if we take a look at a different endpoint, which is under `
  'http://localhost:8090` **GET** and is deprecated (for visibility purposes). The picture is completely different here:

  ![image](https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo/assets/74022878/16903a94-e3ba-4f3f-b3fd-7a48e4a50625)

Although specified without response bodies, `204` and `400` now have a response body, coming from the previous response. 

## Why this is happening

It is hard for me to pinpoint exactly what the reason for this strange behavior is. If we take a look at the generated Swagger UI annotation on controllers, those look fairly normal:
![image](https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo/assets/74022878/372c5188-f046-4307-a7a6-135db6306f81)
![image](https://github.com/GlobeDaBoarder/openapi-generator-wrong-response-body-bug-demo/assets/74022878/04e47616-9d01-40d3-a080-c7decb5ef8d1)


In both cases, Swagger annotation does not have any data or schema references that were not defined in OpenAPI spec.

This leads me to believe that the underlying issue is how **SpringDoc interprets those annotations**. 
It seems like because the status code `200` comes earlier than codes `204` and `404`, SpringDoc for some reason applies this schema even to responses with no schema defined. 

Even though this technically seems like more of a SpringDoc issue, ***It is extremely easy to fix by just slightly adjusting one line in the `api.mustache`  template file of the OpenAPI Spring generator.***
It's a simple approach and a sa result SpringDoc doesn't get confused : ) 

# Proposed solution 

## ❗ _To view the proposed bugfix solution, checkout the `bugfix-proposal` branch_
