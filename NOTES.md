# Assumptions

1. I have used Java 23 and Maven. 
1. In real setting I would check with the team which language, language version and build tool is used. 
1. I settled for the latest stable Spring Boot. 
It should have little impact on anything else as it is a standalone microservice. 
Unless there's security issues for this version, artifact was missing or incompatibilities I would stick to it. 
1. The context path of the new API is not specified, so I would seek consensus as this needs to be consistent
across the platform. It also is visible to the consumer/client. 
For the sake of this exercise I'll leave it parametrised. 
It allows for flexibility as can be passed on the command-line without changing the source code. 
1. only-active parameter is defaulted to false but this behaviour would normally be verified with the product owner
1. address/region is to be omitted

# How to use
```shell
curl -s -X POST -d '{"companyNumber":"06500244"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
curl -s -X POST -d '{"companyName":"BBC LIMITED"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
```

# Tasks to consider and potentially do
1. Investigate failure when running
```shell
curl -s -X POST -d '{"companyName":"BBC LIMITED"}' -H "Content-Type: application/json" -H 'x-api-key: xxxx' "http://localhost:8080/api/v1/search"
```
1. ideally I would use test data that are anonymised 
(this set is publicly visible on companies house but I am still unsure about legal implications, right to reuse)
1. missing integration testing using controller and mocked proxy (there controller tests and there is service with mocked proxy)
1. some assertions are a bit too weak (e.g. checking number of officers instead of name) 
1. needs better error handling (users shouldn't need to know about proxy going through crisis) 
1. logging (I would review it and consider the seams when I/O takes places)
1. request retry strategies
1. I would add swagger to the new endpoint 
1. no observability metrics implemented
1. healthcheck endpoint not checked/enabled 
1. bonus

---
The bottom line is that I run out of time and I would not release the code in this shape yet. 
