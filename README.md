# Morago
🌐 Translator Marketplace | Spring Boot

## 🧑‍💻 Project Description
Morago is an application that caters to clients needing quality translations and translators looking for work.

## ⏱️ Development Period
- 2025.3.27 ~ 2025.06.30

## ⚙️ Tech Stack

**Backend:** Java 21, Spring Boot, Spring Data JPA  
**API Docs:** Swagger  
**Testing:** JUnit 5, AssertJ, Testcontainers  
**Database:** MySQL  
**Build Tool:** Maven  
**IDE:** IntelliJ  
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

> #### :roller_coaster: Flow Chart

---

> #### 🔧 Feature Breakdown

##### 1.


