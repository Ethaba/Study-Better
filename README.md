# Study Better

## 1. Project Overview

Study Better is an Android application developed to help students organise their academic activities in one place. The application provides features for managing modules, assignments, study sessions, progress and achievements, while also providing user registration, login and profile settings.

The project was developed as an academic Android application using Kotlin and XML in Android Studio. A RESTful API was implemented using ASP.NET Core to handle user registration and authentication, with user information stored in an Azure SQL database.

The project focuses on providing a simple and organised student experience while demonstrating Android development, API integration, database connectivity, authentication, testing and continuous integration.

---

## 2. Project Purpose

The purpose of Study Better is to provide students with a central application for organising their academic responsibilities.

The application allows students to:

* Create an account and log in securely.
* View a personalised dashboard.
* Manage their academic modules.
* Add and manage assignments.
* View upcoming assignment deadlines.
* Set and track assignment progress.
* Use a study session timer.
* View academic progress.
* View achievements.
* Manage profile and application settings.
* Change selected settings such as language, study goal and notifications.

---

## 3. Main Features

### User Registration and Login

Users can register using their full name, email address and password.

The application communicates with the REST API when registering and logging in. Passwords are not stored as plain text. The API uses password hashing before storing the password information in the database.

### Dashboard

The dashboard provides an overview of the student's academic information.

It includes:

* Personalised user greeting.
* Upcoming assignments.
* Assignment progress.
* Latest achievements.
* Access to other areas of the application.

### Modules

Students can manage the modules associated with their studies.

### Assignments

Students can create and manage assignments by providing information such as:

* Assignment title.
* Description.
* Module.
* Due date.
* Priority.
* Completion progress.

Assignments can be displayed according to their due dates and progress.

### Calendar

The calendar provides a way of viewing assignment-related dates and managing assignment information.

### Study Session

The study session feature allows students to track their study time using a timer.

### Progress

The progress section provides an overview of assignment completion and academic activity.

### Achievements

The achievements section provides achievement information based on the student's activity within the application.

### Profile and Settings

Users can manage their profile and selected application settings.

---

## 4. Technologies Used

| Technology            | Purpose                                        |
| --------------------- | ---------------------------------------------- |
| Kotlin                | Android application programming language       |
| XML                   | Android user interface layouts                 |
| Android Studio        | Android development environment                |
| ASP.NET Core          | RESTful API                                    |
| C#                    | API development                                |
| Entity Framework Core | Database access and data management            |
| Azure SQL Database    | Online database                                |
| Retrofit              | Communication between Android and the REST API |
| Gson Converter        | Conversion of JSON API responses               |
| JWT                   | Authentication tokens                          |
| PasswordHasher        | Password hashing                               |
| JUnit                 | Unit testing                                   |
| GitHub                | Source code management                         |
| GitHub Actions        | Automated testing and build process            |

Kotlin is officially supported for Android development and is used extensively within the Android development ecosystem.

---

## 5. System Architecture

Study Better uses a client-server architecture.

The Android application communicates with the ASP.NET Core REST API using Retrofit. The API processes requests and communicates with Azure SQL through Entity Framework Core.

Entity Framework Core provides support for SQL Server and Azure SQL through the SQL Server database provider.

---

## 6. REST API

The Android application communicates with the backend through REST API endpoints.

The authentication endpoints include:

```text
POST /api/Auth/register
POST /api/Auth/login
```

### Registration

The Android application sends registration information to the API.

```json
{
  "fullName": "Student Name",
  "email": "student@example.com",
  "password": "password"
}
```

The API validates the information, hashes the password and stores the user information in Azure SQL.

### Login

The Android application sends the user's email and password to the API.

The API verifies the credentials and returns an authentication response containing user information and a JWT token.

The Android application then stores the required session information locally so that the logged-in user can be identified throughout the application.

---

## 7. Database

The application uses Microsoft Azure SQL Database as its online database.

The user table stores information such as:

* User ID
* Full name
* Email address
* Password hash
* Language preference
* Study goal
* Theme
* Notification preference
* Account creation date

The database was created and managed through Azure, while Entity Framework Core was used by the ASP.NET Core API to communicate with the database.

Microsoft provides documentation for connecting .NET applications to Azure SQL Database using Entity Framework Core.

---

## 8. Authentication and Security

Study Better uses authentication through the ASP.NET Core API.

The application does not store the user's password as plain text. Passwords are hashed before being stored in the database.

After a successful login, the API returns a JWT authentication token. The Android application stores the token and relevant user information locally for the current session.

Input validation is also used for fields such as email addresses and passwords to help prevent invalid information from being submitted.

---

## 9. Local Application Data

Some application features use local Android storage to maintain information needed by the application.

User-specific assignment and module information is associated with the logged-in user's ID. This helps prevent information belonging to one user from being displayed for another user on the same application.

---

## 10. Testing

Unit tests were implemented to test important application logic.

The current unit tests include tests for:

### Assignment Priority

The assignment priority logic is tested using different numbers of days until an assignment is due.

Examples include:

* 5 days → High priority
* 6 days → Medium priority
* 10 days → Medium priority
* 14 days → Medium priority
* 15 days → Low priority

### Input Validation

Input validation tests check:

