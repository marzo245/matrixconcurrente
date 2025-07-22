package com.matrix.simulation;

import com.matrix.simulation.entities.EntityType;
import com.matrix.simulation.patterns.ManhattanMovementStrategy;
import com.matrix.simulation.patterns.MovementStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.text.html.parser.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulación de matriz 8x8 con Neón, Agentes, Teletransporte y Obstáculos
 * Implementa varios patrones de diseño:
 * - Singleton Pattern para la simulación
 * - Strategy Pattern para movimientos
 * - Observer Pattern para notificaciones
 * - Factory Pattern para creación de entidades
 */
public class MatrixSimulation {

  private static final Logger logger = LoggerFactory.getLogger(
    MatrixSimulation.class
  );
  private static final int MATRIX_SIZE = 12; // Aumentado de 8 a 12 para más espacio

  private EntityType[][] matrix;
  private Position neonPos;
  private Position teleportPos;
  private List<Position> agentPositions;
  private boolean simulationRunning;
  private final ReentrantLock matrixLock;
  private final MovementStrategy movementStrategy;
  private final List<SimulationObserver> observers;
  private final long startTime; // Tiempo de inicio de la simulación

  // Variable para controlar el tipo de visualización
  private boolean useAnimatedDisplay = true;

  public MatrixSimulation() {
    this(new ManhattanMovementStrategy());
  }

  public MatrixSimulation(MovementStrategy movementStrategy) {
    this.matrix = new EntityType[MATRIX_SIZE][MATRIX_SIZE];
    this.agentPositions = new ArrayList<>();
    this.matrixLock = new ReentrantLock();
    this.movementStrategy = movementStrategy;
    this.observers = new ArrayList<>();
    this.simulationRunning = true;
    this.startTime = System.currentTimeMillis(); // Inicializar tiempo de inicio
    initializeMatrix();
    logger.debug(
      "Simulación inicializada con matriz {}x{}",
      MATRIX_SIZE,
      MATRIX_SIZE
    );
  }

  /**
   * Inicializa la matriz con obstáculos, neón, agentes y teletransporte
   * Con lógica mejorada para evitar que el neón aparezca cerca de agentes
   */
  private void initializeMatrix() {
    // Llenar matriz con espacios vacíos
    for (int i = 0; i < MATRIX_SIZE; i++) {
      for (int j = 0; j < MATRIX_SIZE; j++) {
        matrix[i][j] = EntityType.EMPTY;
      }
    }

    // Colocar obstáculos aleatoriamente (aproximadamente 15% de la matriz para más desafío)
    int obstacleCount = (MATRIX_SIZE * MATRIX_SIZE) / 6; // Más obstáculos para mapa más grande
    for (int i = 0; i < obstacleCount; i++) {
      Position pos = getRandomEmptyPosition();
      matrix[pos.getX()][pos.getY()] = EntityType.OBSTACLE;
    }

    // Colocar teletransporte en una esquina aleatoria, asegurando que no coincida con la posición de Neón
    Position[] corners = {
      new Position(0, 0),
      new Position(0, MATRIX_SIZE - 1),
      new Position(MATRIX_SIZE - 1, 0),
      new Position(MATRIX_SIZE - 1, MATRIX_SIZE - 1),
    };
    int neonX = 0; // fila deseada
    int neonY = 11; // columna deseada
    Position neonFixedPos = new Position(neonX, neonY);
    do {
      teleportPos =
        corners[ThreadLocalRandom.current().nextInt(corners.length)];
    } while (teleportPos.equals(neonFixedPos));
    matrix[teleportPos.getX()][teleportPos.getY()] = EntityType.TELEPORT;

    // Colocar neón SIEMPRE en una posición fija específica
    neonPos = neonFixedPos;
    matrix[neonPos.getX()][neonPos.getY()] = EntityType.NEON;

    // Colocar agentes en posiciones completamente aleatorias
    int agentCount = Math.min(
      7,
      Math.max(
        1,
        System.getProperty("agent.count") != null
          ? Integer.parseInt(System.getProperty("agent.count"))
          : 3
      )
    );
    for (int i = 0; i < agentCount; i++) {
      Position agentPos = getRandomEmptyPosition();
      agentPositions.add(agentPos);
      matrix[agentPos.getX()][agentPos.getY()] = EntityType.AGENT;
    }

    // Colocar neón asegurando distancia mínima de agentes (al menos 4 casillas)

    notifyObservers("Matriz inicializada");
  }

