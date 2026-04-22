# 🍽️ Restaurant Automation System (RAS)

![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-GUI-blue?style=for-the-badge)
![SQLite](https://img.shields.io/badge/SQLite-Database-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)

A robust, full-featured Restaurant Automation System built with Java Swing and SQLite. This project implements a comprehensive set of software engineering requirements (FR-001 to FR-030), demonstrating professional application design, clean MVC architecture, and a modern dark-themed user interface.

## ✨ Key Features

- **Role-Based Access Control:** Secure logins for Managers, Clerks, and Storekeepers with SHA-256 password hashing and account lockout mechanisms.
- **Smart Point of Sale (POS):** Fast and intuitive billing system for clerks to manage customer orders and generate printed receipts.
- **Automated Inventory Management:** Tracks real-time stock levels and automatically calculates minimum thresholds based on a dynamic formula (`ceil(average 3-day usage) * 2`).
- **Auto-Generated Purchase Orders:** One-click generation of purchase orders for all ingredients that fall below their minimum threshold.
- **Invoice & Cheque Processing:** Records received goods and automatically prints cheques (with amounts dynamically converted to words) based on the current cash balance.
- **Comprehensive Analytics:** Generates printable Monthly Sales, Monthly Expenses, and Statistical Sales reports.
- **Premium UI:** Features a sleek, responsive dark mode design powered by FlatLaf.

## 🚀 How to Run

### Prerequisites
- **Java Development Kit (JDK) 11** or higher.
- Terminal or Command Prompt.

### Starting the Application
1. Open your terminal and navigate to the project directory.
2. Run the application using the included Gradle wrapper:

**Windows:**
```powershell
.\gradlew.bat run
```

**Mac/Linux:**
```bash
./gradlew run
```

*Note: The first time you run this, Gradle will automatically download the necessary dependencies (SQLite JDBC, FlatLaf) and compile the project.*

## 🔐 Default Login Credentials

The system comes pre-configured with the following test accounts:

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Manager** | `manager` | `manager123` | Full access (POS, Inventory, POs, Invoices, Menu Management, Reports) |
| **Clerk** | `clerk` | `clerk123` | Limited access (Dashboard, New Order POS) |

> **Security Note:** Entering an incorrect password 5 consecutive times will lock the account.

## 🏗️ Architecture & Technology Stack

- **Frontend:** Java Swing with `FlatLaf` for a modern, flat dark theme.
- **Backend:** Plain Old Java Objects (POJOs) utilizing a clean MVC pattern.
- **Database:** Local SQLite database (`ras_data.db`) managed via native JDBC and DAO pattern.
- **Build System:** Gradle for automated dependency management and compilation.

## 🧪 Testing the Workflow (Presentation Guide)

For a complete demonstration of the system's capabilities during your software engineering presentation, follow this workflow:

1. **Analyze Inventory:** Log in as `manager`. Go to **Inventory** to view low stock items (highlighted in red). Show how thresholds can be auto-recalculated.
2. **Auto-Generate POs:** Navigate to **Purchase Orders** and click the `Auto-Generate POs` button. Show how it intelligently creates orders only for below-threshold ingredients.
3. **Pay Supplier:** Go to **Invoices**, receive the goods for the pending PO, and watch the system automatically check the cash balance and print a formatted cheque.
4. **Make Sales:** Log out, log in as `clerk`, and punch in a few customer orders in the **New Order** tab to generate printed bills and increase the restaurant's cash balance.
5. **View Reports:** Log back in as `manager` and navigate to **Reports** to view and print the live sales analytics generated from your recent activity.
