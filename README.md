# Task
## API Description
This application provides three end-points for three required operations:
1. /api/exchange/{currency}/{date} for operation nr 1
- currency is currency code in ISO-4217 standard
- date is date of day we want to check exchange
2. /api/exchange/{currency}/max-min/?quotations={n} for operation nr 2
- currency is currency code in ISO-4217 standard
- n is number of quotation we want to analyze
3. /api/buy-and-sell/{currency}/difference/?quotations={n} for operation nr 3
- currency is currency code in ISO-4217 standard
- n is number of quotation we want to analyze  
### Moreover to all of this endpoints you can add request parameter detailed={true or false} (default is set to false) which decide whether to return full information or only shorten one  
for example:
- with detailed=false  
0.1042  
- with detailed=true  
{"difference":0.1042,"rate":{"no":"079/C/NBP/2023","effectiveDate":"2023-04-24","bid":5.1540,"ask":5.2582,"mid":null}}  
### Server is available via port 8080
## How to run
### In command line
Firstly make sure that you have installed maven v.3 and jdk17 on your computer.  
Then change directory to root folder of project (where pom.xml is)  
Finally run the following command: `mvn spring-boot:run`  
### Using Docker
Docker image is available at: https://hub.docker.com/r/rlipinskipl/recruitment-task  
Pull docker image
Then run command: `docker run -p8081:8080 rlipinskipl/recruitment-task:latest`  
You can specify any port not only 8081 but second part of this parameter has to be 8080 because server is working there
  
## How to use and test
Commands with expected responses will be presented below
1. `curl "http://localhost:8080/api/buy-and-sell/GBP/difference?quotations=2&detailed=true"`
- status: 200
- content: {"difference":0.1042,"rate":{"no":"079/C/NBP/2023","effectiveDate":"2023-04-24","bid":5.1540,"ask":5.2582,"mid":null}}
2. `curl "http://localhost:8080/api/buy-and-sell/USD/difference?quotations=210"`
- status: 200
- content: 0.1004
3. `curl "http://localhost:8080/api/exchange/USD/max-min?quotations=210&detailed=true"`
- status: 200
- content: {"maxRate":{"no":"188/A/NBP/2022","effectiveDate":"2022-09-28","bid":null,"ask":null,"mid":5.0381},"minRate":{"no":"080/A/NBP/2023","effectiveDate":"2023-04-25","bid":null,"ask":null,"mid":4.1649}}  
4. `curl "http://localhost:8080/api/exchange/CHF/max-min?quotations=15"`
- status: 200
- content: Max rate: 4.7570, Min rate: 4.6914
5. `curl "http://localhost:8080/api/exchange/CHF/2022-04-05?detailed=true"`
- status: 200
- content: {"table":"A","currency":"frank szwajcarski","code":"CHF","rates":[{"no":"066/A/NBP/2022","effectiveDate":"2022-04-05","bid":null,"ask":null,"mid":4.5590}]}
6. `curl "http://localhost:8080/api/exchange/CZK/2021-03-08"`
- status: 200
- content: 0.1738
7. `curl "http://localhost:8080/api/exchange/CZK/2008-04-05"`
- status: 404
- content: Data not found
8. `curl "http://localhost:8080/api/exchange/CZK/max-min?quotations=-12"`
- status: 400
- content: Quotation must be positive integer
9. `curl "http://localhost:8080/api/buy-and-sell/XXXX/difference?quotations=12"`
- status: 400
- content: Currency must be in ISO-4217 standard
10. `curl "http://localhost:8080/api/exchange/USD/08-09-2022"`
- status: 400
- content: Date must be in ISO-8601 standard
