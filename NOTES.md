# Assumptions

1. I have used Java 23 and Maven. 
2. In real setting I would check with the team which language, language version and build tool is used. 
3. I settled for the latest stable Spring Boot. 
It should have little impact on anything else as it is a standalone microservice. 
Unless there's security issues for this version, artifact was missing or incompatibilities I would stick to it. 
4. search by company name/number just passes that value as query into company proxy search API
I would seek clarification with business owner as the search could be expected to be 
an exact match, or include selected fields only, etc. 
5. When company proxy API is passed a number in query it checks that the returned company number matches.
That means a situation when the company proxy API matches number to a field other than company number
and returns that company as if the company number matched. 
6. The context path of the new API is not specified, so I would seek consensus as this needs to be consistent
across the platform. It also is visible to the consumer/client. 
For the sake of this exercise I'll leave it parametrised. 
It allows for flexibility as can be passed on the command-line without changing the source code. 
7. only-active parameter is defaulted to false but this behaviour would normally be verified with the product owner
8. address/region is to be omitted
9. ideally I would use proxy test data that are anonymised but for it to be useful a decent chunk of time would be invested
(this set is publicly visible on companies house but I am still unsure about legal implications, right to reuse)
under src/test/resources/__files/life-like is a dump of vanilla proxy responses (proxy treated as a test system)  
under src/test/resources/__files/minimal are minimal, anonymous and manually crafted proxy responses that focus on illustrating specific concerns
10. in production the message logging and dumping is turned off for no-error scenarios for efficiency. 
11. when GlobalExceptionHandler.handleOtherException is invoked it will log the message.
It will not return the message to the user, but it may log sensitive info, which may be a problem in this case. 
I am aware of this. 
12. 
# How to use
```shell
curl -s -X POST -d '{"companyNumber":"06500244"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
curl -s -X POST -d '{"companyName":"BBC LIMITED"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
```

# Tasks to consider and potentially do
1. needs better error handling (users shouldn't need to know about proxy going through crisis) 
2. request retry strategies
3. I would add swagger to the new endpoint 
4. no observability metrics implemented
5. healthcheck endpoint not checked/enabled 
6. bonus

---
The bottom line is that I run out of time and I would not release the code in this shape yet. 
