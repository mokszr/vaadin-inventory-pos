<p align="center">
  <img src="/assets/images/stock_inventory.jpg" alt="MyStock logo" width="200">
</p>

# myStock — Sample Vaadin Inventory & POS Application

**myStock** is a sample **Inventory & POS (Point of Sale)** application built with **Vaadin Flow** and **Spring Boot**.  
It is designed as a **learning, demo, and reference project** that demonstrates how to build a real-world business application using modern Java technologies.

This project focuses on **clarity, correctness, and realistic domain modeling**, rather than UI tricks or shortcuts.

---
<p align="center">
  <img src="/assets/images/myStock_vaadin_spring_boot_app.jpg" alt="MyStock Screenshot" width="500">
</p>

---

## ✨ Features

### Inventory & Products
- Product management (name, barcode, unit, active flag)
- One-to-one inventory tracking per product
- Reorder level (minimum stock threshold)
- Low-stock detection

### Stock Management
- Stock movements with **IN / OUT** types
- Quantity tracking with history
- Optional notes for each movement

### POS (Point of Sale)
- Scan / enter barcode
- Cart-based checkout
- Stock validation before sale
- Sale & sale line creation

### Dashboard
- Revenue KPIs
- Sales trend (last 14 days)
- Top products by revenue
- Low-stock product list

### Technical
- Internationalization (i18n) — English & Turkish
- Spring Security ready
- Demo data seeding for charts
- Clean domain-driven structure

---

## 🧱 Domain Model Overview

### Core Entities
- **Product** — sellable items with barcode
- **StockItem** — current quantity, reorder level, location
- **StockMovement** — IN / OUT inventory operations
- **Sale** — a completed checkout
- **SaleLine** — individual items within a sale

### Key Design Rule
- **Stock** = current quantity on hand
- **Inventory** = full system (products, stock, movements, sales, analytics)

---

## 🛠️ Technology Stack

### Backend
- Java 25+
- Spring Boot 4
- Spring Data JPA
- Hibernate
- PostgreSQL

### Frontend
- Vaadin Flow 25 (100% Java UI)
- Chart.js (for dashboards)

### Other
- Spring Security
- i18n with `.properties` files
- Maven / Gradle compatible

---

## 🌍 Internationalization (i18n)

The application uses **key-based translations** with placeholders.

Example:
```properties
pos.checkout.success=Payment completed: {0}
pos.checkout.success=Ödeme alındı: {0}
```

---

## Open-source & Independent Developer Project

MyStock is fully open source and developed independently by **Murat Öksüzer**.

If MyStock has been helpful and you’d like to support the project, even a small donation provides great motivation.

---
## Support the Project ☕

If you’d like to support, you can buy me a coffee:

<a href="https://www.buymeacoffee.com/muratoksuzer" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="60" width="217">
</a>

If you can’t donate, no worries — starring the GitHub repo, sharing the project, or giving feedback also helps a lot. ❤️

---

## Contact Me

I’d love to hear your thoughts, suggestions, and feedback. You can reach me or follow the project through the links below:

- 📧 **Email:** [muratoksuzer01@gmail.com](mailto:muratoksuzer01@gmail.com)
- ⭐ **GitHub:** [MyStock Repository](https://github.com/mokszr/vaadin-inventory-pos)
- ▶️ **YouTube:** [@muratoksuzer](https://www.youtube.com/@muratoksuzer)
- 💼 **LinkedIn:** [My Profile](https://www.linkedin.com/in/murat-%C3%B6ks%C3%BCzer-bb856644/)
- 🕊️ **X (Twitter):** [@murat_dev01](https://x.com/murat_dev01)
- 🌐 **Website:** [www.muratoksuzer.com](https://www.muratoksuzer.com/)

---

## License

MyStock is licensed under the **Apache License 2.0**. See the full license text in the [LICENSE](LICENSE) file.

---

