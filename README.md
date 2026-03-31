# 🔐 Ecommerce Full Stack - M6 (Spring Boot + Security)

Aplicación ecommerce desarrollada como parte de mi formación en desarrollo Full Stack, enfocada en la implementación de autenticación, autorización y gestión de usuarios con roles.

🔗 Parte de proyecto completo:
Frontend | Backend | Base de Datos | CRUD | Security

---

## 📌 Descripción

Este proyecto corresponde a la migración de una aplicación ecommerce previamente desarrollada con Servlets (M5) hacia Spring Boot, incorporando Spring Security para la gestión de autenticación y autorización.

Se implementa un sistema de login, registro de usuarios y control de acceso basado en roles (ADMIN y CLIENT), junto con la administración de productos mediante un CRUD protegido.

---

## 🛠 Tecnologías

☕ Java
🌱 Spring Boot
🔐 Spring Security
🗄️ MySQL
📄 Thymeleaf
🌐 HTML • CSS • JavaScript
🎨 Bootstrap 5

---

## 🔐 Funcionalidades principales

✔ Registro de usuarios (rol CLIENT por defecto)
✔ Inicio y cierre de sesión (login/logout)
✔ Autenticación mediante email y contraseña
✔ Control de acceso por roles (ADMIN / CLIENT)
✔ Protección de rutas con Spring Security
✔ CRUD de productos (solo ADMIN)
✔ Visualización de catálogo para usuarios autenticados

---

## 👥 Roles del sistema

* 👤 CLIENT:

  * Acceso a catálogo de productos
  * Navegación general

* 🛠 ADMIN:

  * Gestión completa de productos (crear, editar, eliminar)
  * Acceso a rutas protegidas `/admin/**`

---

## 📂 Estructura del proyecto

* `controller` → Controladores (Spring MVC)
* `service` → Lógica de negocio
* `repository` → Acceso a datos (JPA)
* `model` → Entidades
* `config` → Configuración de seguridad
* `templates` → Vistas Thymeleaf

---

## ▶️ Cómo ejecutar

1. Clonar repositorio:

```
git clone https://github.com/manecist/Ecommerce-CRUD-SECURITY-M6
```

2. Configurar base de datos en `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/tu_bd
spring.datasource.username=usuario
spring.datasource.password=clave
```

3. Ejecutar el proyecto desde IntelliJ o con Maven:

```
mvn spring-boot:run
```

4. Acceder en navegador:

```
http://localhost:8080
```

---

## 🔑 Acceso de prueba

* ADMIN:

  * Email: admin@magical.cl
  * Contraseña: Admin.2026!

---

## 📷 Vista del proyecto

### 🏠 Home
<img width="1919" height="1079" alt="home" src="https://github.com/user-attachments/assets/6cfc34b6-df4f-4ffa-b305-e51ea6ed2ad8" />

### 🔐 Login
<img width="816" height="1012" alt="login" src="https://github.com/user-attachments/assets/83a83adb-d054-44dd-98c2-9674d24f0439" />

### 📝 Registro
<img width="1916" height="1079" alt="registro" src="https://github.com/user-attachments/assets/bfeb04e8-2ea5-43f5-a9c5-4887c5bec8df" />

### 📦 Productos
<img width="1822" height="1079" alt="CRUD productos y home (5)" src="https://github.com/user-attachments/assets/313006fd-c2e4-4b74-86db-28025d9c3610" />
<img width="1919" height="1079" alt="CRUD productos y home (6)" src="https://github.com/user-attachments/assets/867c6521-a700-48cd-95b9-3a54f1971950" />
<img width="1067" height="982" alt="CRUD productos y home (7)" src="https://github.com/user-attachments/assets/4934599a-7535-4af9-aab3-8fb1dbc6b35f" />

### 🛠 Panel Admin
<img width="1919" height="516" alt="saludo" src="https://github.com/user-attachments/assets/e75bf129-4bbd-4097-867c-e7898d64e1a4" />

---

## ⚙️ Estado del proyecto

🚧 En desarrollo
Actualmente en proceso de migración a Spring Boot y mejora de vistas con Thymeleaf.

---

## 🚀 Mejoras futuras

* Implementación de carrito de compras
* Gestión de pedidos
* Historial de compras
* Subida y gestión de imágenes
* Validaciones avanzadas en formularios
* Mejoras en la interfaz de usuario

---
