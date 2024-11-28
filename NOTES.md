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