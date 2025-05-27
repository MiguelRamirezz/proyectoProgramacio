# Tienda Online - Backend

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.0-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📋 Descripción

Backend de una tienda online desarrollada con Spring Boot que incluye:
- Gestión de productos
- Carrito de compras
- Proceso de pago
- Autenticación y autorización con JWT
- Panel de administración
- Documentación con OpenAPI/Swagger

## 🚀 Características

- **Autenticación JWT**
- **API RESTful**
- **Documentación con Swagger**
- **Validación de datos**
- **Manejo de errores global**
- **Seguridad con Spring Security**
- **Base de datos relacional (MySQL/PostgreSQL)**
- **Paginación y ordenación**
- **Búsqueda y filtrado**

## 🛠️ Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- Base de datos MySQL/PostgreSQL
- IDE de tu preferencia (IntelliJ IDEA, Eclipse, VS Code)

## 🔧 Configuración

1. **Clonar el repositorio**
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd proyectoProgramacion
   ```

2. **Configurar la base de datos**
   - Crear una base de datos MySQL/PostgreSQL
   - Configurar las credenciales en `application.properties`

3. **Variables de entorno**
   Copiar el archivo `.env.example` a `.env` y configurar las variables necesarias:
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/tienda_online
   SPRING_DATASOURCE_USERNAME=usuario
   SPRING_DATASOURCE_PASSWORD=contraseña
   JWT_SECRET=tu_clave_secreta_muy_larga_y_segura
   JWT_EXPIRATION=86400000
   ```

4. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

## 📚 Documentación de la API

La documentación de la API está disponible en:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/example/proyectoProgramacion/
│   │   ├── config/          # Configuraciones de la aplicación
│   │   ├── controller/      # Controladores REST
│   │   ├── model/           # Entidades y DTOs
│   │   ├── repository/      # Repositorios de datos
│   │   ├── security/        # Configuración de seguridad
│   │   ├── service/         # Lógica de negocio
│   │   └── util/            # Utilidades y constantes
│   └── resources/
│       ├── static/         # Archivos estáticos
│       ├── templates/       # Plantillas Thymeleaf
│       └── application.properties
└── test/                    # Pruebas unitarias e integración
```

## 🔒 Seguridad

La aplicación utiliza Spring Security con JWT para la autenticación. Los endpoints están protegidos según los siguientes roles:

- **/api/auth/** - Público
- **/api/public/** - Público
- **/api/user/** - Requiere autenticación
- **/api/admin/** - Requiere rol ADMIN

## 📦 Dependencias Principales

- **Spring Boot Starter Web** - Para aplicaciones web
- **Spring Data JPA** - Para el acceso a datos
- **Spring Security** - Para autenticación y autorización
- **Spring Validation** - Para validación de datos
- **Lombok** - Para reducir código boilerplate
- **MapStruct** - Para mapeo entre entidades y DTOs
- **SpringDoc OpenAPI** - Para documentación de la API
- **MySQL Connector** - Controlador de base de datos
- **JWT** - Para autenticación basada en tokens

## 🧪 Pruebas

Para ejecutar las pruebas:

```bash
mvn test
```

## 🚀 Despliegue

### Perfiles de Spring

La aplicación soporta múltiples perfiles:
- **dev** - Desarrollo local
- **test** - Entorno de pruebas
- **prod** - Producción

Ejecutar con un perfil específico:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

Construir la imagen:

```bash
docker build -t tienda-online-backend .
```

Ejecutar el contenedor:

```bash
docker run -p 8080:8080 tienda-online-backend
```

## 🤝 Contribución

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Haz commit de tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Haz push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## ✉️ Contacto

   Miguel Ramirez - [@tucuenta](https://twitter.com/tucuenta) - u20232217205@usco.edu.co

Enlace del Proyecto: [https://github.com/tucuenta/proyecto-tienda-online](https://github.com/tucuenta/proyecto-tienda-online)

---

<div align="center">
  <sub>Creado con ❤️ por Miguel Ramirez</sub>
</div>
