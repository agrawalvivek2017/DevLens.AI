
# DevLens AI - Code Review & Optimization Tool

An AI-powered backend service that automatically reviews code for quality, security, and best practices, offering actionable improvements.

## Features

### Core Features
- **Code Quality Analysis** - Detects inefficient code, redundant logic, and suggests optimized solutions
- **Security Analysis** - Identifies vulnerabilities like SQL injection, XSS, hardcoded secrets, etc.
- **Best Practices Enforcement** - Ensures compliance with coding standards (Java Clean Code, PEP 8, etc.)
- **Automated Fix Suggestions** - AI generates possible fixes for detected issues
- **Multiple Language Support** - Initially for Java, expandable to Python, JavaScript, etc.
- **REST API** - Simple API for integration with other tools

## Tech Stack

- **Backend Framework**: Java with Spring Boot
- **AI Integration**: Ollama/Llama3
- **Database**: PostgreSQL
- **API Documentation**: OpenAPI (Swagger)
- **Containerization**: Docker

## Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (alternatively, you can use H2 for development)
- [Ollama](https://ollama.ai/) (for AI code analysis)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/devlens.git
   cd devlens
   ```

2. Configure your database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/devlens
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Install and run Ollama with a Llama3 model:
   ```bash
   # Follow Ollama installation instructions at https://ollama.ai/
   ollama pull llama3 # or your preferred model
   ```

4. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Access the API documentation:
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

## API Endpoints

### Code Snippets
- `POST /api/snippets` - Submit a code snippet for analysis
- `GET /api/snippets/{id}` - Get a specific code snippet
- `GET /api/snippets` - Get all code snippets
- `GET /api/snippets/language/{language}` - Get code snippets by language
- `DELETE /api/snippets/{id}` - Delete a code snippet

### Analysis
- `POST /api/analysis/snippet/{snippetId}` - Analyze a code snippet
- `GET /api/analysis/snippet/{snippetId}` - Get analysis results for a snippet
- `GET /api/analysis` - Get all analysis results
- `GET /api/analysis/language/{language}` - Get analysis results by language

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.