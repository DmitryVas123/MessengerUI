# Messenger Application

A real-time messaging application built with JavaFX that demonstrates client-server architecture, multithreading, and modern UI design principles.

## 📋 Project Overview

This messenger application showcases fundamental concepts in network programming and desktop application development. It implements a custom XML-based protocol for client-server communication and provides a clean, intuitive user interface.

**Development Context**: Created as a course project for university 

## ✨ Key Features

- **Real-time Messaging**: Instant message delivery between connected users
- **User Presence**: Online/offline status tracking for all users
- **Private Conversations**: One-to-one messaging with conversation history
- **Intuitive UI**: Clean, modern interface built with JavaFX
- **Multi-threaded Architecture**: Efficient handling of concurrent connections
- **Custom Protocol**: XML-based message format for structured communication

## 🛠️ Technical Stack

- **Language**: Java 24
- **UI Framework**: JavaFX 17.0.6
- **Build Tool**: Maven 3.8.5
- **Architecture**: Client-Server with TCP/IP
- **Protocol**: Custom XML-based messaging

## 📁 Project Structure

```
MessengerUI/
├── src/main/java/msg/messengerui/
│   ├── HelloApplication.java      # JavaFX application entry point
│   ├── HelloController.java       # Main UI controller
│   ├── client/
│   │   └── Client.java           # Console-based client (testing)
│   ├── server/
│   │   ├── Server.java           # Main server application
│   │   └── ClientHandler.java    # Handles individual client connections
│   └── common/
│       └── XMLBuilder.java        # XML message builder utility
└── src/main/resources/msg/messengerui/
    ├── hello-view.fxml            # UI layout definition
    ├── style.css                  # Light theme styles
    └── dark-theme.css             # Dark theme styles (prepared)
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MessengerUI
   ```

2. **Start the Server**
   ```bash
   mvn compile
   mvn exec:java -Dexec.mainClass="msg.messengerui.server.Server"
   ```
   Server will start on port 12345

3. **Launch the Client Application**
   ```bash
   mvn javafx:run
   ```
   Or using Maven wrapper:
   ```bash
   ./mvnw javafx:run
   ```

4. **Connect Multiple Clients**
   - Run additional instances using the same command
   - Each client will prompt for a username
   - Users appear in the online users list automatically

## 💻 Usage

1. **Login**: Enter your username when prompted
2. **Select User**: Click on any online user from the left panel
3. **Send Message**: Type your message and press Enter or click "Send"
4. **View Notifications**: System messages appear in the bottom notification panel

## 🏗️ Architecture Overview

### Client-Server Model

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Client 1  │◄────────┤   Server    │────────►│   Client 2  │
│  (JavaFX UI)│  TCP/IP │ (Port 12345)│  TCP/IP │  (JavaFX UI)│
└─────────────┘         └─────────────┘         └─────────────┘
```

### Message Flow

1. Client sends XML-formatted message to server
2. Server parses the message and identifies recipient
3. Server forwards message to target client
4. Client updates UI in real-time using Platform.runLater()

### XML Protocol Examples

**Authentication**:
```xml
<auth username="user1" password="1234" />
```

**Message**:
```xml
<message from="user1" to="user2" type="text">Hello!</message>
```

**Status Update**:
```xml
<status user="user1" status="online" />
```

## 🎯 Key Technical Implementations

### Multithreading
- Separate threads for reading server messages and handling UI updates
- Thread-safe client list management on the server
- Daemon threads prevent application hanging on close

### Network Programming
- Socket-based TCP/IP communication
- BufferedReader/PrintWriter for text-based protocol
- Graceful connection handling and cleanup

### JavaFX Best Practices
- FXML-based UI design for separation of concerns
- Platform.runLater() for thread-safe UI updates
- Observable collections for dynamic user list

## 🧪 Testing

Start the server and multiple client instances to test:
- Message delivery between users
- User connection/disconnection handling
- Online status updates
- Error handling for offline users

## 📝 Skills Demonstrated

- **Object-Oriented Programming**: Clean class structure and separation of concerns
- **Network Programming**: Socket communication, protocol design
- **Multithreading**: Concurrent client handling, thread safety
- **UI Development**: JavaFX, FXML, CSS styling
- **Design Patterns**: MVC architecture, Builder pattern (XMLBuilder)
- **Error Handling**: Graceful exception handling and user feedback
- **Build Tools**: Maven project configuration


## 👤 Author

Dmytro Vasylets. Developed as a course project for university

## 📄 License

This project is created for educational and portfolio purposes.

---

**Note**: This is a learning project demonstrating fundamental concepts in Java development, networking, and UI design. It is not intended for production use.
