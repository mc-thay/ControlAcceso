# Sistema de Control de Acceso para Docentes y Administradores

Este proyecto es una solución digital diseñada para gestionar el acceso a aulas y laboratorios en la UNS. Desarrollado utilizando Kotlin en Android Studio, Firebase Authentication y Firestore, permite a los docentes realizar solicitudes para el uso de espacios y a los administradores gestionar dichas solicitudes.

---

## 🚀 Funcionalidades Principales

### Docentes
- **Ingreso al Aula**: Los docentes pueden enviar solicitudes para usar un aula o laboratorio.  
- **Seguimiento de Solicitudes**: Visualización del estado de las solicitudes enviadas.  
- **Salida del Aula**: Función para registrar el fin de uso del aula, actualizando automáticamente el estado de la solicitud.

### Administradores
- **Gestión de Solicitudes**: Visualización de todas las solicitudes enviadas por los docentes.  
- **Control de Acceso**: Capacidad de aceptar o rechazar solicitudes.  
- **Historial**: Seguimiento de solicitudes pasadas para fines administrativos.

---

## 🛠️ Tecnologías Utilizadas
- **Lenguaje**: [Kotlin](https://kotlinlang.org/)  
- **Base de Datos**: [Firebase Firestore](https://firebase.google.com/products/firestore)  
- **Autenticación**: [Firebase Authentication](https://firebase.google.com/products/auth)  
- **UI/UX**: Diseño adaptable con [Material Design](https://material.io/) y RecyclerView para la presentación de datos.

---

## 📂 Estructura del Proyecto
1. **Login**: Sistema de autenticación por correo electrónico para docentes y administradores.
2. **Gestión de Roles**: Navegación a secciones específicas según el rol asignado en la base de datos.
3. **Solicitudes**: Modelo de datos que incluye información sobre:
   - **Solicitante**: Nombre y correo del usuario que realiza la solicitud.
   - **Fecha y Hora**: Momento de la solicitud.
   - **Laboratorio/Aula**: Espacio asignado de forma aleatoria.
   - **Estado**: PENDIENTE, ACEPTADA, RECHAZADA o TERMINADA.
   - **Hora de Salida**: Campo inicialmente nulo que se actualiza al finalizar el uso del espacio.
4. **Gestión de Estado**: Los administradores pueden modificar el estado de una solicitud según las necesidades.

---

DESCARGA NUESTRA APP escaneando el QR:
![image](https://github.com/user-attachments/assets/8d3e990d-2d70-4a1f-bf04-676e148e26f3)




## 📑 Cómo Empezar
1. Clona este repositorio:
   ```bash
   git clone https://github.com/mc-thay/ControlAcceso
