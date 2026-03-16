# \# 🏦 Secure Banking API

# 

# A Banking Backend built with Java 17 and Spring Boot.

# 

# \---

# 

# \## 📁 Project Structure

# ```

# src/

# ├── main/

# │   ├── java/com/banking/

# │   │   ├── BankingApplication.java

# │   │   ├── audit/

# │   │   │   ├── Auditable.java

# │   │   │   ├── AuditService.java

# │   │   │   └── AuditAspect.java

# │   │   ├── config/

# │   │   │   ├── SecurityConfig.java

# │   │   │   └── OpenApiConfig.java

# │   │   ├── controller/

# │   │   │   ├── AccountController.java

# │   │   │   ├── TransactionController.java

# │   │   │   └── AuthController.java

# │   │   ├── dto/

# │   │   │   ├── AccountRequest.java

# │   │   │   ├── AccountResponse.java

# │   │   │   ├── AmountRequest.java

# │   │   │   ├── TransferRequest.java

# │   │   │   ├── TransactionResponse.java

# │   │   │   ├── AuthDto.java

# │   │   │   └── ApiResponse.java

# │   │   ├── exception/

# │   │   │   ├── CustomException.java

# │   │   │   └── GlobalExceptionHandler.java

# │   │   ├── model/

# │   │   │   ├── Account.java

# │   │   │   ├── Transaction.java

# │   │   │   ├── User.java

# │   │   │   └── AuditLog.java

# │   │   ├── repository/

# │   │   │   ├── AccountRepository.java

# │   │   │   ├── TransactionRepository.java

# │   │   │   ├── UserRepository.java

# │   │   │   └── AuditLogRepository.java

# │   │   ├── security/

# │   │   │   ├── JwtService.java

# │   │   │   └── JwtAuthFilter.java

# │   │   └── service/

# │   │       ├── AccountService.java

# │   │       ├── TransactionService.java

# │   │       └── AuthService.java

# │   └── resources/

# │       └── application.properties

# └── test/

# &#x20;   └── java/com/banking/

# &#x20;       ├── controller/

# &#x20;       │   └── AccountControllerTest.java

# &#x20;       └── service/

# &#x20;           ├── AccountServiceTest.java

# &#x20;           └── TransactionServiceTest.java

# ```

# 

# \---

# 

# \## 🚀 How to Run

# ```bash

# mvn spring-boot:run

# ```

# 

# \---

# 

# \## 🌐 URLs

# 

# | URL | Purpose |

# |-----|---------|

# | http://localhost:8080/swagger-ui.html | Swagger API Docs |

# | http://localhost:8080/h2-console | H2 Database |

# 

# \---

# 

# \## 📡 API Endpoints

# 

# | Method | URL | Description |

# |--------|-----|-------------|

# | POST | `/api/auth/register` | Register user |

# | POST | `/api/auth/login` | Login get token |

# | POST | `/api/accounts` | Create account |

# | GET | `/api/accounts/{id}` | Get account |

# | GET | `/api/accounts/{id}/balance` | View balance |

# | POST | `/api/accounts/{id}/deposit` | Deposit money |

# | POST | `/api/accounts/{id}/withdraw` | Withdraw money |

# | POST | `/api/accounts/transfer` | Transfer money |

# | GET | `/api/transactions/{accountId}` | Transaction history |

# 

# \---

# 

# \## 🛠 Tech Stack

# 

# | Technology | Purpose |

# |------------|---------|

# | Java 17 | Programming Language |

# | Spring Boot 3.2.3 | Backend Framework |

# | Spring Security + JWT | Authentication |

# | Spring Data JPA | Database |

# | H2 Database | In-Memory DB |

# | Swagger/OpenAPI | API Docs |

# | Lombok | Less Code |

# | JUnit 5 + Mockito | Testing |

# 

# \---

# 

# \## ✅ Features

# 

# \- ✅ Create bank account

# \- ✅ Deposit money

# \- ✅ Withdraw money

# \- ✅ Transfer between accounts

# \- ✅ Transaction history with pagination

# \- ✅ JWT Authentication

# \- ✅ Global Exception Handling

# \- ✅ Audit Logging using AOP

# \- ✅ Idempotency for transfers

# \- ✅ Swagger API Documentation

# \- ✅ Unit Tests

