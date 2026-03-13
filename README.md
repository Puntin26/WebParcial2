# Sistema de Gestión y Control de Eventos Académicos

Proyecto completo para parcial usando **Javalin 7**, **Hibernate ORM**, **H2 en modo servidor**, **HTML/CSS/JS**, **QR**, **Chart.js**, **Docker multi-stage** y **docker-compose** para despliegue con proxy SSL.

## Credenciales iniciales
- Admin creado automáticamente por script de inicialización:
  - correo: `admin@universidad.edu`
  - contraseña: `admin123`

## Ejecutar local
1. Iniciar H2 en modo servidor (opcional si ya tienes uno):
```bash
./scripts/init-h2-server.sh
```
2. Ejecutar app:
```bash
mvn clean package
java -jar target/eventos-academicos-1.0.0-jar-with-dependencies.jar
```
3. Abrir: `http://localhost:7000`

## Funcionalidades implementadas
- Roles: ADMIN, ORGANIZADOR, PARTICIPANTE.
- Login por sesión.
- CRUD de eventos con publicación/cancelación por roles.
- Vista eventos tipo lista/grid responsiva.
- Inscripción/cancelación con validación de cupo y duplicidad.
- QR único por inscripción + validación de asistencia sin doble marcado.
- Resumen por evento con indicadores y gráficas.
- Panel admin para usuarios y eventos.
- Diagrama de clases en `docs/class-diagram.md`.

## Docker
```bash
docker compose up --build -d
```

### SSL / TLS
Configurar dominio real en `nginx/default.conf` y generar certificado:
```bash
docker compose run --rm certbot certonly --webroot -w /var/www/certbot -d tu-dominio --email tu-correo@dominio.com --agree-tos --no-eff-email
```

