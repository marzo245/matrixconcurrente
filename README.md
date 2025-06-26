# Simulación de Matriz Concurrente 8x8

Este proyecto implementa una simulación en Java de una matriz 8x8 donde un **Neón** intenta llegar a un **Teletransporte** mientras es perseguido por **Agentes**, todo ejecutándose de forma concurrente usando hilos y **patrones de diseño**.

## 🎮 Descripción del Juego

- **Neón (N)**: Se mueve hacia el teletransporte intentando evitar a los agentes
- **Agentes (A)**: Al menos 3 agentes que persiguen al neón para capturarlo
- **Teletransporte (T)**: El objetivo final del neón
- **Obstáculos (#)**: Bloquean el movimiento de todas las entidades
- **Espacios vacíos (.)**: Casillas por las que se pueden mover las entidades

## 🏗️ Patrones de Diseño Implementados

### Strategy Pattern
- `MovementStrategy`: Define algoritmos de movimiento intercambiables
- `ManhattanMovementStrategy`: Implementación del movimiento basado en distancia Manhattan

### Observer Pattern
- `SimulationObserver`: Interface para observar eventos de la simulación
- Notificaciones de eventos importantes (victoria, inicialización, etc.)

### Value Object Pattern
- `Position`: Clase inmutable para representar coordenadas

### Facade Pattern
- `MatrixSimulationApp`: Simplifica la interacción con el sistema completo

### Enum Pattern
- `EntityType`: Define los tipos de entidades con sus símbolos

## 🛠️ Características Técnicas

### Concurrencia
- Cada entidad se ejecuta en su propio hilo
- Sincronización usando `ReentrantLock` para evitar condiciones de carrera
- `ExecutorService` para manejo profesional de hilos
- Diferentes velocidades de movimiento para cada tipo de entidad

### Logging
- Integración con SLF4J y Logback
- Logs tanto en consola como en archivo
- Diferentes niveles de logging por paquete

### Gestión de Dependencias
- Maven para gestión de dependencias y construcción del proyecto
- Estructura estándar de proyecto Maven

## 🚀 Cómo Ejecutar

### Prerrequisitos
- Java 11 o superior
- Maven 3.6 o superior

### Compilación y Ejecución con Maven

```bash
# Limpiar y compilar el proyecto
mvn clean compile

# Ejecutar la simulación
mvn exec:java -Dexec.mainClass="com.matrix.simulation.MatrixSimulationApp"

# Alternativa: Crear JAR ejecutable
mvn clean package
java -jar target/matrix-concurrent-simulation-1.0.0.jar

# Ejecutar tests
mvn test
```

### Usando VS Code
1. Abrir el proyecto en VS Code
2. Asegurarse de tener la extensión "Extension Pack for Java" instalada
3. Usar `Ctrl+Shift+P` y buscar "Java: Compile Workspace"
4. Ejecutar desde la clase `MatrixSimulationApp`

## 🎯 Reglas del Juego

1. **Victoria del Neón**: Si el neón llega al teletransporte
2. **Victoria de los Agentes**: Si cualquier agente captura al neón
3. **Obstáculos**: Ninguna entidad puede atravesar obstáculos
4. **Movimiento**: Cada entidad se mueve un paso por turno hacia su objetivo

## 🔧 Estructura del Proyecto Maven

```
src/
├── main/
│   ├── java/
│   │   └── com/matrix/simulation/
│   │       ├── MatrixSimulationApp.java     # Aplicación principal (Facade)
│   │       ├── MatrixSimulation.java        # Lógica de simulación
│   │       ├── Position.java                # Value Object para coordenadas
│   │       ├── NeonThread.java              # Hilo del neón
│   │       ├── AgentThread.java             # Hilo de agentes
│   │       ├── SimulationObserver.java      # Interface Observer
│   │       ├── entities/
│   │       │   └── EntityType.java          # Enum de tipos de entidad
│   │       └── patterns/
│   │           ├── MovementStrategy.java    # Strategy interface
│   │           └── ManhattanMovementStrategy.java # Implementación Strategy
│   └── resources/
│       └── logback.xml                      # Configuración de logging
├── test/
│   └── java/
│       └── com/matrix/simulation/           # Tests unitarios
└── target/                                  # Archivos compilados (generados)
```

## ⚙️ Configuración

Puedes modificar los siguientes parámetros en `MatrixSimulationApp.java`:

- `neonSpeed`: Velocidad del neón (milisegundos entre movimientos)
- `agentSpeed`: Velocidad base de los agentes
- `displaySpeed`: Frecuencia de actualización de pantalla

### Dependencias Maven

El proyecto incluye:
- **SLF4J + Logback**: Para logging profesional
- **JUnit 5**: Para tests unitarios
- **Maven Shade Plugin**: Para crear JAR ejecutable

## 🎲 Características Aleatorias

- Posiciones iniciales de todas las entidades
- Distribución de obstáculos (aproximadamente 15% de la matriz)
- Cada partida es única

## 🔄 Control de la Simulación

- La simulación se ejecuta automáticamente
- Presiona **ENTER** en cualquier momento para terminar manualmente
- La simulación termina automáticamente cuando hay un ganador
- Los logs se guardan en `logs/matrix-simulation.log`

## 🧵 Detalles de Concurrencia

El proyecto demuestra varios conceptos importantes de programación concurrente:

- **Hilos múltiples**: Cada entidad tiene su propio hilo de ejecución
- **ExecutorService**: Manejo profesional del pool de hilos
- **Sincronización**: Uso de locks para acceso seguro a la matriz compartida
- **Observer Pattern**: Notificaciones asíncronas de eventos
- **Interrupción de hilos**: Manejo adecuado de interrupciones

## 📝 Comandos Maven Útiles

```bash
# Limpiar proyecto
mvn clean

# Compilar código fuente
mvn compile

# Ejecutar tests
mvn test

# Generar documentación
mvn javadoc:javadoc

# Verificar dependencias
mvn dependency:tree

# Crear JAR con dependencias
mvn clean package

# Ejecutar aplicación directamente
mvn exec:java
```

## 🔍 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=MatrixSimulationTest

# Generar reporte de cobertura
mvn jacoco:report
```

## 📝 Posibles Mejoras

- Algoritmos de pathfinding más avanzados (A*)
- Interfaz gráfica con JavaFX
- Configuración dinámica del tamaño de matriz
- Más estrategias de movimiento
- Métricas y estadísticas de rendimiento
- Tests de integración y rendimiento
- Configuración externa con archivos properties
