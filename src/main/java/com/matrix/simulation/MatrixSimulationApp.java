package com.matrix.simulation;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal que ejecuta la simulación de la matriz concurrente
 * Implementa el patrón Facade para simplificar la interacción
 */
public class MatrixSimulationApp implements SimulationObserver {

  private static final Logger logger = LoggerFactory.getLogger(
    MatrixSimulationApp.class
  );

  private MatrixSimulation simulation;
  private ExecutorService executorService;

  public static void main(String[] args) {
    MatrixSimulationApp app = new MatrixSimulationApp();
    app.startSimulation();
  }

  public void startSimulation() {
    printWelcomeMessage();

    // Crear simulación y agregar observer
    simulation = new MatrixSimulation();
    simulation.addObserver(this);

    // Mostrar estado inicial
    System.out.println("Estado inicial:");
    simulation.displayMatrix();

    // Configurar velocidades de movimiento (en milisegundos)
    int neonSpeed = 800; // Neón se mueve cada 800ms
    int agentSpeed = 1000; // Agentes se mueven cada 1000ms
    int displaySpeed = 500; // Actualizar pantalla cada 500ms

    // Crear pool de hilos
    executorService =
      Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      });

    // Crear y lanzar hilos de entidades
    NeonThread neonThread = new NeonThread(simulation, neonSpeed);
    executorService.submit(neonThread);

    // Crear hilos de agentes con diferentes velocidades
    for (int i = 0; i < simulation.getAgentPositions().size(); i++) {
      AgentThread agentThread = new AgentThread(
        simulation,
        i,
        agentSpeed + (i * 100)
      );
      executorService.submit(agentThread);
    }

    // Hilo para mostrar el estado de la matriz
    executorService.submit(() -> {
      while (simulation.isSimulationRunning()) {
        try {
          Thread.sleep(displaySpeed);
          simulation.displayMatrix();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });

    // Hilo para entrada del usuario
    executorService.submit(() -> {
      Scanner scanner = new Scanner(System.in);
      System.out.println(
        "Presiona ENTER en cualquier momento para terminar la simulación..."
      );
      scanner.nextLine();
      simulation.stopSimulation();
      scanner.close();
    });

    // Esperar a que termine la simulación
    waitForCompletion();

    // Mostrar estado final
    System.out.println("\n=== SIMULACIÓN TERMINADA ===");
    simulation.displayMatrix();

    System.out.println("¡Gracias por usar el simulador!");
    logger.info("Aplicación terminada correctamente");
  }

  private void waitForCompletion() {
    try {
      // Esperar hasta que la simulación termine
      while (simulation.isSimulationRunning()) {
        Thread.sleep(100);
      }

      // Dar tiempo para que todos los hilos terminen
      Thread.sleep(1000);

      // Cerrar el pool de hilos
      executorService.shutdown();
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        logger.warn("Algunos hilos no terminaron a tiempo, forzando cierre");
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.error("Simulación interrumpida", e);
      simulation.stopSimulation();
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private void printWelcomeMessage() {
    System.out.println("=".repeat(50));
    System.out.println("    SIMULACIÓN DE MATRIZ CONCURRENTE 8x8");
    System.out.println("=".repeat(50));
    System.out.println("Neón (N) intenta llegar al Teletransporte (T)");
    System.out.println("Los Agentes (A) intentan capturar al Neón");
    System.out.println("Obstáculos (#) bloquean el movimiento");
    System.out.println("=".repeat(50));
    System.out.println();
  }

  @Override
  public void onSimulationEvent(String message) {
    logger.info("Evento de simulación: {}", message);
    if (message.contains("GANADO") || message.contains("terminada")) {
      System.out.println("\n*** " + message + " ***");
    }
  }
}
