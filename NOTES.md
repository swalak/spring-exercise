# How to use
```shell
curl -s -X POST -d '{"companyNumber":"06500244"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
curl -s -X POST -d '{"companyName":"BBC LIMITED"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
```

# Tasks to consider and potentially do
1. bonus - I haven't touched that as it feels this needs a JSON document store which I am about to experiment with

# Assumptions
1. I have used Java 23, Maven and the latest stable Spring Boot.
   In a real setting I would check with the team/lead/guidelines.
   Since it is a standalone microservice it has less (!) of an impact, but there is an impact so needs coordinating.
2. The level I did testing on is quite granular. That's what helped me implement the solution.
   If the new components were constantly changing and the test coupling was causing a drag I would consider
   redefining what the unit size is for the tests. Growing the unit size could help focus on testing higher level
   behaviors (without becoming end to end) and reduce tests' fragility.
3. New API searches for a company by name or number just by passing that value as query into company proxy search API.
   I would seek clarification with business owner as the search could be expected by them to be an exact match or
   include comparison of selected fields only, etc.
4. When company proxy API is passed a number in query the results are not checked by me for the company number
   match. That means a situation is possible that the company proxy API matches number to a field other than 
   company number and returns that company as if the result was for the right company number. That can have serious 
   repercussions if the "now incorrect" data set is used to retrieve some other and more sensitive data. 
5. The context path of the new API is not specified, so I would seek consensus as this needs to be consistent
   across the platform. It also is visible to the consumer/client.
   For the sake of this exercise I'll leave it parametrised.
   It allows for flexibility as can be passed on the command-line without changing the source code.
6. "only-active" parameter is defaulted to false but this behaviour would normally be verified with the product owner
7. I am aware the address model will have more fields that needs adding. I would seek documentation or explore the
   data coming back from proxy to assess what subfields are needed. "region" is already omitted but in a real scenario
   it would be added as well as many more (address lines 2-6, etc.)
8. ideally I would use proxy test data that are anonymised but for it to be useful a decent chunk of time would be
   invested. This set is publicly visible on companies house, but I am still unsure about legal implications.
   The assumption is that the proxy endpoint I used is a test endpoint with test data. 
   under src/test/resources/__files/life-like is a dump of vanilla proxy responses  
   under src/test/resources/__files/minimal are minimal, anonymous, easy to read and manually crafted proxy responses 
   that focus on illustrating specific concerns
9. in production the message logging and dumping is turned off by default for no-error scenarios for efficiency.
10. when GlobalExceptionHandler.handleOtherException is invoked it will log the message.
    It will not return the message to the user, but it may log sensitive info, which may be a problem in this case.
    I am aware of this.
11. http request retryability ignores http header "retry-after" - too much detail to implement for an assessment
12. I can spend weeks on this exercise but gotta say "ready" at some point. These are the next steps I would look into:
    1. swagger
    2. healthcheck
    3. observability metrics
