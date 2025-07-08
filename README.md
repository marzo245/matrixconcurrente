# Simulación de Matriz Concurrente 8x8

Este proyecto implementa una simulación en Java de una matriz 8x8 donde un **Neón** intenta llegar a un **Teletransporte** mientras es perseguido por **Agentes**, todo ejecutándose de forma concurrente usando hilos y **patrones de diseño**.

## 🎮 Lógica del Juego

### Entidades del Juego
- **Neón (N)**: Entidad principal que intenta llegar al teletransporte
- **Agentes (A)**: Al menos 3 agentes que persiguen al neón para capturarlo
- **Teletransporte (T)**: El objetivo final del neón
- **Obstáculos (#)**: Bloquean el movimiento de todas las entidades
- **Espacios vacíos (.)**: Casillas por las que se pueden mover las entidades

### Condiciones de Victoria
1. **Victoria del Neón**: Si el neón llega al teletransporte
2. **Victoria de los Agentes**: Si cualquier agente captura al neón

### Reglas de Movimiento
- Cada entidad se mueve un paso por turno hacia su objetivo
- No pueden atravesar obstáculos
- Los agentes no pueden ocupar la misma casilla
- El neón evita colisionar con los agentes

## 🧵 Arquitectura Concurrente

### Hilos del Sistema

#### 1. **NeonThread** - Hilo del Neón
```java
public class NeonThread extends Thread {
    private final MatrixSimulation simulation;
    private final int moveDelay; // 800ms por defecto
}
```
- **Función**: Controla el movimiento del neón hacia el teletransporte
- **Frecuencia**: Se mueve cada 800ms
- **Estrategia**: Usa algoritmo Manhattan para encontrar la ruta más corta
- **Terminación**: Se detiene cuando llega al teletransporte o es capturado

#### 2. **AgentThread** - Hilos de los Agentes
```java
public class AgentThread extends Thread {
    private final int agentIndex;
    private final int moveDelay; // 1000ms + variación
}
```
- **Función**: Cada agente tiene su propio hilo que lo persigue al neón
- **Frecuencia**: Base de 1000ms + 100ms × índice del agente (velocidades diferentes)
- **Estrategia**: También usa algoritmo Manhattan para perseguir al neón
- **Coordinación**: Evitan colisiones entre ellos usando sincronización

#### 3. **DisplayThread** - Hilo de Visualización
- **Función**: Actualiza la pantalla mostrando el estado de la matriz
- **Frecuencia**: Cada 500ms
- **Sincronización**: Lee el estado de la matriz de forma thread-safe

#### 4. **InputThread** - Hilo de Entrada del Usuario
- **Función**: Escucha la entrada del usuario para terminar la simulación
- **Comportamiento**: Daemon thread que espera input del teclado

#### 5. **MainThread** - Hilo Principal
- **Función**: Coordina todos los hilos usando `ExecutorService`
- **Gestión**: Maneja el ciclo de vida de todos los hilos

### Sincronización y Thread Safety

#### ReentrantLock
```java
private final ReentrantLock matrixLock;
```
- **Propósito**: Protege el acceso concurrente a la matriz compartida
- **Uso**: Cada movimiento está envuelto en `lock()` y `unlock()`
- **Beneficio**: Evita condiciones de carrera entre hilos

#### Ejemplo de Sincronización:
```java
public void moveNeon() {
    matrixLock.lock();
    try {
        // Lógica de movimiento thread-safe
        if (!simulationRunning) return;
        // ... movimiento del neón
    } finally {
        matrixLock.unlock();
    }
}
```

## 🏗️ Patrones de Diseño Implementados

### 1. Strategy Pattern
- **Interface**: `MovementStrategy`
- **Implementación**: `ManhattanMovementStrategy`
- **Propósito**: Permite intercambiar algoritmos de movimiento
```java
public interface MovementStrategy {
    Position calculateNextMove(Position currentPos, Position targetPos, EntityType[][] matrix);
}
```

### 2. Observer Pattern
- **Interface**: `SimulationObserver`
- **Uso**: Notifica eventos importantes (victoria, eventos del juego)
- **Beneficio**: Desacopla la lógica de juego de la presentación

### 3. Value Object Pattern
- **Clase**: `Position`
- **Características**: Inmutable, representa coordenadas
- **Métodos**: `move()`, `manhattanDistance()`, `equals()`, `hashCode()`

### 4. Facade Pattern
- **Clase**: `MatrixSimulationApp`
- **Propósito**: Simplifica la interacción con el sistema complejo
- **Funcionalidad**: Maneja la creación y coordinación de todos los componentes

### 5. Enum Pattern
- **Clase**: `EntityType`
- **Valores**: `NEON`, `AGENT`, `TELEPORT`, `OBSTACLE`, `EMPTY`
- **Beneficio**: Type-safe representation de entidades

## 🔄 Flujo de Ejecución

### 1. Inicialización
```java
MatrixSimulation simulation = new MatrixSimulation();
simulation.addObserver(this);
```
- Se crea la matriz 8x8
- Se colocan entidades aleatoriamente
- Se configuran observers

### 2. Creación de Hilos
```java
ExecutorService executorService = Executors.newCachedThreadPool();
NeonThread neonThread = new NeonThread(simulation, 800);
AgentThread[] agentThreads = new AgentThread[3];
```

### 3. Ciclo de Juego
```java
while (simulation.isSimulationRunning()) {
    // Cada hilo ejecuta su lógica de movimiento
    // Verificación de condiciones de victoria
    // Actualización de la matriz
}
```

### 4. Detección de Victoria
- **Neón llega al teletransporte**: `simulationRunning = false`
- **Agente captura neón**: `simulationRunning = false`
- Todos los hilos verifican esta bandera en cada iteración

### 5. Limpieza y Terminación
```java
executorService.shutdown();
if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
    executorService.shutdownNow();
}
```

## 🛠️ Características Técnicas Avanzadas

### Gestión de Hilos con ExecutorService
- **Tipo**: `CachedThreadPool` para máxima flexibilidad
- **Daemon Threads**: Para componentes de soporte (input, display)
- **Graceful Shutdown**: Espera terminación ordenada de hilos

### Algoritmo de Movimiento Manhattan
```java
int deltaX = Integer.compare(targetPos.getX(), currentPos.getX());
int deltaY = Integer.compare(targetPos.getY(), currentPos.getY());
Position newPos = currentPos.move(deltaX, deltaY);
```
- Calcula la dirección más directa hacia el objetivo
- Maneja obstáculos con lógica de evasión
- Optimiza la ruta en tiempo real

### Logging Profesional
- **Framework**: SLF4J + Logback
- **Niveles**: INFO para eventos de juego, DEBUG para detalles técnicos
- **Configuración**: `logback.xml` con rotación de archivos

### Manejo de Estados
```java
private volatile boolean simulationRunning;
```
- Variable compartida entre hilos
- Terminación coordinada de toda la simulación
- Thread-safe state management

## 🎯 Ventajas de esta Arquitectura

1. **Escalabilidad**: Fácil agregar más agentes o tipos de entidades
2. **Mantenibilidad**: Patrones de diseño facilitan modificaciones
3. **Performance**: Hilos independientes para máximo paralelismo
4. **Robustez**: Sincronización adecuada previene condiciones de carrera
5. **Flexibilidad**: Strategy pattern permite diferentes algoritmos de IA
6. **Observabilidad**: Logging y observers para monitoreo

## 🧪 Testing y Concurrencia

### Tests Unitarios
- Verifican lógica de movimiento
- Testean condiciones de victoria
- Validan thread-safety de operaciones básicas

### Tests de Concurrencia
- Verifican que no hay deadlocks
- Testean condiciones de carrera
- Validan terminación correcta de hilos

## 🛠️ Características Técnicas

### Concurrencia Avanzada
- **Hilos independientes**: Cada entidad se ejecuta en su propio hilo
- **ExecutorService**: Manejo profesional del pool de hilos con `CachedThreadPool`
- **Sincronización**: `ReentrantLock` para acceso seguro a la matriz compartida
- **Velocidades diferenciadas**: Cada tipo de entidad tiene su propia velocidad de movimiento
- **Graceful shutdown**: Terminación ordenada de todos los hilos

### Logging Profesional
- **SLF4J + Logback**: Framework de logging empresarial
- **Niveles configurables**: INFO, DEBUG, WARN, ERROR
- **Rotación de archivos**: Logs se almacenan en `logs/matrix-simulation.log`
- **Configuración externa**: `logback.xml` para personalización

### Gestión de Dependencias
- **Maven**: Gestión completa del ciclo de vida del proyecto
- **Estructura estándar**: Convenciones Maven para organización
- **Dependencias gestionadas**: Versionado automático y resolución de conflictos

### Algoritmos Inteligentes
- **Pathfinding Manhattan**: Algoritmo de búsqueda de rutas eficiente
- **Detección de colisiones**: Prevención de movimientos inválidos
- **Evasión de obstáculos**: Lógica de navegación alrededor de barreras
- **IA básica**: Comportamiento emergente de la interacción entre entidades

## 🚀 Cómo Ejecutar

### Prerrequisitos
- **Java 11** o superior
- **Maven 3.6** o superior

### Compilación y Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/marzo245/matrixconcurrente.git
cd matrixconcurrente

# Limpiar y compilar el proyecto
mvn clean compile

# Ejecutar la simulación
mvn exec:java -Dexec.mainClass="com.matrix.simulation.MatrixSimulationApp"

# Alternativa: Crear JAR ejecutable
mvn clean package
java -jar target/matrix-concurrent-simulation-1.0.0.jar

# Ejecutar tests
mvn test

# Ejecutar en modo silencioso (menos logs)
mvn exec:java -Dexec.mainClass="com.matrix.simulation.MatrixSimulationApp" -q
```

### Usando VS Code
1. Abrir el proyecto en VS Code
2. Asegurarse de tener la extensión "Extension Pack for Java" instalada
3. Usar `Ctrl+Shift+P` y buscar "Tasks: Run Task"
4. Seleccionar "Maven: Ejecutar Simulación"
5. O presionar `F5` para ejecutar en modo debug

### Controles Durante la Ejecución
- **Visualización automática**: La matriz se actualiza cada 500ms
- **Terminar simulación**: Presiona `ENTER` en cualquier momento
- **Logs**: Se muestran eventos importantes en consola
- **Archivo de logs**: Consultar `logs/matrix-simulation.log` para detalles

## 🎯 Configuración y Personalización

### Velocidades de Movimiento
Puedes modificar en `MatrixSimulationApp.java`:
```java
int neonSpeed = 800;      // Neón se mueve cada 800ms
int agentSpeed = 1000;    // Agentes base cada 1000ms
int displaySpeed = 500;   // Actualización de pantalla cada 500ms
```

### Tamaño de Matriz
Cambiar en `MatrixSimulation.java`:
```java
private static final int MATRIX_SIZE = 8; // Cambiar a cualquier tamaño
```

### Número de Agentes
Modificar en el método `initializeMatrix()`:
```java
for (int i = 0; i < 3; i++) { // Cambiar el 3 por el número deseado
    // ... lógica de creación de agentes
}
```

### Densidad de Obstáculos
Ajustar en `initializeMatrix()`:
```java
int obstacleCount = (MATRIX_SIZE * MATRIX_SIZE) / 7; // Cambiar divisor para más/menos obstáculos
```

## 📁 Estructura del Proyecto Maven

```
matrixConcurrente/
├── src/
│   ├── main/
│   │   ├── java/com/matrix/simulation/
│   │   │   ├── MatrixSimulationApp.java     # 🚀 Aplicación principal (Facade)
│   │   │   ├── MatrixSimulation.java        # 🎮 Lógica central del juego
│   │   │   ├── Position.java                # 📍 Value Object para coordenadas
│   │   │   ├── NeonThread.java              # 🔵 Hilo del neón
│   │   │   ├── AgentThread.java             # 🔴 Hilo de agentes
│   │   │   ├── SimulationObserver.java      # 👁️ Interface Observer
│   │   │   ├── entities/
│   │   │   │   └── EntityType.java          # 🏷️ Enum de tipos de entidad
│   │   │   └── patterns/
│   │   │       ├── MovementStrategy.java    # 🧭 Strategy interface
│   │   │       └── ManhattanMovementStrategy.java # 📐 Implementación Manhattan
│   │   └── resources/
│   │       └── logback.xml                  # 📝 Configuración de logging
│   └── test/
│       └── java/com/matrix/simulation/      # 🧪 Tests unitarios
├── target/                                  # 📦 Archivos compilados (auto-generado)
├── logs/                                    # 📊 Archivos de log (auto-generado)
├── .vscode/                                 # ⚙️ Configuración VS Code
├── .github/                                 # 🤖 GitHub copilot instructions
├── pom.xml                                  # 📋 Configuración Maven
├── .gitignore                              # 🚫 Archivos ignorados por Git
└── README.md                               # 📖 Este archivo
```

## � Características del Juego

### Aleatoriedad
- **Posiciones iniciales**: Todas las entidades se colocan aleatoriamente
- **Distribución de obstáculos**: Aproximadamente 14% de la matriz (configurable)
- **Generación de terreno**: Cada partida es única
- **Seed aleatoria**: Usa `ThreadLocalRandom` para mejor rendimiento concurrente

### Mecánicas Avanzadas
- **Colisiones inteligentes**: Los agentes no pueden ocupar la misma casilla
- **Restauración de teletransporte**: Si una entidad pasa sobre T, se restaura automáticamente
- **Movimiento diagonal**: Las entidades se mueven en línea recta hacia su objetivo
- **Evasión**: Si no pueden moverse directamente, intentan rutas alternativas

## 🔄 Estados y Transiciones

### Estados del Juego
1. **INICIALIZANDO**: Creando matriz y colocando entidades
2. **EJECUTANDO**: Simulación activa con hilos corriendo
3. **VICTORIA_NEON**: Neón llegó al teletransporte
4. **VICTORIA_AGENTE**: Un agente capturó al neón
5. **TERMINADO_MANUAL**: Usuario terminó la simulación

### Transiciones de Estado
```
INICIALIZANDO → EJECUTANDO → {VICTORIA_NEON | VICTORIA_AGENTE | TERMINADO_MANUAL}
```

## 🧪 Testing y Calidad

### Tests Implementados
```bash
# Ejecutar todos los tests
mvn test

# Test específico
mvn test -Dtest=MatrixSimulationTest

# Test con cobertura
mvn jacoco:report
```

### Tipos de Tests
- **Tests de unidad**: Verifican lógica individual de cada clase
- **Tests de integración**: Validan interacción entre componentes
- **Tests de concurrencia**: Verifican thread-safety y ausencia de deadlocks
- **Tests de rendimiento**: Miden tiempo de ejecución y uso de memoria

## 📊 Métricas y Monitoreo

### Información de Logs
```
19:22:01.456 [Neon-Thread] INFO  c.m.s.NeonThread - Hilo del Neón iniciado con delay de 800ms
19:22:01.457 [Agent-1-Thread] INFO  c.m.s.AgentThread - Hilo del Agente 1 iniciado con delay de 1000ms
19:22:01.458 [Agent-2-Thread] INFO  c.m.s.AgentThread - Hilo del Agente 2 iniciado con delay de 1100ms
19:22:01.459 [Agent-3-Thread] INFO  c.m.s.AgentThread - Hilo del Agente 3 iniciado con delay de 1200ms
```

### Estadísticas de Partida
- Tiempo total de ejecución
- Número de movimientos realizados por cada entidad
- Distancia recorrida
- Eficiencia de pathfinding

## 📝 Comandos Maven Útiles

```bash
# Desarrollo
mvn clean compile                    # Compilar código fuente
mvn compile exec:java               # Compilar y ejecutar
mvn clean test                      # Ejecutar tests

# Documentación
mvn javadoc:javadoc                 # Generar documentación
mvn site                           # Generar sitio del proyecto

# Análisis
mvn dependency:tree                 # Ver árbol de dependencias
mvn dependency:analyze             # Analizar dependencias no utilizadas
mvn versions:display-updates       # Ver actualizaciones disponibles

# Empaquetado
mvn clean package                   # Crear JAR ejecutable
mvn package -DskipTests            # Crear JAR sin ejecutar tests

# Limpieza
mvn clean                          # Limpiar archivos generados
mvn dependency:purge-local-repository  # Limpiar cache local
```

## 🔧 Solución de Problemas

### Problemas Comunes

#### Error de compilación "cannot find symbol"
```bash
# Limpiar y recompilar
mvn clean compile
```

#### OutOfMemoryError
```bash
# Aumentar memoria heap
export MAVEN_OPTS="-Xmx1024m"
mvn exec:java
```

#### Puerto ocupado / Archivo en uso
```bash
# En Windows, matar procesos Java
taskkill /F /IM java.exe
```

#### Tests fallan aleatoriamente
- Los tests de concurrencia pueden fallar ocasionalmente
- Ejecutar `mvn test` nuevamente
- Es normal en sistemas con alta carga

### Debugging
```bash
# Ejecutar con debug de Maven
mvn -X exec:java

# Ejecutar con debug de Java
mvn exec:java -Dexec.args="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## � Futuras Mejoras

### Posibles Extensiones
- **Algoritmos avanzados**: Implementar A* para pathfinding más inteligente
- **Interfaz gráfica**: Migrar a JavaFX para visualización rica
- **Red multiplayer**: Permitir múltiples jugadores controlando diferentes entidades
- **IA avanzada**: Machine learning para comportamiento adaptativo
- **Configuración externa**: Archivos properties para personalización
- **Métricas avanzadas**: Dashboard web con estadísticas en tiempo real
- **Replay system**: Guardar y reproducir partidas
- **Niveles dinámicos**: Generación procedural de mapas

### Optimizaciones Técnicas
- **Pool de objetos**: Reutilizar instancias de Position
- **Lock-free algorithms**: Usar estructuras de datos concurrentes
- **Profiling**: Optimizar hotspots de CPU
- **Memory mapping**: Para mapas muy grandes
- **Clustering**: Distribución en múltiples JVMs

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver archivo LICENSE para más detalles.

## 👥 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crear una branch para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la branch (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## � Agradecimientos

- **Java Concurrency in Practice** por Brian Goetz
- **Design Patterns** por Gang of Four
- **Clean Code** por Robert C. Martin
- **Maven** por Apache Software Foundation
- **SLF4J/Logback** por QOS.ch

---

**Desarrollado con ❤️ usando Java, Maven, y mejores prácticas de programación concurrente.**
