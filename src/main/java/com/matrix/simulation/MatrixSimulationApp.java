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

    // Forzar visualización simple sin animación
    simulation.setAnimatedDisplay(false);

    // Mostrar estado inicial
    System.out.println("Estado inicial:");
    simulation.displayMatrix();

    // Dar tiempo al usuario para ver el estado inicial
    System.out.println("La simulación comenzará en 3 segundos...");
    System.out.println(
      "MODO: Turnos sincronizados - todos se mueven, luego se muestra"
    );
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // NUEVA LÓGICA: Simulación por turnos sincronizados
    runTurnBasedSimulation();
  }

  /**
   * Ejecuta la simulación por turnos sincronizados sin pausas de tiempo
   */
  private void runTurnBasedSimulation() {
    int turnNumber = 1;

    System.out.println("\n🎮 INICIANDO SIMULACIÓN POR TURNOS SINCRONIZADOS 🎮");
    System.out.println(
      "Cada turno: Neón se mueve → Agentes se mueven → Se muestra resultado"
    );
    System.out.println("Presiona Ctrl+C para detener\n");

    // Hilo para detectar entrada del usuario (opcional)
    Thread inputThread = new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine(); // Esperar ENTER
      simulation.stopSimulation();
      scanner.close();
    });
    inputThread.setDaemon(true);
    inputThread.start();

    while (simulation.isSimulationRunning()) {
      System.out.println("┌" + "─".repeat(48) + "┐");
      System.out.println(
        "│🔄 TURNO #" + String.format("%-38s", turnNumber) + "│"
      );
      System.out.println("└" + "─".repeat(48) + "┘");

      // 1. Mover el Neón
      System.out.println("🔵 Neón hace su movimiento...");
      simulation.moveNeon();

      if (!simulation.isSimulationRunning()) {
        System.out.println("🎯 ¡El Neón llegó al Teletransporte!");
        break; // El neón llegó al teletransporte
      }

      // 2. Mover todos los agentes uno por uno
      System.out.println("🔴 Agentes hacen sus movimientos...");
      for (int i = 0; i < simulation.getAgentPositions().size(); i++) {
        System.out.println("   → Agente " + (i + 1) + " se mueve");
        simulation.moveAgent(i);
        if (!simulation.isSimulationRunning()) {
          System.out.println("💥 ¡Agente " + (i + 1) + " capturó al Neón!");
          break; // Un agente capturó al neón
        }
      }

      if (!simulation.isSimulationRunning()) {
        break;
      }

      // 3. Mostrar estado actualizado después de que todos se movieron
      System.out.println("📊 Resultado del turno " + turnNumber + ":");
      simulation.displayMatrix();

      turnNumber++;

      // 4. Pausa de 5 segundos entre turnos para mejor visualización
      if (simulation.isSimulationRunning()) {
        System.out.println(
          "⏱️  Esperando 5 segundos hasta el próximo turno..."
        );
        System.out.println("    (Presiona Ctrl+C para terminar)");
        try {
          Thread.sleep(5000); // 5 segundos de pausa
        } catch (InterruptedException e) {
          System.out.println("\n⚠️  Simulación interrumpida por el usuario");
          simulation.stopSimulation();
          Thread.currentThread().interrupt();
          break;
        }
      }

      System.out.println(); // Línea en blanco para separar turnos
    }

    // Mostrar estado final
    System.out.println("\n" + "═".repeat(60));
    System.out.println(
      "🏁 SIMULACIÓN TERMINADA DESPUÉS DE " + (turnNumber - 1) + " TURNOS"
    );
    System.out.println("═".repeat(60));
    simulation.displayMatrix();

    logger.info(
      "Aplicación terminada correctamente después de {} turnos",
      turnNumber - 1
    );
  }

  // Ya no necesitamos waitForCompletion() en modo por turnos
  // private void waitForCompletion() { ... }

  private void printWelcomeMessage() {
    System.out.println("=".repeat(50));
    System.out.println("    SIMULACIÓN DE MATRIZ CONCURRENTE 12x12");
    System.out.println("=".repeat(50));
    System.out.println("Neón (N) intenta llegar al Teletransporte (T)");
    System.out.println("Los Agentes (A) intentan capturar al Neón");
    System.out.println("Obstáculos (#) bloquean el movimiento");
    System.out.println("=".repeat(50));
    System.out.println();
  }

  @Override
  public void onSimulationEvent(String message) {
    logger.debug("Evento de simulación: {}", message);
    if (message.contains("GANADO") || message.contains("terminada")) {
      System.out.println("\n*** " + message + " ***");
    }
  }
}
