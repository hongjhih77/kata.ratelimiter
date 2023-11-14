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

## Reference
   - [1] [How to test a Webfilter](https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/test/java/org/springframework/web/cors/reactive/CorsWebFilterTests.java)
   - [2] [Google Guava: RateLimiter ](https://github.com/google/guava/blob/ed21dbb15ae0350fa9097b2959a71501a90d2dbe/guava/src/com/google/common/util/concurrent/RateLimiter.java)
   - [3] System Design Interview: An Insider’s Guide : CHAPTER 4: DESIGN A RATE LIMITER