package fi.metatavu.icplates;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.math.NumberUtils;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import fi.metatavu.icplates.categorizer.PlateCategorizationTask;
import fi.metatavu.icplates.categorizer.PlateCategorizer;
import fi.metatavu.icplates.categorizer.PlateCategorizerExecutor;
import fi.metatavu.icplates.categorizer.PlateMatch;
import fi.metatavu.icplates.pdf.PdfRenderer;

/**
 * Main class
 * 
 * @author Antti Leppä
 */
public class Main {
  
  private static Logger logger = Logger.getLogger(Main.class.getName());
  
  private static final String OPTION_TEMPLATES_FOLDER = "templates-folder";
  private static final String OPTION_PRINT_IMAGES = "print-images";
  private static final String OPTION_THREAD_COUNT = "thrad-count";
  private static final String OPTION_SCORE_THRESHOLD = "scrore-threshold";
  private static final String OPTION_HELP = "help";
  
  private Main() {
  }
  
  /**
   * Main method
   * 
   * @param args
   */
  public static void main(String[] args) {
    CommandLine options = handleOptions(args);
    if (options == null) {
      System.exit(-1);
    }

    String templatesFolder = options.getOptionValue(OPTION_TEMPLATES_FOLDER);
    int threadCount = NumberUtils.createInteger(options.getOptionValue(OPTION_THREAD_COUNT, "3"));
    double scoreThreshold = NumberUtils.createDouble(options.getOptionValue(OPTION_SCORE_THRESHOLD, "-1000"));
    
    List<File> pdfFiles = options.getArgList().stream()
      .map(File::new)
      .collect(Collectors.toList());
    
    PlateCategorizer plateCategorizer  = new PlateCategorizer(templatesFolder, scoreThreshold);
    PlateCategorizerExecutor categorizerExecutor = new PlateCategorizerExecutor(threadCount);

    try {
      PdfRenderer pdfRenderer = new PdfRenderer(false, true);
      List<PlateCategorizationTask> categorizationTasks = new ArrayList<>();
      
      for (File pdfFile : pdfFiles) {
        BufferedImage bufferedImage = pdfRenderer.renderPdf(pdfFile);
        GrayF32 image = ConvertBufferedImage.convertFrom(bufferedImage, (GrayF32) null);
        PlateCategorizationTask categorizationTask = new PlateCategorizationTask(plateCategorizer, pdfFile, image);
        categorizationTasks.add(categorizationTask);
      }
      
      List<PlateMatch> matches = categorizerExecutor.categorize(categorizationTasks);
      for (PlateMatch match : matches) {
        logger.info(() -> String.format("%s %s (%f)", match.getPdfFile().getName(), match.getScore() > scoreThreshold ? "matched" : "did not match", match.getScore()));
        if (options.hasOption(OPTION_PRINT_IMAGES)) {
          match.visualize(new File(options.getOptionValue(OPTION_PRINT_IMAGES)));
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Pdf image rendering failed", e);
      System.exit(-1);
    }
   
    System.exit(0);
  }

  private static CommandLine handleOptions(String[] args) {
    Options options = new Options();
    
    options.addOption(createOption(true, "t", OPTION_TEMPLATES_FOLDER, true, "Templates folder"));
    options.addOption(createOption(false, "c", OPTION_THREAD_COUNT, true, "Thread count used for analyzing. Defaults to 3"));
    options.addOption(createOption(false, "s", OPTION_SCORE_THRESHOLD, true, "Score threshold (0 is prefect match). Defaults to -1000"));
    options.addOption(createOption(false, "i", OPTION_PRINT_IMAGES, true, "Print visualization of discovered patterns into specified folder."));
    options.addOption(createOption(false, "h", OPTION_HELP, false, "Prints help"));

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine commandLine = parser.parse(options, args);
      if (commandLine.hasOption(OPTION_HELP)) {
        printHelp(options);
        return null;
      }

      return commandLine;
    } catch (ParseException e) {
      printHelp(options);
    }

    return null;
  }

  private static void printHelp(Options options) {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("java -jar icplates.jar", options);
  }
  
  private static Option createOption(boolean required, String opt, String longOpt, boolean hasArg, String description) {
    Option option = new Option(opt, longOpt, hasArg, description);
    option.setRequired(required);
    return option;
  }

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
  }
}
