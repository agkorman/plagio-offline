# Offline — barrera física con NFC

Offline es una aplicación Android de bienestar digital pensada para reducir el uso automático o compulsivo de redes sociales y otras aplicaciones. La persona elige qué apps quiere limitar y cuánto dura cada acceso. Cuando intenta abrir una app protegida, Offline muestra una pausa y solicita acercar cualquier tarjeta, llavero o etiqueta NFC.

No se registra una tarjeta específica ni se leen o almacenan datos de pago: detectar un objeto NFC es suficiente. Después del contacto, las aplicaciones seleccionadas quedan disponibles durante el tiempo configurado (5, 10, 20 o 30 minutos). Al finalizar la sesión, Offline volverá a pedir NFC la próxima vez que detecte la apertura de una aplicación protegida.

> Offline crea una barrera de hábitos, no un bloqueo de seguridad. El usuario puede desactivar el servicio desde los ajustes de Android.

## Requisitos

### Teléfono

- Android 8.0 (API 26) o posterior.
- Sensor NFC.
- Una tarjeta, llavero o etiqueta NFC.
- Cable USB que transmita datos, no solamente carga.

Una tarjeta bancaria contactless puede usarse para probar el MVP. Offline sólo detecta el contacto NFC y no procesa información bancaria. Para el uso cotidiano se recomienda una etiqueta o llavero NTAG213, NTAG215 o NTAG216.

### Computadora

- Android Studio.
- Android SDK 35.
- JDK 17. Se recomienda seleccionar el JDK incluido con Android Studio.
- Conexión a internet durante la primera sincronización de Gradle.

## 1. Preparar el teléfono para la instalación

### Activar las opciones de desarrollador

1. Abrí **Ajustes → Acerca del teléfono → Información de software**.
2. Tocá siete veces **Número de compilación**.
3. Introducí el PIN del teléfono si Android lo solicita.
4. Volvé a Ajustes y abrí **Opciones de desarrollador**.
5. Activá **Depuración USB**.

En Samsung y otros fabricantes, los nombres o la ubicación de estos menús pueden variar ligeramente. Si el teléfono tiene un bloqueador de comandos USB o un modo de seguridad reforzada, puede ser necesario desactivarlo temporalmente durante la instalación.

### Conectar el teléfono

1. Desbloqueá el teléfono y conectalo a la computadora.
2. En la notificación de USB, elegí **Transferencia de archivos / Android Auto**.
3. Aceptá **¿Permitir depuración USB?**.
4. Opcionalmente, marcá **Permitir siempre desde esta computadora**.

El teléfono debería aparecer en Android Studio como dispositivo físico. Una advertencia indicando que el cable funciona a velocidad USB 2 no impide instalar ni probar la app.

## 2. Abrir y configurar el proyecto en Android Studio

1. Abrí Android Studio.
2. Seleccioná **Open**.
3. Elegí la carpeta raíz del proyecto `Offline`.
4. Esperá a que finalice **Gradle Sync**.
5. Si Android Studio solicita instalar Android SDK 35, aceptá.
6. Revisá **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.
7. Seleccioná **Embedded JDK 17** o cualquier instalación de JDK 17.

El proyecto incluye su Gradle Wrapper. No es necesario instalar Gradle manualmente.

## 3. Instalar la aplicación desde Android Studio

1. Verificá que la configuración de ejecución seleccionada sea `app`.
2. Elegí el teléfono en el selector de dispositivos.
3. Presioná **Run ▶**.
4. Esperá a que Android Studio compile e instale el APK.
5. Offline debería abrirse automáticamente en el teléfono.

La primera instalación puede tardar más porque Android Studio debe descargar y preparar dependencias. Las actualizaciones posteriores conservan la selección de aplicaciones y la duración configurada siempre que se instalen con el mismo `applicationId` y firma de desarrollo.

El APK de desarrollo se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 4. Permisos y configuraciones del primer inicio

Offline no solicita acceso a archivos, fotos, cámara, micrófono, ubicación, contactos ni datos bancarios. Para funcionar necesita estas dos configuraciones:

### Servicio de accesibilidad

1. Abrí Offline.
2. Si el encabezado muestra **Barrera inactiva**, tocá ese indicador.
3. Android abrirá los ajustes de Accesibilidad.
4. Entrá en **Aplicaciones instaladas** o **Servicios instalados**.
5. Seleccioná **Barreras de Offline**.
6. Activá **Usar Barreras de Offline** y aceptá la advertencia del sistema.
7. Volvé a Offline. El encabezado debería mostrar **Barrera activa**.

Este servicio permite detectar cuándo una aplicación seleccionada pasa al frente. Offline configura el servicio para observar cambios de ventana y no solicita leer el contenido de la pantalla.

