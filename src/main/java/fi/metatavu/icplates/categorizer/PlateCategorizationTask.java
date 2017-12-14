package fi.metatavu.icplates.categorizer;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import boofcv.struct.image.GrayF32;

/**
 * Task for processing plate categorization
 * 
 * @author Antti Leppä
 */
public class PlateCategorizationTask implements Callable<PlateMatch> {

  private Logger logger = Logger.getLogger(PlateCategorizationTask.class.getName());
  private PlateCategorizer plateCategorizer;
  private File pdfFile;
  private GrayF32 image;
  
  /**
   * Constructor 
   * 
   * @param plateCategorizer plate categorizer instance
   * @param pdfFile pdf file
   * @param image pdf image
   */
  public PlateCategorizationTask(PlateCategorizer plateCategorizer, File pdfFile, GrayF32 image) {
    super();
    this.plateCategorizer = plateCategorizer;
    this.pdfFile = pdfFile;
    this.image = image;
  }
  
  @Override
  public PlateMatch call() throws Exception {
    long start = System.currentTimeMillis();
    logger.info(() -> String.format("Categorizing: %s", pdfFile.getName()));
    
    try {
      return plateCategorizer.getMatch(pdfFile, image);
    } finally {
      logger.info(() -> String.format("Categorized: %s in %d s", pdfFile.getName(), (System.currentTimeMillis() - start) / 1000));
    }
  }

}
