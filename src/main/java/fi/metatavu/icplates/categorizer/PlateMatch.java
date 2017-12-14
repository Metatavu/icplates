package fi.metatavu.icplates.categorizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;

/**
 * Plate categorization match object
 * 
 * @author Antti Leppä
 */
public class PlateMatch {
  
  private File pdfFile;
  private GrayF32 image;
  private Double score;
  private List<Match> matches;
  private int templateWidth;
  private int templateHeight;
  
  /**
   * Constructor
   * 
   * @param pdfFile pdf file
   * @param image pdf image
   * @param templateWidth matched template width. Use -1 when no templates matched
   * @param templateHeight matched template height. Use -1 when no templates matched
   * @param score score of match
   * @param matches list of matches
   */
  public PlateMatch(File pdfFile, GrayF32 image, int templateWidth, int templateHeight, Double score, List<Match> matches) {
    this.pdfFile = pdfFile;
    this.image = image;
    this.templateWidth = templateWidth;
    this.templateHeight = templateHeight;
    this.score = score;
    this.matches = matches;
  }
  
  /**
   * Returns score 
   * 
   * @return score
   */
  public Double getScore() {
    return score;
  }
  
  /**
   * Returns matches
   * 
   * @return matches
   */
  public List<Match> getMatches() {
    return matches;
  }
  
  /**
   * Returns pdf file
   * 
   * @return pdf file
   */
  public File getPdfFile() {
    return pdfFile;
  }
  
  /**
   * Returns pdf image
   * 
   * @return pdf image
   */
  public GrayF32 getImage() {
    return image;
  }
  
  /**
   * Visualizes matches into a buffered image
   * 
   * @param output target image
   */
  public void visualize(BufferedImage output) {
    int stroke = 2;
    int width = templateWidth + 2 * stroke;
    int height = templateHeight + 2 * stroke;
    
    Graphics2D graphics = (Graphics2D) output.getGraphics();
    try {
      graphics.setColor(Color.BLUE); 
      graphics.setStroke(new BasicStroke(stroke));

      for (Match match : getMatches()) {
        int x0 = match.x - stroke;
        int y0 = match.y - stroke;
        int x1 = x0 + width;
        int y1 = y0 + height;
  
        graphics.drawLine(x0, y0, x1, y0);
        graphics.drawLine(x1, y0, x1, y1);
        graphics.drawLine(x1, y1, x0, y1);
        graphics.drawLine(x0, y1, x0, y0);
      }
    } finally {
      graphics.dispose();
    }
  }

  /**
   * Visualizes matches into image and stores it into a file
   * 
   * @param outputFolder output folder where the image is stored
   */
  public void visualize(File outputFolder) {
    BufferedImage outputImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
    ConvertBufferedImage.convertTo(image, outputImage);
    visualize(outputImage);
    String imageName = pdfFile.getName().replaceAll(".pdf", ".match.png");
    UtilImageIO.saveImage(outputImage, new File(outputFolder, imageName).getAbsolutePath());
  }
}
