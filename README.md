NLP System
This repository contains a Natural Language Processing (NLP) system implemented in Java using the Spring Framework. The system includes functionalities for processing text input, generating NLP statistics, and comparing results. The application utilizes the Stanford NLP library for text analysis.

Project Structure
The project is structured into several packages and classes:

com.casestudy.app.controller
NLPController: Manages the web interface, text processing, and NLP analysis. Handles input, processes text, and calculates various statistics. Provides endpoints for input, processing, and comparison.

MainController: Controls the main page of the application.

com.casestudy.app.security
SecurityConfig: Configures security settings for the application, defining access rules and authentication.

ServletInitializer: Configures the servlet initializer for deploying the application as a WAR file.

com.casestudy.app
NLPSystemApplication: The main class to run the Spring Boot application.
How to Run
Ensure you have Java and Maven installed on your machine.
Clone the repository: git clone <repository-url>
Navigate to the project directory: cd NLPSystem
Build the project: mvn clean install
Run the application: mvn spring-boot:run
The application will be accessible at http://localhost:8080.

Usage
Access the main page at http://localhost:8080.
Login using the encrypted password generated using spring security. It will provided in the output console. The user name is "User" by default.
Enter text or upload a text file on the input page.
Click "Generate NLP Statistics" to view the results.
Optionally, go to the "Comparison Page" to compare results with aggregated data.
Security
The application uses Spring Security for authentication and authorization. Access to certain pages is restricted, and users may need to log in.

Files and Templates
inputPage.html: Thymeleaf template for the input page.
resultPage.html: Thymeleaf template for displaying NLP statistics.
comparisonPage.html: Thymeleaf template for comparing results.
Additional Information
The system incorporates the Stanford NLP library for text processing and analysis.
Average values for word frequency and sentiment scores are calculated and stored in the session for comparison.
Security configurations are defined in SecurityConfig.java.
The application supports file upload for comparing results.
Feel free to explore and modify the code based on your requirements. If you encounter any issues, please refer to the documentation or open an issue in the repository.
