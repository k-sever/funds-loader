# Velocity Limiter

# Problem
In finance, it's common for accounts to have so-called "velocity limits".
Application should accept or decline attempts to load funds into customers' accounts in real-time.
Each attempt to load funds will come as a single-line JSON payload, structured as follows:
```json
{
    "id": "1234",
    "customer_id": "1234",
    "load_amount": "$123.45",
    "time": "2018-01-01T00:00:00Z"
}
```
Each customer is subject to three limits:
- A maximum of $5,000 can be loaded per day
- A maximum of $20,000 can be loaded per week
- A maximum of 3 loads can be performed per day, regardless of amount

As such, a user attempting to load $3,000 twice in one day would be declined on the second attempt, as would a user attempting to load $400 four times in a day.
For each load attempt, application should return a JSON response indicating whether the fund load was accepted based on the user's activity, with the structure:
```json
{ 
    "id": "1234", 
    "customer_id": "1234", 
    "accepted": true
}
```
You can assume that the input arrives in ascending chronological order and that if a load ID is observed more than once for a particular user, all but the first instance can be ignored (i.e. no response given). 
Each day is considered to end at midnight UTC, and weeks start on Monday (i.e. one second after 23:59:59 on Sunday).

## Build
```shell
docker build . --target build  
```
## Test
```shell
docker build . --target test  
```
## Run
```shell
java -jar target/funds-loader-0.0.1-SNAPSHOT.jar <input_file> <output_file>
```
or
```shell
docker build . --target app -t funds-loader:latest && \
docker run -v <data_folder>:/data funds_loader:latest /data/<input_file> /data/<output_file>
```
## Acceptance tests
```shell
./acceptance_test/run.sh 
```

## TODOs
- CI
- Move defaults to app config