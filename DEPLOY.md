# Despliegue: Render + Supabase + Vercel

## 1. Base de datos en Supabase

En Supabase crea un proyecto y copia los datos de conexion PostgreSQL.

Para Render usa el formato JDBC:

```text
DATABASE_URL=jdbc:postgresql://HOST:PUERTO/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=TU_PASSWORD_DE_SUPABASE
```

Si usas el pooler de Supabase, conserva `?sslmode=require`.

## 2. Backend Java en Render con Docker

El repo ya incluye `render.yaml` y `clublago/Dockerfile`. En Render crea un Blueprint desde el repositorio.

Si lo haces manual desde la interfaz de Render:

```text
Service Type: Web Service
Runtime: Docker
Root Directory: clublago
Dockerfile Path: ./Dockerfile
```

Variables que debes configurar en el servicio `lagoassist-api`:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://HOST:PUERTO/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=TU_PASSWORD_DE_SUPABASE
APP_CORS_ALLOWED_ORIGINS=https://TU_FRONTEND.vercel.app
APP_SEED_ENABLED=false
```

El Dockerfile compila el backend y corre:

```bash
java -jar app.jar
```

Cuando Render termine, tu API quedara parecida a:

```text
https://lagoassist-api.onrender.com/api
```

Prueba:

```text
https://lagoassist-api.onrender.com/api/disciplines
```

## 3. Frontend Angular en Vercel

El frontend esta en la carpeta `LagoAssist`.

Configura en Vercel:

```text
Root Directory: LagoAssist
Build Command: npm run build
Output Directory: dist/LagoAssist/browser
```

El archivo `src/environments/environment.prod.ts` apunta a:

```text
https://lagoassist-api.onrender.com/api
```

Si Render genera otro dominio, cambia ese valor antes de desplegar el front.

## 4. Desarrollo local

Local sigue usando H2:

```bash
cd clublago
./mvnw spring-boot:run
```

Frontend local:

```bash
cd LagoAssist
npm start
```
