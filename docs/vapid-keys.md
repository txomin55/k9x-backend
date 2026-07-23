# Generar un nuevo par de claves VAPID

Las notificaciones Web Push se firman con un par de claves **VAPID** (pública + privada) que
identifica a este servidor ante el push service (FCM, Mozilla, etc.). Esta guía explica cómo generar
un par nuevo y dónde va cada clave.

> ⚠️ **La clave privada es un secreto.** Nunca debe estar en el frontend, en un `.env` commiteado ni
> en el repositorio. Solo vive en la configuración del backend (variable de entorno / secret).

---

## 1. Generar el par

Elige una de las opciones. Todas producen una clave **pública** y una **privada** en base64url
(las que espera la librería `web-push` del backend).

### Opción A — `web-push` (Node, la más simple)

```bash
npx web-push generate-vapid-keys
```

Salida:

```
=======================================

Public Key:
BQ...   ← clave pública

Private Key:
xy...   ← clave privada

=======================================
```

### Opción B — sin instalar nada (OpenSSL)

```bash
# clave privada (curve P-256)
openssl ecparam -genkey -name prime256v1 -noout -out vapid_private.pem

# clave privada en base64url (para VAPID_PRIVATE_KEY)
openssl ec -in vapid_private.pem -outform DER 2>/dev/null | tail -c +8 | head -c 32 | base64 | tr '+/' '-_' | tr -d '='

# clave pública en base64url (para VAPID_PUBLIC_KEY)
openssl ec -in vapid_private.pem -pubout -outform DER 2>/dev/null | tail -c 65 | base64 | tr '+/' '-_' | tr -d '='
```

Borra el `.pem` cuando termines si no lo necesitas.

---

## 2. Dónde va cada clave

| Clave | Variable | Dónde | Secreto |
|---|---|---|---|
| Pública | `VITE_VAPID_PUBLIC_KEY` | Frontend (`ui/app/.env.*`) | No |
| Pública | `VAPID_PUBLIC_KEY` | Backend | No |
| Privada | `VAPID_PRIVATE_KEY` | Backend (secret) | **Sí** |
| — | `VAPID_SUBJECT` | Backend | No — `mailto:tu@correo` o una URL |

**La clave pública del frontend y la del backend deben ser la misma.** Si no coinciden, el navegador
se suscribe con una clave y el backend firma con otra → el push service rechaza el envío.

### Backend (Render → Environment)

```
VAPID_PUBLIC_KEY=BQ...
VAPID_PRIVATE_KEY=xy...
VAPID_SUBJECT=mailto:txomin.sirera@clarity.ai
```

Si `VAPID_PUBLIC_KEY` o `VAPID_PRIVATE_KEY` están vacías, el backend arranca igual pero **no envía**
push (usa un adapter no-op).

### Frontend (`ui/app/.env.integrated`, `.env.staging`, `.env.production`)

```
VITE_VAPID_PUBLIC_KEY=BQ...
```

---

## 3. Rotar (invalidar el par anterior)

Rotar es simplemente **generar un par nuevo y reemplazar las tres variables** (pública en front + back,
privada en back). Hay que hacerlo si la clave privada se filtra alguna vez.

Consecuencia: las suscripciones ya guardadas se firmaron pensando en la clave antigua, así que tras
rotar **dejan de recibir push**. Cada navegador se re-suscribe automáticamente la próxima vez que el
usuario entra logueado (el flujo de `NotificationsInit`), y el backend hace *upsert* de la nueva
suscripción sobre el mismo `endpoint`. Las que queden obsoletas se purgan solas al primer intento de
envío fallido (410/404 Gone).
