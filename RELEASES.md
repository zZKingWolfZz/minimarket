# 📦 Guía de Compilación y Lanzamientos (Releases) - MiniMarket

Esta guía describe cómo generar y publicar versiones oficiales de **MiniMarket Yuly**, incluyendo la versión portable y la versión con instalador.

---

## 🛠️ Archivos del Lanzamiento

Cada versión oficial publicada en GitHub contiene dos archivos clave:

1.  **`MiniMarket.exe` (Versión Portable)**:
    -   Es un ejecutable único que contiene todas las dependencias y recursos de la aplicación.
    -   No requiere instalación; se puede llevar en un USB o ejecutar directamente.
    -   Guarda la configuración de base de datos en la carpeta del usuario (`~/.minimarket/database.properties`).

2.  **`MiniMarket_Setup.exe` (Instalador)**:
    -   Es un instalador de Windows estándar creado con **Inno Setup 6**.
    -   Crea los accesos directos necesarios (Escritorio, Menú de Inicio) apuntando al directorio de instalación correcto.
    -   Previene problemas de escritura de archivos temporales estableciendo el directorio de trabajo predeterminado en `{app}`.

---

## 🔨 Cómo Compilar Desde Cero

### Paso 1: Compilar la Versión Portable (.exe)
Para empaquetar el código Java y las dependencias en un ejecutable Windows, ejecuta en la terminal del proyecto:

```bash
mvn clean package -DskipTests
```

Esto generará el archivo ejecutable portable en:
-   📂 `target/MiniMarket.exe`

### Paso 2: Compilar el Instalador (.exe)
Para crear el instalador de Windows, asegúrate de tener **Inno Setup 6** instalado. Luego ejecuta:

```bash
# Si ISCC está en tu PATH de Windows
ISCC minimarket.iss

# Si no está en el PATH, puedes llamar a la ruta local por defecto de Inno Setup:
& "C:\Users\arnie\AppData\Local\Programs\Inno Setup 6\ISCC.exe" minimarket.iss
```

Esto generará el ejecutable del instalador en:
-   📂 `target/MiniMarket_Setup.exe`

---

## 🚀 Cómo Publicar un Release en GitHub

Los ejecutables se asocian a un lanzamiento (Release) oficial en GitHub. Puedes subirlos de dos maneras:

### Opción A: Automatizado mediante PowerShell (Recomendado)
El proyecto contiene un script en la carpeta de caché del asistente diseñado para conectarse a GitHub con tus credenciales y subir automáticamente los archivos a la versión `v1.0.0`:

```powershell
powershell -File "C:\Users\arnie\.gemini\antigravity-ide\brain\2083d54f-db3c-4e3c-bdd6-b0cf30613495\scratch\create_release.ps1"
```

### Opción B: Manual desde la Web de GitHub
1.  Ve a tu repositorio en GitHub: [zZKingWolfZz/minimarket](https://github.com/zZKingWolfZz/minimarket).
2.  En el menú lateral derecho, haz clic en **Releases** (o ve a `/releases`).
3.  Haz clic en **Draft a new release** (o edita la release `v1.0.0` existente).
4.  Escribe el tag de la versión (ej. `v1.0.0`) y un título descriptivo.
5.  En la sección inferior **"Attach binaries by dropping them here..."**, arrastra y suelta los dos archivos:
    -   `target/MiniMarket.exe`
    -   `target/MiniMarket_Setup.exe`
6.  Haz clic en **Publish release**.
