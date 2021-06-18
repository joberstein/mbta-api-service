# MBTA API Service
This Spring Boot application serves as a client for interacting with the MBTA API.

Callers of this service can:
- Initiate and listen to a prediction stream for a given route, direction, and stop
- Stop listening to a particular prediction stream
- Request a collection of MBTA resources (e.g. routes or stops). These endpoints utilize an in-memory map to 
store the 'Last-Modified' header so they can take advantage of MBTA API caching for all callers
