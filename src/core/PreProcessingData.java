import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class PreProcessingData{
    public static void main(String[] args){
        try{
            File inputDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/Humans/");
            File outputDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/faces/");
            outputDir.mkdirs();

            File[] files = inputDir.listFiles(File::isFile);
            BufferedImage img;
            int count = 0;

            for(File person : files){
                try{
                    img = ImageIO.read(person);
                    if(img == null) continue;
                    BufferedImage scaled = rescale(img);
                    ImageIO.write(scaled, "jpg", new File(outputDir + "/face_" + count + ".jpg"));
                    count++;
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            System.out.println("Done! " + count + " humans images processed.");

            inputDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/archive/");
            outputDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/non_faces/");
            outputDir.mkdirs();

            files = inputDir.listFiles(File::isFile);
            count = 0;
            for(File landscape : files){
                try{
                    img = ImageIO.read(landscape);
                    if(img == null) continue;
                    BufferedImage scaled = rescale(img);
                    ImageIO.write(scaled, "jpg", new File(outputDir + "/non_face_" + count + ".jpg"));
                    count++;
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            System.out.println("Done! " + count + " landscape images processed.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static BufferedImage rescale(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        double scaleX = width / 128.0;
        double scaleY = height / 128.0;
        int srcX, srcY;
        BufferedImage newImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < 128; i++){
            for(int j = 0; j < 128; j++){
                srcX = (int)(j * scaleX);
                srcY = (int)(i * scaleY);
                newImage.setRGB(j, i, image.getRGB(srcX, srcY));
            }
        }
        return newImage;
    }
}
