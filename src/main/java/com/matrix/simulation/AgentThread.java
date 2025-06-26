package com.matrix.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hilo que controla el movimiento de un agente específico
 */
public class AgentThread extends Thread {

  private static final Logger logger = LoggerFactory.getLogger(
    AgentThread.class
  );

  private final MatrixSimulation simulation;
  private final int agentIndex;
  private final int moveDelay;

  public AgentThread(
    MatrixSimulation simulation,
    int agentIndex,
    int moveDelay
  ) {
    this.simulation = simulation;
    this.agentIndex = agentIndex;
    this.moveDelay = moveDelay;
    this.setName("Agent-" + (agentIndex + 1) + "-Thread");
    this.setDaemon(false);
  }

  @Override
  public void run() {
    logger.info(
      "Hilo del Agente {} iniciado con delay de {}ms",
      agentIndex + 1,
      moveDelay
    );

    while (simulation.isSimulationRunning()) {
      try {
        simulation.moveAgent(agentIndex);
        Thread.sleep(moveDelay);
      } catch (InterruptedException e) {
        logger.info("Hilo del Agente {} interrumpido", agentIndex + 1);
        Thread.currentThread().interrupt();
        break;
      }
    }

    logger.info("Hilo del Agente {} terminado", agentIndex + 1);
  }
}
