# Study Better

Study Better is an Android application developed to help students organise their academic activities and manage their studies in one place. The application combines module management, assignment tracking, study sessions, academic progress, achievements and learning resources.

The project consists of an Android application developed using Kotlin and XML, supported by an ASP.NET Core REST API and an Azure SQL Database.

**GitHub Repository:** https://github.com/Ethaba/Study-Better

---

## 1. Project Objectives

Study Better aims to help students:

- Organise their academic modules.
- Manage assignments and monitor deadlines.
- Track the time spent studying.
- Monitor academic progress.
- Access and organise learning resources.
- Track achievements and academic milestones.
- Receive notifications and study reminders.
- Access their accounts using email and password or Google Sign-In.

---

## 2. Main Features

### User Registration and Authentication

- Account registration using personal details.
- Email and password login.
- Google Sign-In using Android Credential Manager and Google Identity.
- JWT-based authentication through the backend API.

### Dashboard

The dashboard provides an overview of the student's academic activities and provides navigation to the application's main features.

### Module Management

Students can create, view, update and delete academic modules.

### Assignment Management

Students can:

- Create and manage assignments.
- Associate assignments with modules.
- Set assignment due dates.
- Select assignment priorities.
- Monitor assignment completion progress.

### Study Sessions

The study-session timer allows students to record the time they spend studying. Session information is saved locally and can be synchronised with the backend when connectivity is available.

### Academic Progress Analytics

The progress screen presents academic information such as:

- Total recorded study time.
- Completed assignments.
- Module progress.
- XP and level indicators.
- Achievement information.

XP, levels and achievement indicators are calculated within the application and should be understood as application-side progress indicators rather than independently verified academic results.

### Academic Resource Management

Students can save and organise academic resources by entering:

- Resource title.
- Description.
- Category.
- Optional external URL.

Resources can be searched, opened when a URL is available, and deleted.


### Achievements

The application includes achievement milestones based on academic activity, including study time, completed assignments and module activity.

Achievement notifications are supported through the application's notification implementation.

### Notifications and Reminders

Study Better includes:

- Firebase Cloud Messaging integration for push notifications.
- Device notification permission handling.
- FCM token registration.
- Local reminders for study and assignment activities.

Push notification delivery was tested using a message sent through the Firebase Console.

### Offline Data and Synchronisation

The Android application uses Room as local storage for supported academic information.

Local database entities and repositories are implemented for:

- Modules.
- Assignments.
- Study sessions.
- Academic resources.

WorkManager is used for background synchronisation.

---

## 3. Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML | Android user interface layouts |
| Android Studio | Development environment |
| Room | Local database |
| WorkManager | Background tasks and synchronisation |
| Firebase Cloud Messaging | Push notifications |
| Google Credential Manager | Google Sign-In |
| Retrofit | Android API communication |
| Gson Converter | JSON conversion |
| ASP.NET Core | REST API development |
| C# | Backend programming |
| Entity Framework Core | Database access |
| Azure SQL Database | Online relational database |
| JWT Bearer | API authentication |
| PasswordHasher | Password hashing |
| JUnit | Unit testing |
| GitHub | Source control |
| GitHub Actions | Automated build and testing |

---

## 4. System Architecture

Study Better follows a client-server architecture.

1. The Android application provides the user interface and handles local application data.
2. Retrofit communicates with the ASP.NET Core REST API.
3. The API handles authentication, validates requests and applies user-specific access rules.
4. Entity Framework Core communicates with Azure SQL Database.
5. Room provides local storage for supported academic information.
6. WorkManager handles background tasks and synchronisation.
7. Firebase Cloud Messaging supports push notification delivery.

---

## 5. Backend API

The backend is an ASP.NET Core Web API project named `StudyBetter.API`.

The API provides authentication and user-specific endpoints for managing academic information.

