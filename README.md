# Smart Outbound Dispatcher on SAP Integration Suite  
**Using Value Mapping + Groovy Routing**

This repository contains an example design for a **single outbound dispatcher** on **SAP Integration Suite** that routes multiple business object types (e.g. Equipment, Functional Location, Orders) to different endpoints using **Value Mapping** and a **Groovy script**.

Instead of maintaining one iFlow per object, this pattern allows you to have:

- **1 integration flow** for all outbound objects  
- **Centralized routing configuration** in Value Mapping (JSON-based)  
- **Reusable Groovy logic** via Script Collection  
- **Standardized exception handling and monitoring**

> All names in this repository (e.g. `XYZ_DistributionInterfaces_v1`) are anonymized placeholders.

---

## 📐 Architecture Overview

### High-Level Flow

1. **Sender** (e.g. SAP S/4HANA) sends an XML message with an object type and related data.  
2. The **Outbound Dispatcher iFlow**:
   - Reads the object type from the payload  
   - Looks up routing configuration from **Value Mapping**  
   - Parses JSON from Value Mapping via **Groovy**  
   - Sets dynamic headers (endpoint, SOAPAction, etc.)  
3. A single **SOAP Receiver** calls the correct partner endpoint using those dynamic values.  
4. An **exception subprocess** captures errors and forwards them to a central **Exception Handling** process.

### Example iFlow (Dispatcher)

```text
Sender (SOAP)
   │
   ▼
[Start Message of Object Changes]
   │
   ▼
[Set Values of the Object (Groovy Routing)]
   │
   ▼
[Router of the Object Changes] -- error --> [Exception Subprocess]
   │
   ▼
[End Message of Object Changes] --> SOAP Receiver
