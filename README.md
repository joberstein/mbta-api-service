# MBTA API Service
This Spring Boot application serves as a client for interacting with the MBTA API.

## Features
Callers of this service can:
- Initiate and listen to a prediction stream for a given route, direction, and stop
- Stop listening to a particular prediction stream
- Request a collection of MBTA resources (e.g. routes or stops). These endpoints utilize an in-memory map to 
store the 'Last-Modified' header so they can take advantage of MBTA API caching for all callers

## Deployment
Currently, this application is deployed with Heroku. This comes with some limitations (shuts down after 30 minutes of 
inactivity), but is a good place to start for standing the service up quickly and easily.

To deploy, (assuming you've already downloaded the heroku client), run:
- `herkou login`
- `mvn clean heroku:deploy`

Note: you'll need to specify the following environment variables in the Heroku configuration console in order to start 
up the service successfully:
- `DB_HOST`: hostname of the MongoDB instance (must be accessible with mongodb+srv)
- `DB_USERNAME`: username for the MongoDB instance
- `DB_PASSWORD`: password for the MongoDB instance
- `SERVICE_ACCOUNT_JSON`: The json from your Firebase service account
- `MBTA_API_KEY`: The api key from the MBTA Developer portal