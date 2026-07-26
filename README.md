# Offline — MVP de barrera NFC

Una app Android de bienestar digital: al abrir una aplicación seleccionada, muestra una pausa y pide acercar cualquier tarjeta, llavero o etiqueta NFC. No registra la tarjeta ni lee datos de pago; detectar una etiqueta es suficiente para iniciar una sesión temporal.

## Flujo

1. Abrí **Offline** y elegí las aplicaciones que querés pausar.
2. Tocá **Activar barrera** y habilitá el servicio de accesibilidad de Offline en Ajustes.
3. Al abrir una aplicación protegida, acercá cualquier objeto NFC cuando aparezca la pantalla de pausa.
4. La sesión queda desbloqueada durante 10 minutos.

## Para probar NFC

- Una tarjeta bancaria contactless puede servir para una prueba puntual, pero no se guarda ni procesa ningún dato de ella.
- Para el uso cotidiano, conviene una etiqueta NTAG213/215/216, tarjeta o llavero NFC económico.
- El teléfono debe tener NFC y tenerlo activado.

## Límites intencionales del MVP

- Es una barrera de hábitos, no un sistema de seguridad: la persona puede desactivar el servicio en Ajustes.
- La lectura NFC ocurre sólo cuando la pantalla de pausa está en primer plano.
- La detección de la aplicación abierta depende del servicio de accesibilidad, que la persona debe habilitar explícitamente.

## Desarrollo

El proyecto usa Kotlin y el plugin Android Gradle. Abrilo con Android Studio y ejecutalo en un teléfono físico con NFC (un emulador no permite probar el toque NFC).
