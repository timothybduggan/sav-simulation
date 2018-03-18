package visualization;

import java.awt.Color;

/**
 * 
 * Converts JavaFX Color to Java.awt Color, and visa versa.
 * 
 * @author mercer
 *
 */

public class ColorTypeConverter {

//  public static void main(String[] args) {
//    javafx.scene.paint.Color fxColor = Awt2Fx(new Color(100, 150, 250 ));
//    System.out.println(fxColor.getRed());
//    System.out.println(fxColor.getBlue());
//    System.out.println(fxColor.getGreen());
//
//    java.awt.Color awtColor = Fx2Awt(fxColor);
//
//    System.out.println(awtColor.getRed());
//    System.out.println(awtColor.getBlue());
//    System.out.println(awtColor.getGreen());
//    
//    fxColor = Awt2Fx(awtColor);
// 
//    System.out.println(fxColor.getRed());
//    System.out.println(fxColor.getBlue());
//    System.out.println(fxColor.getGreen());
//
//    awtColor = Fx2Awt(fxColor);
//
//    System.out.println(awtColor.getRed());
//    System.out.println(awtColor.getBlue());
//    System.out.println(awtColor.getGreen());
//
//  }

  public static Color Fx2Awt(javafx.scene.paint.Color fxColor) {
    int r = (int) (255 * fxColor.getRed());
    int g = (int) (255 * fxColor.getGreen());
    int b = (int) (255 * fxColor.getBlue());
    int a = (int) (255 * fxColor.getOpacity());
    java.awt.Color awtColor = new java.awt.Color(r, g, b, a);
    return awtColor;
  }

  public static javafx.scene.paint.Color Awt2Fx(Color awtColor) {
    int r = awtColor.getRed();
    int g = awtColor.getGreen();
    int b = awtColor.getBlue();
    double a = awtColor.getAlpha()/255.0;
    javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(r, g, b, a); // , opacity); 
    return fxColor;
  }
}
