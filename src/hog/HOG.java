import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HOG{

    BufferedImage img;

    public static void main(String[] args){
             
        HOG hog = new HOG();
        hog.Read();
        hog.Write();

    }

    public void Read(){
        try{
            img = ImageIO.read(new File("/home/sion/Documents/Portofoliu/face_recognition_svm/resources/image.jpg"));
            System.out.println("Image uploaded!");
        }catch(IOException e){
            e.printStackTrace();
      }
    }

    public void Write(){
        try{
            File out = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/resources/image_saved.jpg");
            ImageIO.write(img, "png", out);
            System.out.println("Image saved!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