  /**
   * Encuentra una posición para el neón que esté alejada de los agentes
   */
  private Position findPositionAwayFromAgents(int minDistance) {
    int maxAttempts = 100;
    int attempts = 0;

    while (attempts < maxAttempts) {
      Position candidate = getRandomEmptyPosition();
      boolean validPosition = true;

      // Verificar distancia a todos los agentes
      for (Position agentPos : agentPositions) {
        if (candidate.manhattanDistance(agentPos) < minDistance) {
          validPosition = false;
          break;
        }
      }

      // También verificar distancia al teletransporte (no demasiado cerca)
      if (validPosition && candidate.manhattanDistance(teleportPos) >= 4) {
        return candidate;
      }

      attempts++;
    }

    // Si no encuentra una posición óptima, usar la esquina más alejada de agentes
    Position bestPosition = null;
    int maxMinDistance = 0;

    Position[] fallbackPositions = {
      new Position(0, 0),
      new Position(0, MATRIX_SIZE - 1),
      new Position(MATRIX_SIZE - 1, 0),
      new Position(MATRIX_SIZE - 1, MATRIX_SIZE - 1),
    };

    for (Position pos : fallbackPositions) {
      if (matrix[pos.getX()][pos.getY()] == EntityType.EMPTY) {
        int minDistanceToAgents = Integer.MAX_VALUE;
        for (Position agentPos : agentPositions) {
          minDistanceToAgents =
            Math.min(minDistanceToAgents, pos.manhattanDistance(agentPos));
        }
        if (minDistanceToAgents > maxMinDistance) {
          maxMinDistance = minDistanceToAgents;
          bestPosition = pos;
        }
      }
    }

    return bestPosition != null ? bestPosition : getRandomEmptyPosition();
  }