### NFC

1. Activá NFC desde los ajustes rápidos o desde **Ajustes → Conexiones → NFC**.
2. No hace falta registrar una tarjeta.
3. Cuando aparezca la pantalla de pausa, acercá cualquier objeto NFC a la antena del teléfono.

El permiso técnico de NFC se concede al instalar la app y no muestra un diálogo convencional. El usuario sólo necesita mantener NFC activado.

La lista de aplicaciones instaladas tampoco presenta un diálogo de permiso: el proyecto declara visibilidad únicamente para aplicaciones que pueden abrirse desde el lanzador.

## 5. Probar el flujo completo

1. En Offline, seleccioná una aplicación para proteger.
2. Elegí una duración de 5, 10, 20 o 30 minutos.
3. Confirmá que el encabezado muestre **Barrera activa**.
4. Salí de Offline y abrí la aplicación seleccionada.
5. Cuando aparezca la pausa, acercá una tarjeta o etiqueta NFC.
6. Offline debería confirmar el contacto y abrir la aplicación protegida.
7. Durante la sesión configurada, las aplicaciones seleccionadas podrán abrirse sin repetir el contacto.
8. Una vez vencido el tiempo, el siguiente acceso detectado volverá a solicitar NFC.

En el MVP actual, si el tiempo vence mientras permanecés continuamente dentro de una aplicación, la pausa puede no aparecer hasta el siguiente cambio de ventana o hasta que vuelvas a abrirla.

## 6. Desconectar el teléfono

1. Esperá a que Android Studio termine la instalación.
2. Presioná **Stop** en Android Studio para cerrar la sesión de depuración.
3. Desconectá el cable USB. No hace falta expulsar el teléfono.

Después de instalar Offline, se pueden desactivar **Depuración USB** y las **Opciones de desarrollador**. La aplicación y la barrera NFC continuarán funcionando normalmente porque no dependen de Android Studio ni de la conexión USB.

Para volver a instalar una actualización desde Android Studio habrá que activar nuevamente las opciones de desarrollador y la depuración USB.

## 7. Instalar el APK manualmente

También se puede compartir `app-debug.apk` con una persona cercana e instalarlo directamente:

1. Transferí el APK al teléfono.
2. Abrilo desde Archivos o Descargas.
3. Android puede solicitar habilitar **Instalar aplicaciones desconocidas** para la aplicación desde la que se abrió el archivo.
4. Permití la instalación únicamente para esa aplicación y completá el proceso.
5. Después de instalar, se puede volver a desactivar ese permiso.
6. Realizá la configuración inicial de Accesibilidad y NFC descrita anteriormente.

Instalá APK únicamente cuando conozcas su origen. Una persona que instala el archivo manualmente no necesita activar el modo desarrollador.

## Solución de problemas

### El teléfono no aparece en Android Studio

Reiniciá ADB desde la terminal:

```bash
adb kill-server
adb start-server
adb devices -l
```

- `unauthorized`: falta aceptar la autorización en el teléfono.
- `offline`: desconectá el cable, reiniciá ADB y volvé a conectarlo.
- Lista vacía: probá otro cable, otro puerto USB y el modo Transferencia de archivos.

También se pueden revocar las autorizaciones desde **Opciones de desarrollador → Revocar autorizaciones de depuración USB** y volver a conectar el teléfono.

### La barrera no aparece

- Confirmá que **Barrera activa** aparezca en el encabezado.
- Revisá que la aplicación continúe seleccionada.
- Verificá que la sesión anterior no siga vigente.
- En fabricantes con administración agresiva de batería, configurá Offline como **Sin restricciones**.

### La tarjeta no se detecta

- Confirmá que NFC esté activado.
- Mantené visible la pantalla de pausa.
- Quitá fundas gruesas o magnéticas.
- Mové lentamente la tarjeta por distintas zonas de la parte posterior.
- Probá con otra tarjeta o etiqueta NFC.

## Privacidad y archivos del repositorio

El repositorio no contiene claves NFC ni datos de las tarjetas. Las preferencias de uso se almacenan localmente en el teléfono.

Aunque el repositorio se comparta sólo con amigos o conocidos, no deben versionarse:

- `local.properties`, porque contiene la ruta local del Android SDK.
- Configuración y cachés de Android Studio o Gradle.
- APK, AAB y resultados de compilación.
- Keystores, claves de firma y archivos con contraseñas.
- Archivos `.env`, secretos o credenciales de servicios externos.

El archivo `.gitignore` del proyecto excluye estos elementos. El Gradle Wrapper, incluido `gradle-wrapper.jar`, sí debe permanecer versionado para que todos utilicen la misma versión de Gradle.
