package es.jyago.hermes.simulator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class RealTimeSimulator implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(RealTimeSimulator.class.getName());
    
    private static ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        createExecutor();
        LOG.log(Level.INFO, "contextInitialized() - Creado el simulador en tiempo real");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
        LOG.log(Level.INFO, "contextInitialized() - Eliminado el simulador en tiempo real");
    }

    public static synchronized void submitTask(Runnable runnable) {
        if (executor == null) {
            createExecutor();
        }
        executor.submit(runnable);
    }

    public static synchronized Future<String> submitTask(Callable callable) {
        if (executor == null) {
            createExecutor();
        }
        return executor.submit(callable);
    }

    static void createExecutor() {
        executor = new ThreadPoolExecutor(
                1, // Tendremos siempre un hilo activo como mínimo.
                100, // Número máximo de hilos (pool).
                0L, // Tiempo máximo que esperarán los nuevos hilos que lleguen, si todos los hilos del 'pool' están ocupados.
                TimeUnit.MILLISECONDS, // Unidad de medida para el tiempo de espera máximo.
                new LinkedBlockingQueue<>()); // La cola que se usará para almacenar los hilos antes de ser ejecutados, para resolver el problema productor-consumidor a distintas velocidades.
    }
}
