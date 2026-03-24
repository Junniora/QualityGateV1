package com.example.qualitygate.data.util

import com.example.qualitygate.data.model.ProductClassification

object MilestoneTemplates {
    val NEW_PRODUCT_MILESTONES = listOf(
        "Liberación de Dibujo Técnico",
        "Revisión de Factibilidad",
        "Diseño de Herramental",
        "Aprobación de Proveedores de Materia Prima",
        "Recepción de Materia Prima para Prototipos",
        "Fabricación de Prototipos A",
        "Pruebas de Validación Interna A",
        "Liberación de Diseño de Empaque",
        "Configuración de Línea de Producción",
        "Entrenamiento de Operadores Nivel 1",
        "Fabricación de Prototipos B",
        "Pruebas de Validación Interna B",
        "Envío de Muestras a Cliente",
        "Aprobación de Prototipos por Cliente",
        "Diseño de Estaciones de Inspección",
        "Calibración de Equipos de Medición",
        "Análisis de Modo y Efecto de Falla (AMEF)",
        "Plan de Control Inicial",
        "Validación de Software de Prueba",
        "Revisión de Seguridad e Higiene",
        "Fabricación de Lote Pre-Serie",
        "Estudio de Capacidad de Proceso (Cpk)",
        "Estudio de R&R de Calibres",
        "Validación de Tiempos de Ciclo",
        "Auditoría de Capas de Calidad",
        "Liberación de Instrucciones de Trabajo",
        "Pruebas de Durabilidad",
        "Pruebas de Choque Térmico",
        "Inspección de Primer Artículo (FAI)",
        "Preparación de Documentación PPAP",
        "Envío de PPAP a Cliente",
        "Aprobación de PPAP por Cliente",
        "Revisión de Inventario de Seguridad",
        "Entrenamiento de Operadores Nivel 2",
        "Simulación de Logística y Embarque",
        "Revisión de Criterios de Defectos",
        "Instalación de Pokayokes",
        "Validación de Sistemas de Visión",
        "Run @ Rate (Prueba de Capacidad)",
        "Revisión de Costos Finales",
        "Aprobación de Lanzamiento por Calidad",
        "Aprobación de Lanzamiento por Producción",
        "Reunión de Cierre de Proyecto",
        "Inicio de Producción Masiva (SOP)",
        "Monitoreo Post-Lanzamiento (30 días)"
    )

    val TRANSFER_PRODUCT_MILESTONES = listOf(
        "Notificación de Transferencia",
        "Revisión de Inventario en Planta Origen",
        "Desmontaje de Maquinaria",
        "Logística de Transporte de Activos",
        "Recepción de Maquinaria en Planta Destino",
        "Instalación Eléctrica y Neumática",
        "Nivelación y Anclaje de Máquinas",
        "Revisión de Herramentales Existentes",
        "Actualización de Dibujos Técnicos",
        "Validación de Proveedores Locales",
        "Capacitación de Personal en Planta Destino",
        "Configuración de Layout de Producción",
        "Pruebas de Conectividad de Red",
        "Carga de Software de Control",
        "Primeras Pruebas de Funcionamiento",
        "Ajuste de Parámetros de Proceso",
        "Fabricación de Muestras de Validación",
        "Inspección Dimensional de Muestras",
        "Pruebas de Desempeño Funcional",
        "Revisión de AMEF Adaptado",
        "Actualización de Plan de Control",
        "Validación de Empaque Local",
        "Estudio de R&R Local",
        "Estudio de Capacidad Local (Cpk)",
        "Re-Certificación de Operadores",
        "Inspección de Seguridad Local",
        "Preparación de Paquete de Re-sumisión PPAP",
        "Aprobación de Cliente para Transferencia",
        "Validación de Tiempos de Ciclo Locales",
        "Auditoría de Proceso Interna",
        "Revisión de Mantenimiento Preventivo",
        "Lote de Prueba de Alta Velocidad",
        "Verificación de Pokayokes",
        "Liberación por Ingeniería de Calidad",
        "Aprobación de Gerencia de Planta",
        "Inicio de Producción en Nueva Sede",
        "Monitoreo de Calidad (Fase Estabilización)",
        "Cierre Administrativo de Transferencia"
    )

    fun getMilestonesFor(classification: ProductClassification): List<String> {
        return when (classification) {
            ProductClassification.NUEVO_PRODUCTO -> NEW_PRODUCT_MILESTONES
            ProductClassification.TRANSFERENCIA_PRODUCTO -> TRANSFER_PRODUCT_MILESTONES
        }
    }
}
