# Simulación de Matriz Concurrente 12x12

Este proyecto implementa una simulación en Java de una matriz 12x12 donde un **Neón** intenta llegar a un **Teletransporte** mientras es perseguido por **Agentes**, todo ejecutándose de forma concurrente usando hilos y patrones de diseño.

## 🎮 Lógica del Juego

### Entidades
- **Neón (N)**: Intenta llegar al teletransporte.
- **Agentes (A)**: Persiguen al neón para capturarlo.
- **Teletransporte (T)**: Objetivo final del neón.
- **Obstáculos (#)**: Bloquean el movimiento.
- **Espacios vacíos (.)**: Casillas libres.

### Reglas y Condiciones
- Cada entidad se mueve un paso por turno.
- No pueden atravesar obstáculos.
- Los agentes no pueden ocupar la misma casilla.
- El neón evita colisionar con los agentes.
- Victoria del Neón: llega al teletransporte.
- Victoria de los Agentes: capturan al neón.

## 🤖 Lógica de Movimiento

### Neón (N)
- **Algoritmo:** Utiliza BFS (Breadth-First Search) para encontrar el camino más corto hasta el teletransporte.
- **Evasión:** Considera obstáculos (`#`) y agentes (`A`) como casillas bloqueadas, por lo que siempre busca rodearlos.
- **Decisión:** Si existe un camino, Neón lo sigue paso a paso. Si no hay camino posible, intenta moverse a cualquier casilla libre adyacente (aunque no acerque al objetivo). Si está completamente rodeado, se queda quieto.
- **Ventaja:** Esta lógica le permite a Neón encontrar rutas alternativas y no quedarse atascado ante obstáculos, siempre que exista un camino.

### Agentes (A)
- **Algoritmo:** Usan la estrategia Manhattan, es decir, siempre intentan moverse en línea recta (horizontal o vertical) hacia la posición actual de Neón.
- **Evasión:** No atraviesan obstáculos ni pueden ocupar la misma casilla que otro agente.
- **Decisión:** Si el movimiento directo está bloqueado, prueban moverse en X o Y, y si tampoco es posible, intentan alternativas adyacentes.
- **Objetivo:** Su meta es capturar a Neón lo más rápido posible, priorizando el camino más corto disponible.

## 🧵 Arquitectura Concurrente

- **NeonThread**: Controla el movimiento del neón (BFS).
- **AgentThread**: Cada agente tiene su propio hilo (Manhattan).
- **DisplayThread**: Actualiza la pantalla.
- **InputThread**: Escucha la entrada del usuario.
- **MainThread**: Coordina todos los hilos.
- **ReentrantLock**: Protege el acceso concurrente a la matriz.

## 🏗️ Patrones de Diseño
- **Strategy Pattern**: `MovementStrategy` (BFS para Neón, Manhattan para agentes).
- **Observer Pattern**: Notifica eventos importantes.
- **Value Object Pattern**: `Position` (coordenadas inmutables).
- **Facade Pattern**: `MatrixSimulationApp` (interfaz principal).
- **Enum Pattern**: `EntityType` (tipos de entidad).

## 🔄 Flujo de Ejecución
- Matriz 12x12.
- Neón inicia en (0, 11).
- Teletransporte en una esquina aleatoria distinta.
- Agentes en posiciones aleatorias.
- Obstáculos aleatorios.
- Neón usa BFS para buscar el camino más corto al teletransporte.
- Agentes usan Manhattan para perseguir a Neón.
- La simulación termina si Neón llega al teletransporte o es capturado.

## 🚀 Cómo Ejecutar

### Prerrequisitos
- Java 11+
- Maven 3.6+

### Compilación y Ejecución
```bash
git clone https://github.com/marzo245/matrixconcurrente.git
cd matrixconcurrente
mvn clean compile
mvn exec:java -Dexec.mainClass="com.matrix.simulation.MatrixSimulationApp"
```

## 🎯 Personalización
- Velocidades, tamaño de matriz, número de agentes y densidad de obstáculos se pueden ajustar en el código fuente (`MatrixSimulation.java` y `MatrixSimulationApp.java`).

## 📁 Estructura del Proyecto
```
matrixConcurrente/
├── src/main/java/com/matrix/simulation/...
├── src/test/java/com/matrix/simulation/...
├── pom.xml
├── README.md
└── ...
```

## 📝 Notas Importantes
- El movimiento de Neón es inteligente: si existe un camino, lo encontrará y lo seguirá, rodeando obstáculos y agentes.
- Si el camino está bloqueado, Neón intentará moverse a cualquier casilla libre adyacente.
- La posición inicial de Neón es fija y los agentes aparecen en lugares aleatorios.

## 🧪 Testing
- Ejecuta `mvn test` para correr los tests unitarios y de concurrencia.

## 📄 Licencia
Este proyecto está bajo la licencia MIT.

## 👥 Contribuciones
¡Las contribuciones son bienvenidas! Haz un fork, crea una branch y abre un Pull Request.