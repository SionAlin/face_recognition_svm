package svm;

import java.lang.Math;
import java.io.*;

public class SVMClassifier implements Serializable{

    double gamma, coef0;
    double[][] supportVectors;
    int[] svLabels;
    double[] alphas;
    double b;
    public String personName;

    public SVMClassifier(double gamma, double coef0, String personName){
        this.gamma = gamma;
        this.coef0 = coef0;
        this.personName = personName;
    }

    public double kernel(double[] a, double[] b){
        double dot = 0.0;
        for(int i = 0; i < a.length; i++){
            dot += a[i] * b[i];
        }
        return Math.tanh(gamma * dot + coef0);
    }

    private double decisionFunction(double[] x){
        double sum = 0.0;
        for(int i = 0; i < alphas.length; i++){
            sum += alphas[i] * svLabels[i] * kernel(supportVectors[i], x);
        }
        return sum + b;
    }

    public int predict(double[] x){
        if(decisionFunction(x) >= 0)
            return 1;
        else
            return -1;
    }

    public void save(String path){
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(this);
            oos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static SVMClassifier load(String path){
        SVMClassifier clf = null;
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            clf = (SVMClassifier) ois.readObject();
            ois.close();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        return clf;
    }
}
