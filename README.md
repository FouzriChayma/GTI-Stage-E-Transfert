# E-Transfert - Banking Transfer Management System

A comprehensive full-stack banking application for managing transfer requests, scheduling appointments, real-time customer support, and push notifications. Built with Spring Boot backend and Angular frontend.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Documentation](#detailed-documentation)
- [Contributing](#contributing)

## 🎯 Overview

E-Transfert is a modern banking platform that enables clients to:
- Initiate and manage transfer requests
- Schedule appointments with bank representatives
- Communicate with customer support via real-time chat
- Receive push notifications for important updates
- View comprehensive statistics and analytics

The system supports multiple user roles:
- **CLIENT**: Create transfer requests, schedule appointments, chat with support
- **CHARGE_CLIENTELE**: Validate/reject transfers, manage customer support
- **ADMINISTRATOR**: Full system access, user management, comprehensive analytics

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication with refresh tokens
- Role-based access control (RBAC)
- Password encryption with BCrypt
- Session management
- Secure password change with verification

### 💸 Transfer Management
- Create transfer requests with multiple account types
- Document upload support
- Beneficiary management
- Transfer status tracking (PENDING, VALIDATED, REJECTED, INFO_REQUESTED)
- Automatic notifications for status changes

### 📅 Appointment Scheduling
- Schedule appointments with bank representatives
- Multiple meeting types (VIDEO_CALL, PHONE_CALL, IN_PERSON)
- Time slot availability checking
- Automated reminder notifications

### 💬 Real-time Customer Support
- WebSocket-based chat system (STOMP protocol)
- Agent assignment
- Message read status
- Real-time message broadcasting

### 🔔 Push Notifications
- Real-time push notifications via WebSocket
- User-specific and role-based notifications
- Notification types:
  - Transfer validated/rejected
  - Transfer pending (for agents)
  - Appointment reminders
  - Support messages
  - Daily reports (for administrators)

### 📊 Statistics & Analytics
- Comprehensive user statistics
- Transfer request analytics with charts
- Appointment statistics
- Monthly trends and status distribution

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Java Version**: 17
- **Build Tool**: Maven
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT
- **Real-time**: Spring WebSocket (STOMP)
- **Scheduling**: Spring Scheduling

### Frontend
- **Framework**: Angular 19.0
- **Language**: TypeScript 5.6
- **UI Library**: PrimeNG 19.1
- **Icons**: PrimeIcons 7.0
- **Styling**: Tailwind CSS 3.4, SCSS
- **Real-time**: STOMP.js, SockJS
- **Charts**: Chart.js 4.4
- **PDF Generation**: jsPDF 3.0

## 📁 Project Structure

```
stageGTI/
├── backend/
│   └── E-Transfert/
│       └── E-Transfert/          # Spring Boot Backend
│           ├── src/
│           │   ├── main/
│           │   │   ├── java/      # Java source code
│           │   │   └── resources/ # Configuration files
│           │   └── test/          # Test files
│           ├── pom.xml
│           └── README.md          # Detailed backend documentation
│
└── sakai-ng-master/
    └── sakai-ng-master/           # Angular Frontend
        ├── src/
        │   ├── app/               # Application source code
        │   │   ├── components/   # Angular components
        │   │   ├── services/     # Angular services
        │   │   ├── guards/       # Route guards
        │   │   └── models/       # TypeScript models
        │   └── assets/           # Static assets
        ├── package.json
        └── README.md              # Detailed frontend documentation
```

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

### Backend Prerequisites
- **Java JDK 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code)

### Frontend Prerequisites
- **Node.js** (v18 or higher)
- **npm** (v9 or higher) or **yarn**
- **Angular CLI** (v19.0.6 or higher)
  ```bash
  npm install -g @angular/cli
  ```

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/FouzriChayma/GTI-Stage-E-Transfert.git
cd GTI-Stage-E-Transfert
```

### 2. Backend Setup

```bash
cd backend/E-Transfert/E-Transfert

# Configure database in src/main/resources/application.properties
# Update MySQL connection details

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### 3. Frontend Setup

```bash
cd sakai-ng-master/sakai-ng-master

# Install dependencies
npm install

# Configure environment
# Update src/app/environments/environment.ts with backend API URL

# Start development server
npm start
```

The frontend will be available at `http://localhost:4200`

### 4. Database Setup

1. Create a MySQL database
2. Update `application.properties` with your database credentials
3. The application will automatically create tables on first run (if JPA auto-ddl is enabled)

## 📚 Detailed Documentation

For more detailed information, please refer to:

- **[Backend Documentation](./backend/E-Transfert/E-Transfert/README.md)**
  - API endpoints
  - Database schema
  - Security configuration
  - WebSocket setup
  - Testing guide

- **[Frontend Documentation](./sakai-ng-master/sakai-ng-master/README.md)**
  - Component structure
  - Services and routing
  - Authentication flow
  - Real-time features
  - Styling guide

## 🔧 Configuration

### Backend Configuration

Update `backend/E-Transfert/E-Transfert/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/etransfert
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your_secret_key
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Frontend Configuration

Update `sakai-ng-master/sakai-ng-master/src/app/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'http://localhost:8080/ws'
};
```

## 🧪 Testing

### Backend Tests
```bash
cd backend/E-Transfert/E-Transfert
mvn test
```

### Frontend Tests
```bash
cd sakai-ng-master/sakai-ng-master
npm test
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/change-password` - Change password

### Transfer Requests
- `POST /api/transfer-requests` - Create transfer request
- `GET /api/transfer-requests` - Get all transfers
- `GET /api/transfer-requests/{id}` - Get transfer by ID
- `PUT /api/transfer-requests/{id}/status` - Update transfer status

### Appointments
- `POST /api/appointments` - Create appointment
- `GET /api/appointments` - Get all appointments
- `GET /api/appointments/availability` - Check availability

### Messages
- `POST /api/messages` - Send message
- `GET /api/messages/conversations` - Get conversations
- `GET /api/messages/conversations/{id}` - Get conversation messages

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is part of a GTI internship project.

## 👤 Author

**Fouzri Chayma**
- GitHub: [@FouzriChayma](https://github.com/FouzriChayma)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Angular team for the powerful frontend framework
- PrimeNG for the comprehensive UI component library

---

For detailed setup instructions and API documentation, please refer to the individual README files in the backend and frontend directories.

