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
   git clone https://github.com/agrawalvivek2017/DevLens.AI.git
   cd DevLens.AI
   ```

2. Set up PostgreSQL:
   ```bash
   # Install PostgreSQL (if not installed)
   # Mac: brew install postgresql
   # Start PostgreSQL
   brew services start postgresql
   
   # Create the database
   psql postgres
   CREATE DATABASE devlens;
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE devlens TO postgres;
   ALTER DATABASE devlens OWNER TO postgres;
   \q
   ```

3. Configure your database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/devlens
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

4. Install and run Ollama with a mistral model:
   - Download the macOS app from the Ollama website:
      - Visit https://ollama.com/download
      - Install the .dmg file by dragging Ollama to your Applications folder
      - Launch Ollama from your Applications folder
   - Pull the mistral model (in Terminal):
     ```bash
     ollama pull mistral
     ```

5. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. Access the API documentation:
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

## Example Usage

Submit a code snippet:
```bash
curl -X POST http://localhost:8080/api/snippets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Java Class",
    "language": "java",
    "content": "public class Test {\n    private String password = \"secretpassword\";\n    \n    public void doSomething() {\n        // TODO: implement this method\n        System.out.println(\"Hello world\");\n    }\n    \n    public static void main(String[] args) {\n        Test test = new Test();\n        test.doSomething();\n    }\n}"
  }'
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.