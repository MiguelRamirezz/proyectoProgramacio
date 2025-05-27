# Documentación de la API de Tienda en Línea

Bienvenido a la documentación de la API de Tienda en Línea. Este documento te guiará a través de todos los endpoints disponibles.

## Tabla de Contenidos
1. [Configuración Inicial](#configuración-inicial)
2. [Autenticación](#autenticación)
3. [Usuarios](#usuarios)
4. [Productos](#productos)
5. [Pagos](#pagos)
6. [Variables de Entorno](#variables-de-entorno)
7. [Ejemplos de Uso](#ejemplos-de-uso)

## Configuración Inicial

1. **Importar la colección**:
   - Abre Postman
   - Haz clic en "Import" > "File"
   - Selecciona el archivo `postman_collection.json`

2. **Configurar entorno**:
   - Crea un nuevo entorno en Postman
   - Añade estas variables:
     - `baseUrl`: `http://localhost:8090`
     - `authToken`: (se actualizará automáticamente)
     - `userId`: (se actualizará automáticamente)
     - `productId`: (se actualizará automáticamente)
     - `orderId`: (se actualizará automáticamente)

## Autenticación

### 1. Registro de Usuario
- **Método**: POST
- **URL**: `/api/auth/register`
- **Cuerpo**:
  ```json
  {
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan.perez@ejemplo.com",
    "username": "juanperez",
    "password": "Contraseña123!"
  }
  ```

### 2. Inicio de Sesión
- **Método**: POST
- **URL**: `/api/auth/login`
- **Cuerpo**:
  ```json
  {
    "username": "juan.perez@ejemplo.com",
    "password": "Contraseña123!"
  }
  ```
- **Respuesta**: Incluye el token JWT para autenticación

## Usuarios

### 1. Obtener Todos los Usuarios
- **Método**: GET
- **URL**: `/api/users`
- **Parámetros**:
  - `page`: Número de página (0-indexado)
  - `size`: Tamaño de la página
  - `sort`: Campo de ordenación (ej: `nombre,asc`)

### 2. Obtener Usuario por Username
- **Método**: GET
- **URL**: `/api/users/{username}`

### 3. Actualizar Usuario
- **Método**: PUT
- **URL**: `/api/users/{username}`
- **Cuerpo**: Datos actualizados del usuario

### 4. Eliminar Usuario
- **Método**: DELETE
- **URL**: `/api/users/{username}`

## Productos

### 1. Obtener Todos los Productos
- **Método**: GET
- **URL**: `/api/productos`
- **Parámetros**:
  - `page`: Número de página
  - `size`: Tamaño de la página
  - `sort`: Campo de ordenación
  - `categoria`: Filtrar por categoría
  - `precioMin`: Precio mínimo
  - `precioMax`: Precio máximo

### 2. Obtener Producto por ID
- **Método**: GET
- **URL**: `/api/productos/{id}`

### 3. Crear Producto (Admin)
- **Método**: POST
- **URL**: `/api/admin/productos`
- **Cuerpo**:
  ```json
  {
    "nombre": "Nuevo Producto",
    "descripcion": "Descripción detallada",
    "precio": 999.99,
    "stock": 50,
    "categoria": "ELECTRONICA"
  }
  ```

## Pagos

### 1. Procesar Pago
- **Método**: POST
- **URL**: `/api/pagos`
- **Cuerpo**:
  ```json
  {
    "ordenId": 1,
    "metodoPago": "TARJETA_CREDITO",
    "monto": 199.98,
    "detallesTarjeta": {
      "numeroTarjeta": "4111111111111111",
      "nombreTitular": "Juan Pérez",
      "fechaExpiracion": "12/25",
      "cvv": "123"
    }
  }
  ```

## Variables de Entorno

La colección utiliza las siguientes variables:

| Variable    | Descripción                                  | Ejemplo                |
|-------------|---------------------------------------------|------------------------|
| baseUrl     | URL base de la API                          | http://localhost:8080  |
| authToken   | Token JWT para autenticación                | (se actualiza solo)    |
| userId      | ID del usuario actual                       | (se actualiza solo)    |
| productId   | ID de producto para pruebas                 | (se actualiza solo)    |
| orderId     | ID de orden para pruebas                    | (se actualiza solo)    |

## Ejemplos de Uso

### Flujo Típico de Uso

1. **Registrar un nuevo usuario**:
   ```http
   POST /api/auth/register
   ```

2. **Iniciar sesión**:
   ```http
   POST /api/auth/login
   ```
   Guarda el token JWT devuelto.

3. **Obtener todos los productos**:
   ```http
   GET /api/productos
   ```

4. **Crear un nuevo producto (admin)**:
   ```http
   POST /api/admin/productos
   Authorization: Bearer <token>
   ```

5. **Realizar un pago**:
   ```http
   POST /api/pagos
   Authorization: Bearer <token>
   ```

## Errores Comunes

| Código | Descripción                     | Solución                              |
|--------|---------------------------------|---------------------------------------|
| 401    | No autorizado                   | Verifica el token JWT                 |
| 403    | Prohibido                      | Verifica los permisos del usuario     |
| 404    | Recurso no encontrado          | Verifica el ID del recurso            |
| 400    | Solicitud incorrecta           | Verifica el cuerpo de la solicitud    |
| 500    | Error del servidor             | Revisa los logs del servidor          |
