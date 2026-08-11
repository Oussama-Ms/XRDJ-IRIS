<div align="center">
  <img src="https://www.albaridbank.ma/themes/baridbank/logo.png" alt="IRIS Logo" width="100"/>
  <h1>XRDJ-IRIS</h1>
  <p><b>Intelligent Banking Data Assistant & Metrics Dashboard</b></p>
</div>

<br />

## 📖 About The Project

**XRDJ-IRIS** is a comprehensive, full-stack enterprise application designed to process, analyze, and visualize banking transaction metrics. It focuses on tracking **Compte Rendu d'Evénement (CRE)** and **Écritures Comptables (EC)** across various banking flows (e.g., Dotation, Virement). 

The platform features an **AI-powered Chatbot** equipped with Conversational Memory and Dynamic Retrieval-Augmented Generation (RAG) that allows users to seamlessly query, compare, and analyze historical data using natural language.

---

## 🚀 Tech Stack

### Frontend
- <img src="https://upload.wikimedia.org/wikipedia/commons/c/cf/Angular_full_color_logo.svg" width="15" /> **Angular 17** - Core framework
- <img src="https://upload.wikimedia.org/wikipedia/commons/4/4c/Typescript_logo_2020.svg" width="15" /> **TypeScript** - Strongly typed logic
- <img src="https://www.chartjs.org/img/chartjs-logo.svg" width="15" /> **Chart.js / ng2-charts** - Advanced data visualization
- <img src="https://upload.wikimedia.org/wikipedia/commons/d/d5/CSS3_logo_and_wordmark.svg" width="15" /> **Vanilla CSS** - Custom responsive styling
- 📄 **jsPDF & xlsx** - Exporting reports to PDF and Excel
- 🔌 **STOMP / SockJS** - Real-time WebSocket communication

### Backend
- <img src="https://upload.wikimedia.org/wikipedia/commons/7/79/Spring_Boot.svg" width="15" /> **Spring Boot 3.2.3** - Core backend framework (Java 17)
- 🧠 **Spring AI (Ollama)** - LLM integration and Conversational Memory
- ⚙️ **Spring Batch** - High-performance file parsing and data ingestion
- 🔐 **Spring Security & JWT** - Authentication and role-based access control
- 🐘 **PostgreSQL** - Relational database for transaction storage
- 🔄 **Spring WebSockets** - Real-time streaming for AI chat responses
- 🌶️ **Lombok** - Boilerplate reduction

---

## ✨ Key Features

### 1. Intelligent AI Assistant (Dynamic RAG)
- **Conversational Memory:** Remembers previous questions in the session (e.g., asking "What about yesterday?" right after asking about today).
- **Dynamic Context Injection:** Instantly scans prompts for keywords (*"today"*, *"yesterday"*, *"all time"*) and autonomously runs optimized PostgreSQL queries to feed 100% accurate context to the LLM.
- **Multi-Context Comparison:** Capable of comparing multiple dates (e.g., *"Compare yesterday with all time data"*) by stitching multiple database aggregates into a single AI context.
- **Real-Time Streaming:** Streams the AI's response token-by-token directly to the Angular frontend via WebSockets.

### 2. High-Performance Data Processing
- **Spring Batch Jobs:** Automated ingestion of massive `RuleCounterRecord` and `AnomalyRecord` files.
- **File Archiving:** Tracks ingestion dates and statuses across all uploaded files via the `FileArchive` module.
- **Robust Parsing:** Safely parses complex banking fluxes and determines global status (e.g., *Traité complètement*, *Rejeté partiellement*).

### 3. Data Visualization & Reporting
- **Interactive Dashboards:** Built with Chart.js to visualize processed vs. rejected CREs and ECs.
- **Export Capabilities:** Instantly export any metrics table or visualization directly to PDF or Excel.

### 4. Enterprise Security
- **JWT Authentication:** Secure stateless session management for all API endpoints.
- **Role-Based Access:** Protects sensitive batch triggers and data streams.

---

## 📂 Project Structure

```text
XRDJ-IRIS/
├── backend/                  # Spring Boot Java Application
│   ├── src/main/java/.../iris/
│   │   ├── ai/               # AI Tools & Dynamic RAG Logic
│   │   ├── controller/       # REST APIs & ChatController
│   │   ├── model/            # JPA Entities (RuleCounterRecord, etc.)
│   │   ├── repository/       # Spring Data JPA Repositories
│   │   └── service/          # Business Logic & Batch Launchers
│   └── pom.xml               # Maven Dependencies
│
└── frontend/                 # Angular 17 Application
    ├── src/app/
    │   ├── components/       # Chat Widget, Layout, Visualize
    │   ├── services/         # API HTTP & WebSocket Services
    │   └── guards/           # Route Guards (Auth)
    └── package.json          # Node Dependencies
```

---

## 🛠️ Getting Started

### Prerequisites
- Java 17+
- Node.js (v18+)
- PostgreSQL installed and running
- Ollama (running locally with Llama 3 or preferred model)

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Configure your PostgreSQL and Ollama settings in `application.properties`.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Angular development server:
   ```bash
   npm start
   ```
4. Open `http://localhost:4200` in your browser.

---

## 📝 License
This project is proprietary and confidential. Unauthorized copying of this repository, via any medium, is strictly prohibited.
