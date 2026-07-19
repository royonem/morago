# morago

🌐 Translator Marketplace | Spring Boot

## 🧑‍💻 Project Description
Morago is a solo project application that caters to clients needing quality translations and translators looking for work.

## ⏱️ Development Period
- 2025.3.27 ~ 2025.06.30

## ⚙️ Tech Stack

**Backend:** Java 21, Spring Boot, Spring Data JPA  
**API Docs:** Swagger  
**Testing:** JUnit 5, AssertJ, Testcontainers  
**Database:** MySQL  
**Build Tool:** Maven  
**Deployment:** Railway


## 🌱 ERD

<img width="1661" height="914" alt="Morago_ERD_260717" src="https://github.com/user-attachments/assets/a865803b-7708-40ab-9487-0f62f8d33d73" />

* **Users**: Core user accounts — covers clients, translators, and admins (role distinguished via `Users_Roles`), including auth credentials, contact info, and availability status
* **Roles**: Defines the permission levels (ADMIN, TRANSLATOR, CLIENT) assignable to users
* **Topics**: Specific subjects a translator may specialize in and/or a client may request (e.g. Hospital, Restaurant, Factory)
* **Categories**: Higher-level grouping for topics
* **Languages**: Supported languages on the platform, used to indicate which languages a translator specializes in
* **Calls**: A call session between a client and translator — tracks lifecycle status, timing, duration limits, and cost
* **Wallets**: Each user's balance, used to pay for or earn from calls
* **Transactions**: Ledger of all wallet balance changes — deposits, call charges, call earnings, and withdrawals
* **Withdrawals**: Requests from users to cash out wallet balance to a linked bank account — requires admin approval before payout
* **Bank_Accounts**: Linked bank account details used for withdrawal payouts and/or deposits
* **Files**: Uploaded file metadata (e.g. profile pictures, topic icons)
* **Notifications**: In-app notifications sent to users
* **Refresh_Tokens**: Persisted JWT refresh tokens for maintaining authenticated sessions



## 💡 Main Features

### 1. Authentication
<img width="2400" height="1350" alt="1_Secure-Access-with-JWT-and-Refresh-Tokens" src="https://github.com/user-attachments/assets/55676398-81ad-408a-bfff-902bdbfe8358" />

### 2. User Management
<img width="2400" height="1350" alt="7_User-Management-and-Localization" src="https://github.com/user-attachments/assets/e287b1b5-1da8-4877-9875-7ed0163711ae" />

### 3. Topics
<img width="2400" height="1350" alt="6_Topic-and-Category-Management" src="https://github.com/user-attachments/assets/b3824ef8-d9f9-49d7-b56c-2ce1d33c916e" />

### 4. Financials
<img width="2400" height="1350" alt="4_Banking-Transactions-and-Withdrawals" src="https://github.com/user-attachments/assets/f78e202f-9ef5-4205-bba2-544631c8249f" />

### 5. Live Calls
<img width="2400" height="1368" alt="End-to-End-Call-Management" src="https://github.com/user-attachments/assets/d80aabcb-0900-4436-b2d9-0493da71c3e7" />

### 6. Notifications
<img width="2400" height="1350" alt="5_Notification-System" src="https://github.com/user-attachments/assets/fd0363be-b661-48e7-a0ba-39c0daa90e18" />

### 7. File Management
<img width="2400" height="1350" alt="3_File-Upload-Storage-and-Deletion" src="https://github.com/user-attachments/assets/c7f5f193-e40e-438d-bc5f-43f46a7973b0" />