* Valid email addresses.
* Invalid email addresses.
* Empty email addresses.
* Valid passwords.
* Passwords shorter than six characters.
* Empty passwords.

The tests were executed successfully in Android Studio.

Android Studio provides tools for creating and running Android tests, while command-line testing can also be used as part of continuous integration systems.

---

## 11. GitHub Actions

GitHub Actions was implemented to automatically build and test the project.

The workflow is located at:

```text
.github/workflows/android.yml
```

The workflow performs the following steps:

1. Checks out the project from GitHub.
2. Sets up JDK 11.
3. Sets up Gradle.
4. Runs the unit tests.
5. Builds the debug APK.

The workflow is triggered when changes are pushed to the `master` branch or when a pull request targets the `master` branch.

The workflow successfully completed the project build and tests.

GitHub Actions workflows are defined using YAML files inside the `.github/workflows` directory and can automate build and testing processes.

---

## 12. GitHub Repository

The source code for Study Better is available on GitHub:

**Repository:**
https://github.com/Ethaba/Study-Better

The repository contains the Android Studio project, application source code, tests and GitHub Actions workflow.

Git was used throughout development to keep track of changes and maintain the project source code.

---

## 13. Project Structure

The main Android project is organised using the following structure:

```text
Study-Better
│
├── .github
│   └── workflows
│       └── android.yml
│
├── app
│   └── src
│       ├── main
│       │   ├── java
│       │   └── res
│       │
│       ├── test
│       │   └── java
│       │
│       └── androidTest
│
├── gradle
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

---
## API Creation Proof

<img width="1366" height="768" alt="image" src="https://github.com/user-attachments/assets/029dc1d8-4c6c-490f-9018-506cbdb25a32" />


<img width="1366" height="768" alt="image" src="https://github.com/user-attachments/assets/8106f37f-212b-4f15-a88d-d1eac204f438" />


## 14. Demonstration Video

A demonstration video was recorded showing the functionality of the Study Better application.

The demonstration includes the main application flow, including registration, login, dashboard functionality and the application's main features.

**Demo Video:**
*https://youtu.be/P8KLJHX3Uzs* 

 Alternative link if YouTube removes the video
 *https://drive.google.com/file/d/1TSWU8SD6MR5c1MgIgvlSAW6MMUmhAaNb/view?usp=drive_link*  
 
---

## 15. Design Considerations

The application was designed with simplicity and ease of navigation in mind.

The main design considerations were:

* Clear navigation between application features.
* Consistent layouts across screens.
* Simple input forms.
* Feedback when users enter invalid information.
* Clear presentation of assignment deadlines and progress.
* Personalised information based on the logged-in user.
* A layout suitable for use on a mobile device.

The application uses XML layouts to maintain control over the structure and appearance of each screen.

---

## 16. Development and Version Control

GitHub was used as the project's source code repository.

Development changes were committed throughout the project so that changes could be tracked and the project could be restored to earlier versions if required.

GitHub Actions was added to improve the development process by automatically running tests and building the Android application after changes are pushed.

---

## 17. Future Improvements

Possible improvements for future versions of Study Better include:

* Moving more application data from local storage to the REST API.
* Adding online synchronisation for assignments and modules.
* Adding push notifications for upcoming deadlines.
* Adding more detailed study statistics.
* Adding additional authentication options.
* Hosting the API online so that the application does not depend on a locally running development server.
* Adding additional automated UI tests.


---
## 18. AI Usage Statement

AI tools were used during the development of the Study Better application as a supporting resource. They were mainly used to help explain unfamiliar Android Studio and Kotlin concepts, identify programming errors, assist with debugging, and suggest possible solutions when problems occurred during development.

AI assistance was also used to help understand API integration, Retrofit, testing and GitHub Actions. The generated suggestions were reviewed and adapted to the requirements of the application before being implemented.

The final application was tested by running the application on an Android device, testing its main functions and running the project's unit tests. GitHub Actions was also used to automatically build and test the project.


## 19. References

Android Developers. (n.d.). *Develop Android apps with Kotlin*. Available at: https://developer.android.com/kotlin (Accessed: 23 September 2026).

Android Developers. (n.d.). *Test your app*. Available at: https://developer.android.com/studio/test (Accessed: 23 September 2026).

GitHub Docs. (n.d.). *Workflows*. Available at: https://docs.github.com/en/actions/concepts/workflows-and-actions/workflows (Accessed: 23 September 2026).

GitHub Docs. (n.d.). *Quickstart for GitHub Actions*. Available at: https://docs.github.com/en/actions/get-started/quickstart (Accessed: 23 September 2026).

Microsoft. (n.d.). *Connect to and Query Azure SQL Database Using .NET and Entity Framework Core*. Microsoft Learn. Available at: https://learn.microsoft.com/en-us/azure/azure-sql/database/azure-sql-dotnet-entity-framework-core-quickstart (Accessed: 23 September 2026).

Microsoft. (n.d.). *Microsoft SQL Server Database Provider - EF Core*. Microsoft Learn. Available at: https://learn.microsoft.com/en-us/ef/core/providers/sql-server/ (Accessed: 23 September 2026).

Retrofit. (n.d.). *Retrofit: A type-safe HTTP client for Android and Java*. Available at: https://square.github.io/retrofit/ (Accessed: 23 September 2026).
