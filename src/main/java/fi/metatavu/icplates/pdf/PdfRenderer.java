package fi.metatavu.icplates.pdf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * PDF Renderer
 * 
 * @author Antti Leppä
 */
public class PdfRenderer {
  
  private boolean transparentize;
  private boolean allPages;
  
  public PdfRenderer(boolean allPages, boolean transparentize) {
    this.allPages = allPages;
    this.transparentize = transparentize;
  }

  /**
   * Renders PDF into BufferedImage
   * 
   * @param file pdf file
   * @return Rendered image
   * @throws IOException throw if file reading fails
   */
  public BufferedImage renderPdf(File file) throws IOException {
    PDDocument document = PDDocument.load(file);
    PDFRenderer pdfRenderer = new PDFRenderer(document);
    List<BufferedImage> pages = new ArrayList<>();
    
    if (allPages) {
      for (int page = 0; page < document.getNumberOfPages(); ++page) { 
        pages.add(pdfRenderer.renderImageWithDPI(page, 300, ImageType.ARGB));
      }
    } else {
      pages.add(pdfRenderer.renderImageWithDPI(0, 300, ImageType.ARGB));
    }

    int targetHeight = 0;
    int targetWidth = 0;
    
    for (BufferedImage bufferedImage : pages) {
      targetHeight = Math.max(bufferedImage.getHeight(), targetHeight);
      targetWidth += bufferedImage.getWidth();
    }
    
    BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics graphics = targetImage.getGraphics();
    
    int offset = 0;
    for (BufferedImage page : pages) {
      graphics.drawImage(page, offset, 0, null);
      offset += page.getWidth();
    }
    
    if (transparentize) {
      colorToAlpha(targetImage, Color.WHITE);
    }
    
    document.close();
    
    return targetImage;
  }
  
  private void colorToAlpha(BufferedImage image, Color remove) {
    int width = image.getWidth();
    int height = image.getHeight();
    int[] pixels = new int[width * height];
    
    image.getRGB(0, 0, width, height, pixels, 0, width);
    for (int i = 0; i < pixels.length; i++) {
      if (pixels[i] == remove.getRGB()) {
        pixels[i] = 0x00ffffff;
      }
    }
    
    image.setRGB(0, 0, width, height, pixels, 0, width);
  } 
  
}
