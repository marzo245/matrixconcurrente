package com.matrix.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hilo que controla el movimiento del Neón hacia el teletransporte
 */
public class NeonThread extends Thread {

  private static final Logger logger = LoggerFactory.getLogger(
    NeonThread.class
  );

  private final MatrixSimulation simulation;
  private final int moveDelay;

  public NeonThread(MatrixSimulation simulation, int moveDelay) {
    this.simulation = simulation;
    this.moveDelay = moveDelay;
    this.setName("Neon-Thread");
    this.setDaemon(false);
  }

  @Override
  public void run() {
    logger.debug("Hilo del Neón iniciado con delay de {}ms", moveDelay);

    while (simulation.isSimulationRunning()) {
      try {
        simulation.moveNeon();
        Thread.sleep(moveDelay);
      } catch (InterruptedException e) {
        logger.debug("Hilo del Neón interrumpido");
        Thread.currentThread().interrupt();
        break;
      }
    }

    logger.debug("Hilo del Neón terminado");
  }
}
