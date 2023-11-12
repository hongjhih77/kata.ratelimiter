# Kata: Rate Limiter

## Tasks

1. Create a Backend Service
   - Use Spring Boot WebFlux

2. Design RESTful API
   - Create just one API endpoint: GET `/api/resource`.

3. Rate Limiter
   - Implement a simple Rate Limiter to restrict access to /api/resource.
   - Limit each IP address to 10 requests per minute.
   - If the limit is exceeded, return an HTTP 429 status (Too Many Requests).
