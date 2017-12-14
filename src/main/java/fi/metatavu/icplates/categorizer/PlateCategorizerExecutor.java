package fi.metatavu.icplates.categorizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous executor for plate categorize tasks
 * 
 * @author Antti Leppä
 */
public class PlateCategorizerExecutor {

  private Logger logger = Logger.getLogger(PlateCategorizerExecutor.class.getName());
  private int poolSize;

  /**
   * Constructor
   * 
   * @param poolSize thread count
   */
  public PlateCategorizerExecutor(int poolSize) {
    this.poolSize = poolSize;
  }

  /**
   * Executes set of categorization tasks
   * 
   * @param tasks tasks
   * @return matches
   */
  public List<PlateMatch> categorize(List<PlateCategorizationTask> tasks) {
    List<PlateMatch> result = new ArrayList<>();
    
    ExecutorService executor = Executors.newFixedThreadPool(poolSize);
    List<Future<PlateMatch>> futures = new ArrayList<>(tasks.size());
    
    for (PlateCategorizationTask task : tasks) {
      Future<PlateMatch> future = executor.submit(task);
      futures.add(future);
    }
    
    for (Future<PlateMatch> future : futures) {
      try {
        result.add(future.get());
      } catch (InterruptedException | ExecutionException e) {
        logger.log(Level.SEVERE, "PlateMatch future interrupted", e);
      }
    }
    
    return result;
  }
  
}
