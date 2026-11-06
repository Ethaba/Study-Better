# Study Better

Study Better is an Android application designed to help students organise their academic work in one place. It brings together module and assignment management, study-session tracking, academic progress and learning resources.

This repository contains the Android application and its supporting ASP.NET Core REST API.

## Project objectives

The application aims to help students:
- Keep track of academic modules and assignments.
- Monitor assignment deadlines and completion.
- Record study sessions and review study activity.
- View academic progress and achievements.
- Keep useful academic resources together.
- Access their account through standard login or Google Sign-In.

## Main features

### Core application
- User registration and email/password login.
- Google Sign-In through Google Identity / Credential Manager.
- Personalised dashboard.
- Module management.
- Assignment management.
- Study-session timer and session history.
- Academic progress screen.
- Academic resource management.
- Profile and application settings.

### Offline data
The Android application uses Room as local storage for supported academic data. The project includes local entities and data-access components for modules, assignments, study sessions and academic resources. Synchronisation is handled through the application's background-work implementation.


### Notifications
Firebase Cloud Messaging is being integrated to support push notifications. The Android project has been connected to a Firebase project and includes the initial messaging-service and notification-permission implementation.


### Languages
The intended language support for the project includes English, isiZulu and isiXhosa.

## Technology stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML | Android screen layouts |
| Android Studio | Android development environment |
| Room | Local on-device database |
| WorkManager | Background work and synchronisation |
| Firebase Cloud Messaging | Push notification delivery |
| Android Credential Manager / Google Identity | Google Sign-In |
| ASP.NET Core | REST API |
| C# | Backend implementation |
| Entity Framework Core | Database access |
| Azure SQL Database | Online relational database |
| Retrofit | Android-to-API communication |
| Gson Converter | JSON conversion |
| JWT Bearer | API authentication |
| PasswordHasher | Password hashing |
| JUnit | Unit testing |
| GitHub | Source control |
| GitHub Actions | Automated build and test workflow |

## System architecture

Study Better follows a client-server design.

1. The Android application provides the user interface and manages local application data.
2. Retrofit sends API requests from Android to the ASP.NET Core backend.
3. The backend validates requests, applies user-specific access rules and handles authentication.
4. Entity Framework Core communicates with Azure SQL Database.
5. Room stores supported data locally so that the application can access it without relying on a live API connection.
6. WorkManager is used for scheduled background work, including data synchronisation.
7. Firebase Cloud Messaging is used for push-notification delivery.

## Backend API

The backend is an ASP.NET Core Web API project named `StudyBetter.API`.

The API includes authentication endpoints for registration and login, Google authentication, and user-scoped endpoints for modules, assignments and study sessions.

Known routes include:

| Feature | Route |
|---|---|
| Register | `POST /api/Auth/register` |
| Login | `POST /api/Auth/login` |
| Google login | `POST /api/Auth/google` |
| Modules | `api/Modules` |
| Assignments | `api/Assignments` |
| Study sessions | `api/StudySessions` |

The exact supported HTTP methods and request fields are defined by the API controllers and DTOs in the source project.

### Authentication

For email/password authentication, the API hashes passwords rather than storing them as plain text. After successful authentication, the API returns a JWT. The Android application includes this token in the Authorization header for protected requests.

Google Sign-In obtains an ID token on Android and sends it to the backend. The backend validates the token against the configured Google Web OAuth client ID, checks that the email is verified, and then signs the user in or creates an account.

## Database and local storage

Azure SQL Database is used for online data. Entity Framework Core provides the backend database access.

Room is used on Android for local data. The local database contains entities for:
- Modules
- Assignments
- Study sessions
- Academic resources

Room migrations are used to update the local database schema as new entities and fields are introduced.

## Academic resources

The Academic Resources feature allows a student to save resource details such as a title, description, category and optional URL. Resource records are stored locally in Room and can be searched, opened when a URL is supplied, and deleted.

The current implementation stores resource information and links. It should not be described as uploading or storing the actual contents of PDF or other files unless file storage is added separately.

## Academic progress

The Progress screen uses locally available module, assignment and study-session data to display study time, completed assignments, module progress, XP and level information.

The current achievement and XP calculations are application-side progress indicators. They should not be represented as server-validated awards unless persistent achievement handling is added.

## Running the project

### Android application
1. Install Android Studio and the required Android SDK.
2. Clone or download this repository.
3. Open the Android project in Android Studio.
4. Confirm that `app/google-services.json` is present for Firebase configuration.
5. Allow Gradle to sync.
6. Configure the API base URL in `RetrofitClient.kt` to point to the backend environment being used.
7. Run the app on an emulator or a physical Android device.

### Backend API
1. Install the .NET SDK version required by the backend project.
2. Open the `StudyBetter.API` project.
3. Configure the Azure SQL connection string using User Secrets or environment variables.
4. Configure `Google:ClientId` using User Secrets or an environment variable.
5. Run the API project.
6. Ensure the Android device can reach the API host and that the configured base URL and network settings are correct.

Example development command:

```powershell
dotnet run --urls "http://0.0.0.0:5293"
```

This exposes the development API on the local network interface. Use a secure, hosted HTTPS endpoint for production.


## GitHub Actions

The Android workflow is located at:

`.github/workflows/android.yml`

The workflow is intended to automate the Android build and unit-test process when changes are pushed or pull requests are opened.

## Repository

GitHub repository: https://github.com/Ethaba/Study-Better

## Project structure

The Android project follows the standard Gradle structure:

```text
Study-Better/
├── .github/
│   └── workflows/
│       └── android.yml
├── app/
│   ├── google-services.json
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── res/
│       ├── test/
│       └── androidTest/
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

The backend API is maintained in the `StudyBetter.API` project.

## Demonstration video

Previous demonstration video: https://youtu.be/P8KLJHX3Uzs

Alternative video link: https://drive.google.com/file/d/1TSWU8SD6MR5c1MgIgvlSAW6MMUmhAaNb/view?usp=drive_link


## References

- Android Developers. Kotlin for Android: https://developer.android.com/kotlin 
- Android Developers. Test apps on Android: https://developer.android.com/studio/test
- Android Developers. Room persistence library: https://developer.android.com/training/data-storage/room
- Android Developers. WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager
- Firebase. Set up Firebase Cloud Messaging for Android: https://firebase.google.com/docs/cloud-messaging/android/get-started
- Firebase. Add Firebase to your Android project: https://firebase.google.com/docs/android/setup
- Google. Credential Manager: https://developer.android.com/identity
- GitHub Docs. GitHub Actions workflows: https://docs.github.com/en/actions
- Microsoft. EF Core with Azure SQL: https://learn.microsoft.com/en-us/azure/azure-sql/database/azure-sql-dotnet-entity-framework-core-quickstart
- Microsoft. EF Core SQL Server provider: https://learn.microsoft.com/en-us/ef/core/providers/sql-server/
- Square. Retrofit: https://square.github.io/retrofit/
