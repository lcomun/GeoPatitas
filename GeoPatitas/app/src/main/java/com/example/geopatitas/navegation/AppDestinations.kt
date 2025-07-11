package com.example.geopatitas.navegation


object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val CIUDADANO_DASHBOARD_ROUTE = "ciudadano_dashboard"
    const val ORGANIZACION_DASHBOARD_ROUTE = "organizacion_dashboard"

    // Para rutas con argumentos, define la plantilla de la ruta
    const val CREAR_CUENTA_BASE_ROUTE = "crear_cuenta"
    const val CREAR_CUENTA_ROUTE = "$CREAR_CUENTA_BASE_ROUTE?nombre={nombre}&correo={correo}"

    object VecinoFlow {
        const val ROOT = "ciudadano_flow"
        const val DASHBOARD = "ciudadano_dashboard_screen"
    }

    object AliadoFlow {
        const val ROOT = "aliado_flow"
        const val DASHBOARD = "aliado_dashboard_screen"
    }

    // Puedes tener objetos similares para otros flujos (Ej. OrganizacionFlow, AuthFlow)
    object AuthFlow {
        const val ROOT = "auth_flow"
        const val LOGIN = "login_screen" // Si tuvieras un sub-grafo para login
        const val SIGN_UP = "signup_screen"
    }

    // Ruta para la pantalla de carga inicial
    const val LOADING_ROUTE = "loading"
}