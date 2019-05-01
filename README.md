# Money-transfer Task

This task will be used to mimic money transfer API between accounts

Technology Stack Used
1. Vertx in memory server
2. Google Guice

## Please note that application is configured to run on default 8091.

## General usage

Following API are exposed

1. create account - http://localhost:8091/accounts/create POST method and require following json input
   {
      "name":"Account Owner Name",
      "Type":"Type of Account"
   }
   Successful response will have following json
   {
       "id": <Account Id>,
       "name": < Account Owner>,
       "type": <Account Type>,
       "amount": <Account Balancing amount default 0>,
       "creationDate":<Account Creational date and time>
     }

2. Deposit the amount - http://localhost:8091/accounts/:id/deposit/:amount PATCH method
   Example - /accounts/1/deposit/23.45
           - /accounts/1/deposit/100

3. Transfer - http://localhost:8091/transfer POST method and require following json input
   {
     "from":<transfer fund from account>
     "to" :<to account which will receive amount>
     "amount":<transfer amount>
   }
   Response - it will return list of json account objects

4. User detail http://localhost:8091/account/:id
   Response User AccountDTO Json
   {
       "id": < Account ID >,
       "amount": <Balance Amount>,
       "name": < Account Owner>,
       "type": < Account Type>
   }


All amounts are BigDecimals.

## Testing
Rest Assured dependecy is being used along with Vertx-unit
Integration Tests are written to consider all possible scenarios


## Running

$ java -jar target/money-transfer-1.0-SNAPSHOT-fat.jar
