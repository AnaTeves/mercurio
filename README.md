### Sistema de Gestión de Ventas

Aplicación de escritorio para la gestión de ventas, desarrollada en **Java** con interfaz gráfica en **JavaFX**. El sistema está diseñado utilizando el patrón de arquitectura **MVC (Modelo-Vista-Controlador)**, garantizando un código limpio, escalable y fácil de mantener.

## Sobre el proyecto

Este proyecto surgió como una solución integral para administrar el ciclo comercial de un negocio. No solo incluye el desarrollo del software, sino que abarca todo el ciclo de vida de la ingeniería de software: desde la especificacion de requerimientos hasta el modelado de la base de datos y la documentación final.

### Perfiles de usuario
El sistema cuenta con control de acceso basado en roles (RBAC):
*   **Administrador:** Acceso total al sistema. Gestión de usuarios, configuración global, reportes históricos y control absoluto de la base de datos.
*   **Gerente:** Supervisión del negocio. Acceso a reportes de ventas, estadísticas de rendimiento, gestión de inventario y supervisión de vendedores.
*   **Vendedor:** Perfil operativo. Orientado a la carga rápida de ventas, consulta de stock y facturación diaria.

## Características 

*   **Arquitectura MVC:** Separación clara entre la lógica de negocio, la interfaz de usuario y el acceso a datos.
*   **Capa de Servicios:** Implementación de controladores de servicios para intermediar y optimizar las consultas entre la aplicación y la base de datos.
*   **Interfaz Gráfica (UI):** Vistas desarrolladas utilizando archivos `.fxml`, separando el diseño del comportamiento.
*   **Base de Datos Relacional:** Diseño normalizado y gestionado a través de SQL Server Management Studio (SSMS).

## Documentación adjunta

Este repositorio incluye la documentación completa de la ingeniería del proyecto en la carpeta `/docs`:
- Especificación de Requerimientos (SRS)
- Modelado de la base de datos
- Manual de Usuario

## Capturas de pantalla del sistema


| Auditoria del sistema | Dashboard de Ventas | Formulario de registro |
|:---:|:---:|
| ![Auditoria](auditoriaSistema.png) | ![Formulario de registro](formRegistro.png) | ![Registro de venta](registroVenta.png) |