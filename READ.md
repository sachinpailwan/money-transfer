# Money-transfer Task

This task will be used to mimic money transfer API between accounts

Technology Stack Used
1. Vertx in memory server
2. Google Guice

## Please note that application is configured to run on default 8091.

## General usage

Following API are exposed

1. create account - /accounts/create POST method and require following json input
   {
      "name":"Account Owner Name",
      "Type":"Type of Account"
   }
   Successful response will have following json
   {
       "id": 1,
       "name": "account owner name",
       "type": "account type",
       "amount": "account balancing amount default 0",
       "creationDate":"account creational date and time"
     }
2. deposit the amount - /accounts/:id/deposit/:amount GET method
   Example - /accounts/1/deposit/23.45

3. transfer -/transfer POST method and require following json input
   {
     "from":<transfer fund from account>
     "to" :<to account which will receive amount>
     "amount":<transfer amount>
   }
   Response - it will return list of json account objects
4. User detail /account/:id
   Response

All amounts are BigDecimals.

## Testing
Rest Assured dependecy is being used along with Vertx-unit
Integration Tests are written to consider all possible scenarios


## Running

$ java -jar target/money-transfer-1.0-SNAPSHOT-fat.jar