### Authentication Routes

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/Auth/register` | Register a new account |
| POST | `/api/Auth/login` | Authenticate using email and password |
| POST | `/api/Auth/google` | Authenticate using Google Sign-In |

### Academic Routes

| Feature | Endpoint |
|---|---|
| Modules | `/api/Modules` |
| Assignments | `/api/Assignments` |
| Study Sessions | `/api/StudySessions` |

The exact supported HTTP methods and request fields are defined by the API controllers and request models.

### Authentication and Security

The backend uses password hashing instead of storing passwords as plain text.

After successful authentication, the API returns a JWT authentication token. The Android application stores the token locally and includes it in the Authorization header when accessing protected endpoints.

Google Sign-In obtains an ID token on Android and sends it to the backend for validation.

---

## 6. Database and Local Storage

### Azure SQL Database

Azure SQL Database is used for online application data.

Entity Framework Core provides the backend with database access.

### Room Database

Room provides local Android storage for supported application data.

The local database includes entities for:

- Modules.
- Assignments.
- Study sessions.
- Academic resources.

Room migrations are used to update the local database structure as the application develops.

---

## 7. Firebase Cloud Messaging

Firebase Cloud Messaging is integrated into the Android application to support push notifications.

The implementation includes:

- Firebase project configuration.
- Android Firebase messaging service.
- FCM token retrieval and storage.
- Notification permission handling.
- Notification channel configuration.
- Incoming notification processing.

A test notification was successfully received on the Android device through Firebase Console.

---

## 8. Running the Project

### Android Application

1. Install Android Studio and the required Android SDK.
2. Clone or download the repository.
3. Open the Android project in Android Studio.
4. Ensure that `app/google-services.json` is present.
5. Allow Gradle to synchronise.
6. Configure the API base URL in `RetrofitClient.kt`.
7. Ensure that the backend API is running and accessible to the Android device.
8. Run the application on an emulator or physical Android device.

### Backend API

1. Install the .NET SDK required by the backend project.
2. Open the `StudyBetter.API` project.
3. Configure the Azure SQL connection string using User Secrets or environment variables.
4. Configure the Google OAuth client ID using User Secrets or environment variables.
5. Run the backend API.
6. Ensure that the Android application can reach the configured API address.

Example development command:

```powershell
dotnet run --urls "http://0.0.0.0:5293"
```

---

## 9. Testing

Testing activities include:

- Email and password authentication.
- Google Sign-In on a physical Android device.
- Module and assignment management.
- Study-session recording.
- Local Room database operations.
- Firebase push notification delivery.
- Academic resource creation, search and deletion.
- Academic progress calculations.
- Unit tests.
- Automated GitHub Actions builds and tests.

Google Sign-In was successfully tested on a physical device.

A Firebase Console test notification was also successfully received.

The backend authentication and academic endpoints were tested using Swagger.

<img width="1359" height="725" alt="Screenshot 2026-11-07 133552" src="https://github.com/user-attachments/assets/3d93bc29-85a6-4e78-995d-ffcd4587c0e8" />


---

## 10. GitHub Actions

The Android GitHub Actions workflow is located at:

`.github/workflows/android.yml`

The workflow automates the project's build and testing process when changes are pushed or pull requests are opened.

The repository's GitHub Actions workflow has completed successfully during development.

The latest workflow result can be viewed under the **Actions** tab in the GitHub repository.

---

## 11. Release and Play Store Preparation

A signed release APK was generated successfully using Android Studio's signing wizard.

### Release Information

| Item | Value |
|---|---|
| Application name | Study Better |
| Application ID | `com.example.studybetter` |
| Version name | `1.0` |
| Version code | `1` |
| Minimum SDK | 24 |
| Target SDK | 36 |
| Release format generated | Signed APK |
| APK filename | `app-release.apk` |

The release APK was approximately 8.2 MB when generated.

<img width="526" height="609" alt="Screenshot 2026-11-07 104833" src="https://github.com/user-attachments/assets/0e4d0b9a-5bb5-424f-adf3-256e855b8824" />

<img width="560" height="392" alt="Screenshot 2026-11-07 105046" src="https://github.com/user-attachments/assets/c155636f-d3da-457a-85aa-f372c96e1b1f" />


The release signing keystore is named `StudyBetter.jks`.

### Play Store Preparation Materials

The following materials were prepared:

- Application icon.
- Promotional feature graphic.
- Phone screenshots.
- Short application description.
- Full application description.
- Privacy policy.
- Store listing information.

---

## 12. Repository Structure

The Android application follows the standard Android Studio Gradle project structure.

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

---

## 13. Demonstration Video

Previous demonstration video:

https://youtu.be/P8KLJHX3Uzs

Alternative video:

https://drive.google.com/file/d/1TSWU8SD6MR5c1MgIgvlSAW6MMUmhAaNb/view?usp=drive_link

---

## 14. References

- Android Developers. Kotlin for Android. https://developer.android.com/kotlin
- Android Developers. Test your app. https://developer.android.com/studio/test
- Android Developers. Room persistence library. https://developer.android.com/training/data-storage/room
- Android Developers. WorkManager. https://developer.android.com/topic/libraries/architecture/workmanager
- Firebase. Firebase Cloud Messaging for Android. https://firebase.google.com/docs/cloud-messaging/android/get-started
- Firebase. Add Firebase to your Android project. https://firebase.google.com/docs/android/setup
- Google. Credential Manager. https://developer.android.com/identity
- GitHub Docs. GitHub Actions. https://docs.github.com/en/actions
- Microsoft Learn. Azure SQL Database with Entity Framework Core. https://learn.microsoft.com/en-us/azure/azure-sql/database/azure-sql-dotnet-entity-framework-core-quickstart
- Microsoft Learn. EF Core SQL Server provider. https://learn.microsoft.com/en-us/ef/core/providers/sql-server/
- Square. Retrofit. https://square.github.io/retrofit/

---

**Study Better**

Plan. Learn. Achieve.
