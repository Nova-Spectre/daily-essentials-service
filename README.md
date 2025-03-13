# Daily-essentials-service

## Overview

This Spring Boot application provides a comprehensive backend for a grocery inventory management system with powerful search capabilities. Users can browse the complete inventory with rich filtering options and sorting functionality before placing orders. The service handles inventory management, product categorization, and provides a RESTful API for frontend integration.

## Tech Stack

- **Java 21** - Core programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Data access and ORM
- **Hibernate** - JPA implementation
- **H2** - In Memory Database
- **JUnit 5** - Testing framework
- **Mockito** - Mocking library for testing
- **Lombok** - Reduces boilerplate code
- **Maven/Gradle** - Build tools


## Features

- **Item Management**
  - Add items with category, brand, and price
  - Update item details
  - Automatic creation of brands and categories as needed
  - Price management and updates

- **Inventory Management**
  - Add inventory quantities for items
  - Update existing inventory levels
  - Track available quantities
  - Automatic status updates (AVAILABLE/OUT_OF_STOCK)
  - Inventory validation to prevent negative quantities

- **Search Functionality**
  - Filter by brand (multiple brands supported)
  - Filter by category (multiple categories supported)
  - Filter by price range (min/max)
  - Combine multiple filters for precise results
  - Pagination support with configurable page size
  - Empty results handling with descriptive error messages

- **Sort Options**
  - Sort by price (ascending/descending)
  - Sort by quantity (ascending/descending)
  - Default sorting by price when no sort criteria specified
  - Support for null value handling in sorting
  - Extensible sorting system for future criteria

- **Error Handling**
  - Comprehensive exception handling
  - Detailed error responses with codes and messages
  - Validation for all input parameters
  - Business logic validation


## API Endpoints

### Inventory Management
- `POST /v1/inventory` - Add new inventory or update existing
- `GET /v1/inventory` - Get all inventory items

### Search
- `GET /v1/search` - Search inventory with filters and sorting
## API Specifications


Find the detailed API specifications [here](https://docs.google.com/document/d/e/2PACX-1vQt1Ngi7zeiw72dSmtIl7G_YWCLnc4XyDJA3htl2ru65X931s44k_I_nQGRX2DsM5Y2xZqC04S2hgev/pub)

## Project Structure

The application follows a standard layered architecture:

- **Controller Layer** - Handles HTTP requests and responses
- **Service Layer** - Contains business logic
- **Repository Layer** - Interfaces with the database
- **Model Layer** - Defines entity classes
- **DTO Layer** - Data Transfer Objects for request/response
- **Exception Handling** - Global exception handlers and custom exceptions
- **Validation** - Input validation logic

## ER Diagram

![ER Diagram](https://www.mermaidchart.com/raw/7a39163c-30bc-4d65-af3b-d35062fdf03a?theme=light&version=v0.1&format=svg)


## Setup and Installation

1. Clone the repository
2. Configure database settings in `application.properties`
3. Run `./mvnw spring-boot:run` to start the application
4. Access the API at `http://localhost:8082`

## Future Enhancements

- Order processing functionality
- User authentication and authorization
- Admin dashboard for inventory management
- Integration with payment gateways