  /**
   * Encuentra una posición vacía cerca de la posición dada
   */
  private Position findNearbyEmptyPosition(Position center) {
    for (int radius = 1; radius < MATRIX_SIZE; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          int newX = center.getX() + dx;
          int newY = center.getY() + dy;
          if (
            newX >= 0 && newX < MATRIX_SIZE && newY >= 0 && newY < MATRIX_SIZE
          ) {
            if (matrix[newX][newY] == EntityType.EMPTY) {
              return new Position(newX, newY);
            }
          }
        }
      }
    }
    // Si no encuentra ninguna, usar el método original
    return getRandomEmptyPosition();
  }

  /**
   * Genera una posición aleatoria vacía en la matriz
   */
  private Position getRandomEmptyPosition() {
    Position pos;
    do {
      pos =
        new Position(
          ThreadLocalRandom.current().nextInt(MATRIX_SIZE),
          ThreadLocalRandom.current().nextInt(MATRIX_SIZE)
        );
    } while (matrix[pos.getX()][pos.getY()] != EntityType.EMPTY);
    return pos;
  }

  /**
   * Mueve el neón hacia el teletransporte usando la estrategia de movimiento
   */
  public void moveNeon() {
    matrixLock.lock();
    try {
      if (!simulationRunning) return;

      Position newPos = movementStrategy.calculateNextMove(
        neonPos,
        teleportPos,
        matrix,
        agentPositions
      );

      // Si la nueva posición es diferente y válida
      if (
        !newPos.equals(neonPos) &&
        isValidMove(newPos) &&
        matrix[newPos.getX()][newPos.getY()] != EntityType.AGENT
      ) {
        // Limpiar posición anterior
        matrix[neonPos.getX()][neonPos.getY()] = EntityType.EMPTY;

        // Verificar si llegó al teletransporte
        if (newPos.equals(teleportPos)) {
          logger.debug("¡NEÓN HA LLEGADO AL TELETRANSPORTE!");
          simulationRunning = false;
          notifyObservers("¡NEÓN HA GANADO!");
          return;
        }

        // Mover a nueva posición
        neonPos = newPos;
        matrix[neonPos.getX()][neonPos.getY()] = EntityType.NEON;

        // Restaurar teletransporte si es necesario
        if (
          matrix[teleportPos.getX()][teleportPos.getY()] == EntityType.EMPTY
        ) {
          matrix[teleportPos.getX()][teleportPos.getY()] = EntityType.TELEPORT;
        }
      }
    } finally {
      matrixLock.unlock();
    }
  }

  /**
   * Mueve un agente específico hacia el neón
   */
  public void moveAgent(int agentIndex) {
    matrixLock.lock();
    try {
      if (!simulationRunning || agentIndex >= agentPositions.size()) return;

      Position agentPos = agentPositions.get(agentIndex);
      Position newPos = movementStrategy.calculateNextMove(
        agentPos,
        neonPos,
        matrix,
        agentPositions
      );

      // Si la nueva posición es diferente y válida
      if (
        !newPos.equals(agentPos) &&
        isValidMove(newPos) &&
        matrix[newPos.getX()][newPos.getY()] != EntityType.OBSTACLE
      ) {
        // Verificar si capturó al neón
        if (newPos.equals(neonPos)) {
          logger.debug("¡AGENTE {} HA CAPTURADO AL NEÓN!", agentIndex + 1);
          simulationRunning = false;
          notifyObservers("¡AGENTE " + (agentIndex + 1) + " HA GANADO!");
          return;
        }

        // Si no hay conflicto con otro agente, moverse
        if (
          matrix[newPos.getX()][newPos.getY()] == EntityType.EMPTY ||
          matrix[newPos.getX()][newPos.getY()] == EntityType.TELEPORT
        ) {
          matrix[agentPos.getX()][agentPos.getY()] = EntityType.EMPTY;
          agentPositions.set(agentIndex, newPos);
          matrix[newPos.getX()][newPos.getY()] = EntityType.AGENT;

          // Restaurar teletransporte si es necesario
          if (agentPos.equals(teleportPos)) {
            matrix[teleportPos.getX()][teleportPos.getY()] =
              EntityType.TELEPORT;
          }
        }
      }
    } finally {
      matrixLock.unlock();
    }
  }

  /**
   * Verifica si una posición es válida dentro de la matriz
   */
  private boolean isValidMove(Position pos) {
    return (
      pos.getX() >= 0 &&
      pos.getX() < MATRIX_SIZE &&
      pos.getY() >= 0 &&
      pos.getY() < MATRIX_SIZE
    );
  }

  /**
   * Configurar si usar visualización animada o simple
   */
  public void setAnimatedDisplay(boolean animated) {
    this.useAnimatedDisplay = animated;
  }

  /**
   * Método principal de visualización que elige entre animada o simple
   */
  public void displayMatrix() {
    if (useAnimatedDisplay) {
      displayMatrixAnimated();
    } else {
      displayMatrixSimple();
    }
  }

  /**
   * Muestra el estado actual de la matriz con animación secuencial
   */
  private void displayMatrixAnimated() {
    matrixLock.lock();
    try {
      // Limpiar pantalla para actualización en vivo
      clearScreen();

      System.out.println(
        "╔════════════════════════════════════════════════════╗"
      );
      System.out.println(
        "║            MATRIZ CONCURRENTE 12x12 - EN VIVO     ║"
      );
      System.out.println(
        "╚════════════════════════════════════════════════════╝"
      );
      System.out.println();

      // Mostrar encabezado de columnas
      System.out.print("   "); // Espacio extra para números de fila de dos dígitos
      for (int j = 0; j < MATRIX_SIZE; j++) {
        System.out.printf("%2d ", j); // Formato de 2 caracteres para columnas
      }
      System.out.println();

      // Mostrar matriz completa sin pausas para visualización en vivo
      for (int i = 0; i < MATRIX_SIZE; i++) {
        System.out.printf("%2d ", i); // Formato de 2 caracteres para filas
        for (int j = 0; j < MATRIX_SIZE; j++) {
          String symbol = String.valueOf(matrix[i][j].getSymbol());
          // Añadir colores para mejor visualización
          System.out.print(getColoredSymbol(symbol) + " ");
        }
        System.out.println();
      }

      System.out.println();
      System.out.println(
        "🔵 N=Neón  🔴 A=Agente  ⭐ T=Teletransporte  ⬛ #=Obstáculo  ⬜ .=Vacío"
      );
      System.out.println("═".repeat(60));

      // Mostrar estadísticas básicas
      showGameStats();

      System.out.println("═".repeat(60));
      System.out.println("Presiona ENTER para terminar la simulación");

      // Forzar que se muestre inmediatamente
      System.out.flush();
    } finally {
      matrixLock.unlock();
    }
  }

  /**
   * Versión simple de displayMatrix sin animación (para terminales que no soporten colores)
   */
  public void displayMatrixSimple() {
    matrixLock.lock();
    try {
      System.out.println("\n" + "=".repeat(50));
      System.out.println("         MATRIZ CONCURRENTE 12x12");
      System.out.println("=".repeat(50));

      // Mostrar encabezado de columnas
      System.out.print("   "); // Espacio extra para números de fila de dos dígitos
      for (int j = 0; j < MATRIX_SIZE; j++) {
        System.out.printf("%2d ", j); // Formato de 2 caracteres
      }
      System.out.println();

      // Mostrar matriz
      for (int i = 0; i < MATRIX_SIZE; i++) {
        System.out.printf("%2d ", i); // Formato de 2 caracteres para filas
        for (int j = 0; j < MATRIX_SIZE; j++) {
          System.out.print(matrix[i][j].getSymbol() + " ");
        }
        System.out.println();
      }

      System.out.println();
      System.out.println(
        "N=Neón, A=Agente, T=Teletransporte, #=Obstáculo, .=Vacío"
      );
      System.out.println("=".repeat(40));

      // Estadísticas básicas
      long currentTime = System.currentTimeMillis();
      long elapsedSeconds = (currentTime - startTime) / 1000;
      int distance = neonPos.manhattanDistance(teleportPos);

      System.out.println(
        "Tiempo: " +
        elapsedSeconds +
        "s | Distancia: " +
        distance +
        " | Agentes: " +
        agentPositions.size()
      );
      System.out.println();
    } finally {
      matrixLock.unlock();
    }
  }

  /**
   * Método de visualización que se puede llamar desde dentro de un lock
   */
  private void displayMatrixWithoutLock() {
    if (useAnimatedDisplay) {
      displayMatrixAnimatedWithoutLock();
    } else {
      displayMatrixSimpleWithoutLock();
    }
  }

  /**
   * Versión sin lock del método animado
   */
  private void displayMatrixAnimatedWithoutLock() {
    // Separador visual sin limpiar pantalla agresivamente
    System.out.println("\n" + "═".repeat(40));

    System.out.println("╔════════════════════════════════════════╗");
    System.out.println("║     MATRIZ CONCURRENTE 12x12          ║");
    System.out.println("╚════════════════════════════════════════╝");
    System.out.println();

    // Mostrar encabezado de columnas
    System.out.print("   ");
    for (int j = 0; j < MATRIX_SIZE; j++) {
      System.out.printf("%2d ", j);
    }
    System.out.println();

    // Mostrar matriz (sin pausa para movimientos rápidos)
    for (int i = 0; i < MATRIX_SIZE; i++) {
      System.out.printf("%2d ", i);
      for (int j = 0; j < MATRIX_SIZE; j++) {
        String symbol = String.valueOf(matrix[i][j].getSymbol());
        System.out.print(getColoredSymbol(symbol) + " ");
      }
      System.out.println();
    }

    System.out.println();
    System.out.println(
      "🔵 N=Neón  🔴 A=Agente  ⭐ T=Teletransporte  ⬛ #=Obstáculo  ⬜ .=Vacío"
    );
    System.out.println("═".repeat(40));

    // Mostrar estadísticas básicas
    showGameStats();
  }

  /**
   * Versión sin lock del método simple
   */
  private void displayMatrixSimpleWithoutLock() {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("    MATRIZ CONCURRENTE 12x12");
    System.out.println("=".repeat(40));

    // Mostrar encabezado de columnas
    System.out.print("   ");
    for (int j = 0; j < MATRIX_SIZE; j++) {
      System.out.printf("%2d ", j);
    }
    System.out.println();

    // Mostrar matriz
    for (int i = 0; i < MATRIX_SIZE; i++) {
      System.out.printf("%2d ", i);
      for (int j = 0; j < MATRIX_SIZE; j++) {
        System.out.print(matrix[i][j].getSymbol() + " ");
      }
      System.out.println();
    }

    System.out.println();
    System.out.println(
      "N=Neón, A=Agente, T=Teletransporte, #=Obstáculo, .=Vacío"
    );
    System.out.println("=".repeat(40));

    // Estadísticas básicas
    long currentTime = System.currentTimeMillis();
    long elapsedSeconds = (currentTime - startTime) / 1000;
    int distance = neonPos.manhattanDistance(teleportPos);

    System.out.println(
      "Tiempo: " +
      elapsedSeconds +
      "s | Distancia: " +
      distance +
      " | Agentes: " +
      agentPositions.size()
    );
    System.out.println();
  }

  /**
   * Limpia la pantalla completamente para visualización en vivo
   */
  private void clearScreen() {
    try {
      // Para Windows
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } else {
        // Para Unix/Linux/Mac - Limpieza completa
        System.out.print("\033[2J"); // Limpiar pantalla completa
        System.out.print("\033[H"); // Mover cursor a la esquina superior izquierda
        System.out.flush();
      }
    } catch (Exception e) {
      // Fallback: múltiples líneas en blanco
      for (int i = 0; i < 100; i++) {
        System.out.println();
      }
    }
  }

  /**
   * Añade colores a los símbolos para mejor visualización
   */
  private String getColoredSymbol(String symbol) {
    switch (symbol) {
      case "N":
        return "\033[34m" + symbol + "\033[0m"; // Azul para Neón
      case "A":
        return "\033[31m" + symbol + "\033[0m"; // Rojo para Agente
      case "T":
        return "\033[33m" + symbol + "\033[0m"; // Amarillo para Teletransporte
      case "#":
        return "\033[90m" + symbol + "\033[0m"; // Gris para Obstáculo
      default:
        return symbol; // Sin color para espacios vacíos
    }
  }

  /**
   * Muestra estadísticas básicas del juego
   */
  private void showGameStats() {
    long currentTime = System.currentTimeMillis();
    long elapsedSeconds = (currentTime - startTime) / 1000;

    System.out.println("⏱️  Tiempo transcurrido: " + elapsedSeconds + "s");
    System.out.println(
      "🎯 Neón en: (" + neonPos.getX() + ", " + neonPos.getY() + ")"
    );
    System.out.println(
      "🚀 Teletransporte en: (" +
      teleportPos.getX() +
      ", " +
      teleportPos.getY() +
      ")"
    );

    // Calcular distancia Manhattan del neón al teletransporte
    int distance = neonPos.manhattanDistance(teleportPos);
    System.out.println("📏 Distancia al objetivo: " + distance + " pasos");

    System.out.println("👥 Agentes activos: " + agentPositions.size());
  }

  // Observer Pattern methods
  public void addObserver(SimulationObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(SimulationObserver observer) {
    observers.remove(observer);
  }

  private void notifyObservers(String message) {
    for (SimulationObserver observer : observers) {
      observer.onSimulationEvent(message);
    }
  }

  // Getters
  public boolean isSimulationRunning() {
    return simulationRunning;
  }

  public void stopSimulation() {
    simulationRunning = false;
    logger.debug("Simulación detenida manualmente");
    notifyObservers("Simulación detenida");
  }

  public Position getNeonPosition() {
    return neonPos;
  }

  public Position getTeleportPosition() {
    return teleportPos;
  }

  public List<Position> getAgentPositions() {
    return new ArrayList<>(agentPositions);
  }

  public int getMatrixSize() {
    return MATRIX_SIZE;
  }

  public EntityType[][] getMatrix() {
    matrixLock.lock();
    try {
      EntityType[][] copy = new EntityType[MATRIX_SIZE][MATRIX_SIZE];
      for (int i = 0; i < MATRIX_SIZE; i++) {
        System.arraycopy(matrix[i], 0, copy[i], 0, MATRIX_SIZE);
      }
      return copy;
    } finally {
      matrixLock.unlock();
    }
  }
}
