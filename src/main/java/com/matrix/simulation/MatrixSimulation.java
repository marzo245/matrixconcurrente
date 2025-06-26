package com.matrix.simulation;

import com.matrix.simulation.entities.EntityType;
import com.matrix.simulation.patterns.ManhattanMovementStrategy;
import com.matrix.simulation.patterns.MovementStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
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
  private static final int MATRIX_SIZE = 8;

  private EntityType[][] matrix;
  private Position neonPos;
  private Position teleportPos;
  private List<Position> agentPositions;
  private boolean simulationRunning;
  private final ReentrantLock matrixLock;
  private final MovementStrategy movementStrategy;
  private final List<SimulationObserver> observers;

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
    initializeMatrix();
    logger.info(
      "Simulación inicializada con matriz {}x{}",
      MATRIX_SIZE,
      MATRIX_SIZE
    );
  }

  /**
   * Inicializa la matriz con obstáculos, neón, agentes y teletransporte
   */
  private void initializeMatrix() {
    // Llenar matriz con espacios vacíos
    for (int i = 0; i < MATRIX_SIZE; i++) {
      for (int j = 0; j < MATRIX_SIZE; j++) {
        matrix[i][j] = EntityType.EMPTY;
      }
    }

    // Colocar obstáculos aleatoriamente (aproximadamente 15% de la matriz)
    int obstacleCount = (MATRIX_SIZE * MATRIX_SIZE) / 7;
    for (int i = 0; i < obstacleCount; i++) {
      Position pos = getRandomEmptyPosition();
      matrix[pos.getX()][pos.getY()] = EntityType.OBSTACLE;
    }

    // Colocar teletransporte
    teleportPos = getRandomEmptyPosition();
    matrix[teleportPos.getX()][teleportPos.getY()] = EntityType.TELEPORT;

    // Colocar neón
    neonPos = getRandomEmptyPosition();
    matrix[neonPos.getX()][neonPos.getY()] = EntityType.NEON;

    // Colocar agentes (al menos 3)
    for (int i = 0; i < 3; i++) {
      Position agentPos = getRandomEmptyPosition();
      agentPositions.add(agentPos);
      matrix[agentPos.getX()][agentPos.getY()] = EntityType.AGENT;
    }

    notifyObservers("Matriz inicializada");
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
        matrix
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
          logger.info("¡NEÓN HA LLEGADO AL TELETRANSPORTE!");
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
        matrix
      );

      // Si la nueva posición es diferente y válida
      if (
        !newPos.equals(agentPos) &&
        isValidMove(newPos) &&
        matrix[newPos.getX()][newPos.getY()] != EntityType.OBSTACLE
      ) {
        // Verificar si capturó al neón
        if (newPos.equals(neonPos)) {
          logger.info("¡AGENTE {} HA CAPTURADO AL NEÓN!", agentIndex + 1);
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
   * Muestra el estado actual de la matriz
   */
  public void displayMatrix() {
    matrixLock.lock();
    try {
      System.out.println("\n=== ESTADO ACTUAL DE LA MATRIZ ===");
      for (int i = 0; i < MATRIX_SIZE; i++) {
        for (int j = 0; j < MATRIX_SIZE; j++) {
          System.out.print(matrix[i][j].getSymbol() + " ");
        }
        System.out.println();
      }
      System.out.println("N=Neón, A=Agente, T=Teletransporte, #=Obstáculo");
      System.out.println("=====================================\n");
    } finally {
      matrixLock.unlock();
    }
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
    logger.info("Simulación detenida manualmente");
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
