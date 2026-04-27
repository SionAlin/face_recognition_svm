package hog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;

public class HOG{

    private double[][] toGrayScale(BufferedImage img){
        try{
            double[][] grayImg;
            int rgb, R,G,B;
            int width, height;

            if(img == null)
                throw new Exception("Image not found!");

            width = img.getWidth();
            height = img.getHeight();

            grayImg = new double[height][width];

            for(int i = 0; i < height; i++){
                for(int j = 0; j < width; j++){
                    rgb = img.getRGB(j,i);
                    R = (rgb >> 16) & 0xff;
                    G = (rgb >> 8) & 0xff;
                    B = rgb & 0xff;

                    grayImg[i][j] = 0.299*R + 0.587*G + 0.114*B;
                } 
            }

            return grayImg;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private double[][] computeGradientMag(double[][] grayImg){
        try{
            if(grayImg == null)
                throw new Exception("Image not found!");

            double[][] magnitude;
            int width, height;
            double Gx, Gy;

            height = grayImg.length;
            width = grayImg[0].length;
            magnitude = new double[height][width];

            for(int i = 1; i < height - 1; i++){
                for(int j = 1; j < width - 1; j++){
                    Gx = grayImg[i][j+1] - grayImg[i][j-1];
                    Gy = grayImg[i+1][j] - grayImg[i-1][j];

                    magnitude[i][j] = Math.sqrt(Gx*Gx + Gy*Gy);
                }
            }

            return magnitude;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private double[][] computeGradientOri(double[][] grayImg){
        try{
            if(grayImg == null)
                throw new Exception("Image not found!");

            double[][] orientation;
            int width, height;
            double Gx, Gy;
            double angle;

            height = grayImg.length;
            width = grayImg[0].length;
            orientation = new double[height][width];

            for(int i = 1; i < height - 1; i++){
                for(int j = 1; j < width - 1; j++){
                    Gx = grayImg[i][j+1] - grayImg[i][j-1];
                    Gy = grayImg[i+1][j] - grayImg[i-1][j];

                    angle = Math.atan2(Gy, Gx);
                    if(angle < 0)
                        angle += Math.PI;
                    orientation[i][j] = angle;
                }
            }

            return orientation;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public double[] computeHOG(BufferedImage img){
        try{
            int bin;
            int index1 = 0;
            int index2 = 0;
            int width = img.getWidth();
            int numCellsX = width / 8;
            int height = img.getHeight();
            int numCellsY = height / 8;

            double[] hist = new double[9];
            double[] hogVector = new double[(numCellsY - 1) * (numCellsX - 1) * 36];
            double[] block = new double[36];
            
            double[][] grayImg = toGrayScale(img);
            double[][] magnitude = computeGradientMag(grayImg);
            double[][] orientation = computeGradientOri(grayImg);
  
            double[][][] cells = new double[numCellsY][numCellsX][9];

            for(int i = 0; i < numCellsY; i++){
                for(int j = 0; j < numCellsX; j++){

                    hist = new double[9];
                    for(int k = 0; k < 8; k++){
                        for(int h = 0; h < 8; h++){
                            bin = (int)(orientation[k + (i * 8)][h + (j * 8)] / (Math.PI / 9));
                            bin = Math.min(8, Math.max(0, bin));
                            hist[bin] += magnitude[k + (i * 8)][h + (j * 8)];
                        }
                    }
                    cells[i][j] = hist;
                }
            }

            for(int i = 0; i < numCellsY - 1; i++){
                for(int j = 0; j < numCellsX - 1; j++){
                    block = new double[36];
                    index1 = 0; 
                    for(int dy = 0; dy < 2; dy++){
                        for(int dx = 0; dx < 2; dx++){
                            for(int k = 0; k < 9; k++){
                                block[index1++] = cells[i + dy][j + dx][k];
                            }
                        }
                    }

                    double norm = 0.0;
                    for(double v : block)
                        norm += v*v;

                    norm = Math.sqrt(norm + 1e-6);

                    for(int k = 0; k < 36; k++){
                        hogVector[index2++] = block[k] / norm;
                    }
                }
            }

            return hogVector;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage toBufferedImage(double[][] grayImg){
        int height = grayImg.length;
        int width = grayImg[0].length;

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int val = (int) grayImg[i][j];
                val = Math.max(0, Math.min(255, val));
                int pixel = (val << 16) | (val << 8) | val;
                out.setRGB(j,i,pixel);
            }
        }
        return out;
    }

    public BufferedImage Read(){
        try{
            BufferedImage img = ImageIO.read(new File("/home/sion/Documents/Portofoliu/face_recognition_svm/resources/image.jpg"));
            System.out.println("Image uploaded!");
            return img;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void Write(BufferedImage aux, String nume){
        try{
            File out = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/resources/" + nume + ".jpg");
            ImageIO.write(aux, "png", out);
            System.out.println("Image saved!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

