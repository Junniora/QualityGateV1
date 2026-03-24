# QualityGate (QA PMS)

**QualityGate** es un Sistema de Gestión de Calidad (Product Management System) diseñado específicamente para el seguimiento y validación de productos en entornos industriales, como la fabricación de clusters automotrices.

La aplicación digitaliza el proceso de aseguramiento de calidad, permitiendo una trazabilidad completa desde el registro inicial de un producto hasta su aprobación final para producción masiva.

## 🚀 Propósito del Proyecto
El objetivo principal es eliminar el seguimiento manual y disperso de las actividades de validación (milestones). QualityGate asegura que cada etapa del proceso de calidad se cumpla bajo los estándares requeridos, permitiendo la colaboración en tiempo real entre supervisores, revisores y aprobadores.

## 👥 Roles y Flujo de Trabajo
La aplicación utiliza un sistema de control de acceso basado en roles (RBAC) gestionado a través de Firebase:

1.  **Supervisor (Producción/Calidad):**
    *   Registra nuevos productos con evidencias fotográficas (4 ángulos obligatorios).
    *   Realiza la planeación de fechas (Milestones).
    *   Registra las fechas reales de ejecución durante la etapa "On Going".
2.  **Revisor (Ingeniería de Calidad):**
    *   Realiza la "Pre-Revisión" de la planeación.
    *   Ejecuta la "Revisión Final" técnica una vez completadas las actividades.
    *   Provee feedback constructivo en caso de rechazo.
3.  **Aprobador (Gerencia de Planta/Calidad):**
    *   Otorga la validación final para el cierre del proyecto y liberación de producción.

## 🛠️ Funcionalidades Principales
*   **Gestión de Milestones Automática:** Generación inteligente de 45 actividades para "Nuevos Productos" y 38 para "Transferencias".
*   **Evidencia Fotográfica:** Integración con la cámara del dispositivo y almacenamiento en la nube (Firebase Storage).
*   **Ciclo de Vida del Producto:** Estados dinámicos: *Planning ➔ Pre-Revision ➔ On Going ➔ Final Revision ➔ Aprobación Final ➔ Completed*.
*   **Sistema de Feedback:** Registro histórico de comentarios y motivos de rechazo para mejora continua.
*   **Dashboard Inteligente:** Interfaz personalizada según el rol del usuario logueado.

## 🏗️ Arquitectura Técnica
*   **Lenguaje:** Kotlin
*   **UI:** Jetpack Compose (Diseño Moderno y Reactivo)
*   **Arquitectura:** MVVM (Model-View-ViewModel)
*   **Backend:** Firebase
    *   *Authentication:* Registro e inicio de sesión seguro.
    *   *Firestore:* Base de datos NoSQL en tiempo real para productos y hitos.
    *   *Storage:* Almacenamiento de imágenes de alta resolución.
*   **Navegación:** Jetpack Navigation Component.

## 📸 Capturas de Pantalla (Estructura de UI)
1.  **Login:** Acceso seguro con validación de correo.
2.  **Dashboard (QualityGate):** Acceso rápido a las tareas críticas del rol.
3.  **Registro:** Formulario validado con captura de 4 fotos.
4.  **Detalle de Producto:** Vista interactiva de hitos y cronograma.

---
*Desarrollado para la optimización de procesos de calidad industrial.*
