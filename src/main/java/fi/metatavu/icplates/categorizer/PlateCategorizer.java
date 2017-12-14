package fi.metatavu.icplates.categorizer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;

/**
 * Plate categorizer
 * 
 * @author Antti Leppä
 */
public class PlateCategorizer {
  
  private Logger logger = Logger.getLogger(PlateCategorizer.class.getName());
  private double scoreThreshold;
  private List<Template> templates;
  
  /**
   * Constructor
   * 
   * @param templatesFolder templates folder
   * @param scoreThreshold match threshold
   */
  public PlateCategorizer(String templatesFolder, double scoreThreshold) {
    this.templates = getTemplates(templatesFolder);
    this.scoreThreshold = scoreThreshold;
  }
  
  /**
   * Computes match for single plate
   * 
   * @param pdfFile pdf file
   * @param image pdf image
   * @return match
   */
  public PlateMatch getMatch(File pdfFile, GrayF32 image) {
    for (Template template : templates) {
      long start = System.currentTimeMillis();
      GrayF32 templateImage = template.getImage();

      List<Match> matches = getMatches(image, templateImage, 1).stream().filter(match -> match.score > scoreThreshold).collect(Collectors.toList());
      
      if (!matches.isEmpty()) {
        logger.log(Level.INFO, () -> String.format("%s: template %s matched. Matching took %d ms", pdfFile.getName(), template.getFile().getName(), (System.currentTimeMillis() - start)));
        return new PlateMatch(pdfFile, image, templateImage.width, templateImage.height, matches.get(0).score, matches);
      } else {
        logger.log(Level.INFO, () -> String.format("%s: template %s did not match. Matching took %d ms", pdfFile.getName(), template.getFile().getName(), (System.currentTimeMillis() - start)));
      }
    }
    
    logger.info("No matches found");
    
    return new PlateMatch(pdfFile, image, 0, 0, -100000d, Collections.emptyList());
  }
  
  private List<Template> getTemplates(String templatesFolder) {
    List<File> templateFiles = getTemplateFiles(templatesFolder);
    
    List<Template> result = new ArrayList<>(templateFiles.size());
    for (File templateFile : templateFiles) {
      GrayF32 templateImage = UtilImageIO.loadImage(templateFile.getParent(), templateFile.getName(), GrayF32.class);
      result.add(new Template(templateFile, templateImage));
    }
    
    logger.info(() -> String.format("Loaded %s templates", result.size()));
    
    return result;
  }
  
  private List<File> getTemplateFiles(String templatesFolder) {
    File templates = new File(templatesFolder);
    List<File> result = Arrays.asList(templates.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".png")));
    
    Collections.sort(result, (file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()));
    
    return result;
  }
  
  private List<Match> getMatches(GrayF32 image, GrayF32 template, int expectedMatches) {
    TemplateMatching<GrayF32> matcher = FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ, GrayF32.class);

    matcher.setImage(image);
    matcher.setTemplate(template, null, expectedMatches);
    matcher.process();
    
    return matcher.getResults().toList();
  }
  
  private class Template {
    
    private File file;
    private GrayF32 image;
    
    public Template(File file, GrayF32 image) {
      this.file = file;
      this.image = image;
    }
    
    public File getFile() {
      return file;
    }
    
    public GrayF32 getImage() {
      return image;
    }
    
  }
  
}
