// Top-level build file donde puedes agregar opciones de configuración comunes a todos los sub-proyectos/módulos.
plugins {
    alias(libs.plugins.android.application) apply false // Solo en el módulo correspondiente
    id("com.google.gms.google-services") version "4.4.4" apply false // Solo en el módulo correspondiente
}

