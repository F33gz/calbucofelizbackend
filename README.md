# Calbuco Feliz Backend

Backend de la aplicación Calbuco Feliz desarrollado con Spring Boot 3.4.5 y Java 21.

## 🚀 Características

- **Framework**: Spring Boot 3.4.5
- **Java**: 21 (OpenJDK)
- **Base de datos**: PostgreSQL
- **Seguridad**: Spring Security + JWT
- **WebSockets**: Para comunicación en tiempo real
- **APIs**: RESTful con documentación OpenAPI/Swagger
- **Containerización**: Docker y Docker Compose

## 📋 Requisitos

- Docker y Docker Compose
- Git

## 🛠️ Instalación y Ejecución

### Configuración inicial

1. **Clonar el repositorio**
   ```bash
   git clone <repo-url>
   cd CalbucoFeliz-Backend
   ```

2. **Configurar variables de entorno**
   ```bash
   # Copiar el archivo de ejemplo
   cp .env.example .env
   
   # Editar .env con tus configuraciones
   # Especialmente la configuración de base de datos
   ```

### Opción 1: Desarrollo rápido (con BD incluida)

#### En Windows (PowerShell):
```powershell
# Desarrollo con PostgreSQL incluido
.\docker-build.ps1 dev
```

#### En Linux/Mac:
```bash
# Desarrollo con PostgreSQL incluido
./docker-build.sh dev
```

### Opción 2: Producción (BD externa)

#### En Windows (PowerShell):
```powershell
# Asegúrate de configurar .env primero
.\docker-build.ps1 up
```

#### En Linux/Mac:
```bash
# Asegúrate de configurar .env primero
./docker-build.sh up
```

### Opción 3: Comandos Docker Compose directos

```bash
# Para desarrollo (con BD incluida)
docker-compose -f docker-compose.dev.yml up -d

# Para producción (BD externa)
docker-compose up -d

# Ver logs
docker-compose logs -f app
```

## 🌐 Acceso a la aplicación

Una vez iniciados los servicios:

- **API Backend**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health

### Si usas modo desarrollo (`dev`):
- **Base de datos**: localhost:5432
  - Database: `calbuco`
  - Usuario: `test`
  - Contraseña: `test`

### Si usas modo producción (`up`):
- Configura tu base de datos externa en el archivo `.env`

## ⚙️ Variables de entorno

El archivo `.env` debe contener:

```env
# Base de datos (requerido)
# Para Neon PostgreSQL (recomendado para producción):
DATABASE_URL=postgresql://usuario:password@host.neon.tech/database?sslmode=require
# Para desarrollo local:
# DATABASE_URL=jdbc:postgresql://localhost:5432/calbuco

DATABASE_USERNAME=tu_usuario
DATABASE_PASSWORD=tu_password

# JWT (requerido)
JWT_SECRET=tu_clave_secreta_muy_larga_y_segura_de_al_menos_32_caracteres
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
JWT_ISSUER=CalbucoFeliz

# Servicios externos (opcional)
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

TWILIO_ACCOUNT_SID=tu_account_sid
TWILIO_AUTH_TOKEN=tu_auth_token
TWILIO_PHONE_NUMBER=tu_phone_number
```

### 🔒 Configuración de Neon PostgreSQL

Para usar Neon como base de datos:

1. **Crea una base de datos en [Neon](https://neon.tech)**
2. **Copia la connection string completa** (incluye SSL automáticamente)
3. **Configura en `.env`**:
   ```env
   DATABASE_URL=postgresql://usuario:password@host.neon.tech/database?sslmode=require
   ```

**Nota importante**: La aplicación está configurada para manejar automáticamente:
- ✅ Conexiones SSL/TLS
- ✅ Pool de conexiones optimizado para la nube
- ✅ Timeouts apropiados para conexiones remotas
- ✅ Reconexión automática

## 📖 Comandos útiles

```bash
# Desarrollo rápido con BD incluida
.\docker-build.ps1 dev
./docker-build.sh dev

# Modo producción con BD externa
.\docker-build.ps1 up
./docker-build.sh up

# Ver logs en tiempo real
docker-compose logs -f app
# O para desarrollo
docker-compose -f docker-compose.dev.yml logs -f app

# Acceder al contenedor de la aplicación
docker-compose exec app bash

# Reconstruir completamente
.\docker-build.ps1 rebuild
./docker-build.sh rebuild

# Limpiar todo (contenedores, imágenes, volúmenes)
.\docker-build.ps1 clean
./docker-build.sh clean
```

## 🏗️ Estructura del proyecto

```
src/
├── main/
│   ├── java/cl/metspherical/calbucofelizbackend/
│   │   ├── CalbucoFelizBackendApplication.java
│   │   ├── common/          # Componentes comunes
│   │   │   ├── config/      # Configuraciones
│   │   │   ├── domain/      # Entidades de dominio
│   │   │   ├── repository/  # Repositorios
│   │   │   └── security/    # Configuración de seguridad
│   │   └── features/        # Funcionalidades
│   │       ├── auth/        # Autenticación
│   │       ├── emergency/   # Emergencias
│   │       ├── events/      # Eventos
│   │       ├── mediations/  # Mediaciones
│   │       ├── posts/       # Publicaciones
│   │       └── users/       # Usuarios
│   └── resources/
│       └── application.properties
└── test/                    # Tests
```

## 🐳 Docker

### Dockerfile mejorado
- **Multi-stage build** para optimizar tamaño
- **Usuario no-root** para seguridad
- **Configuración JVM optimizada** para contenedores
- **Healthcheck** incluido

### Docker Compose
- **Servicios**: Backend + PostgreSQL
- **Networking**: Red privada entre servicios
- **Volúmenes persistentes** para la base de datos
- **Health checks** para ambos servicios
- **Variables de entorno** configurables

## 🔧 Desarrollo

### Variables de entorno importantes

```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/calbuco
SPRING_DATASOURCE_USERNAME=test
SPRING_DATASOURCE_PASSWORD=test
```

### Profiles de Spring

- `default`: Desarrollo local
- `docker`: Ejecución en contenedores
- `test`: Para testing

## 🚨 Troubleshooting

### Problemas comunes

1. **Puerto 8080 ocupado**:
   ```bash
   # Cambiar puerto en docker-compose.yml
   ports:
     - "8081:8080"  # Usar puerto 8081 en lugar de 8080
   ```

2. **Error de conexión a base de datos**:
   ```bash
   # Verificar que PostgreSQL esté corriendo
   docker-compose logs db
   
   # Reiniciar servicios
   docker-compose restart
   ```

3. **Problemas de permisos en Windows**:
   ```powershell
   # Ejecutar PowerShell como administrador
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

## 📚 Documentación adicional

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Docker Documentation](https://docs.docker.com/)

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request