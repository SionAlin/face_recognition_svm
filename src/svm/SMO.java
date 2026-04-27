package svm;

import java.util.Random; 

public class SMO{

    SVMClassifier classifier;
    double C, tol, gamma, coef0;
    int maxIter;
    double[][] x;
    int[] y;
    double[] alpha;
    double b;
    Random rand;

    public SMO(double C, double tol, int maxIter, double gamma, double coef0){
        this.C = C;
        this.tol = tol;
        this.maxIter = maxIter;
        this.gamma = gamma;
        this.coef0 = coef0;
        this.rand = new Random();
    }

    public SVMClassifier train(double[][] x, int[] y, String personName){
        this.x = x;
        this.y = y;

        int numChanged, k = 0;
        int n = y.length;
        int svCount;
        classifier = new SVMClassifier(gamma, coef0, personName);
        alpha = new double[n];
        b = 0;

        while(k < maxIter){
            numChanged = 0;
            for(int i = 0; i < n; i++){
                if(examineExample(i))
                    numChanged++;
            }
            if(numChanged == 0) break;
            k++;
        }

        svCount = 0;
        for(double a: alpha) if(a > 1e-8) svCount++;

        double[][] sv = new double[svCount][];
        int[] svY = new int[svCount];
        double[] svA = new double[svCount];

        int idx = 0;
        for(int i = 0; i < n; i++){
            if(alpha[i] > 1e-8){
                sv[idx] = x[i];
                svY[idx] = y[i];
                svA[idx] = alpha[i];
                idx++;
            }
        }

        classifier.supportVectors = sv;
        classifier.svLabels = svY;
        classifier.alphas = svA;
        classifier.b = b;

        return classifier;
    }

    private boolean examineExample(int i){
        /* KKT
         * if alpha[i] = 0 -> y[i] * decisionFunction(x[i]) >= 1
         * if alpha[i] = C -> y[i] * decisionFunction(x[i]) <= 1
         * if 0 < alpha[i] < C -> y[i] * decisionFunction(x[i]) = 1
         */

        double Ei = decisionFunction(i) - y[i];
        double ri = Ei * y[i];
        boolean kktVerif = false;
        if((ri < -tol && alpha[i] < C) || (ri > tol && alpha[i] > 0)){
            kktVerif = true;
        }

        int j = -1;
        j = rand.nextInt(x.length);
        while(j == i) j = rand.nextInt(x.length);

        if(kktVerif == true){
            optimizeStep(i, j);
            return true;
        }
        return false;
    }

    private void optimizeStep(int i, int j){
        try{
            double Ei = decisionFunction(i) - y[i];
            double Ej = decisionFunction(j) - y[j];
            double L, H, eta;

            double oldAlphaI, oldAlphaJ;
            double b1, b2;
            double dAi, dAj;

            if(y[i] == y[j]){
                L = Math.max(0, alpha[j] + alpha[i] - C);
                H = Math.min(C, alpha[j] + alpha[i]);
            }else{
                L = Math.max(0, alpha[j] - alpha[i]);
                H = Math.min(C, C + alpha[j] - alpha[i]);
            }

            if(L < H){
                eta = kernelValue(i,i) +  kernelValue(j,j) - 2 * kernelValue(i,j);
                if(eta == 0)
                    throw new Exception("eta is 0");
 
                oldAlphaI = alpha[i];
                oldAlphaJ = alpha[j];

                alpha[j] = alpha[j] + y[j] * (Ei - Ej) / eta;
                alpha[j] = Math.min(H, Math.max(L, alpha[j]));

                if(Math.abs(alpha[j] - oldAlphaJ) < 1e-5) return;

                alpha[i] = alpha[i] + y[i] * y[j] * (oldAlphaJ - alpha[j]);

                dAi = alpha[i] - oldAlphaI;
                dAj = alpha[j] - oldAlphaJ;

                b1 = b - Ei - y[i]*dAi*kernelValue(i,i) - y[j]*dAj*kernelValue(i,j);
                b2 = b - Ej - y[i]*dAi*kernelValue(i,j) - y[j]*dAj*kernelValue(j,j);

                if(alpha[i] < C && 0 < alpha[i]) b = b1;
                else if(alpha[j] < C && 0 < alpha[j]) b = b2;
                else b = (b1 + b2) / 2;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private double decisionFunction(int i){
        double sum = 0.0;
        for(int k = 0; k < alpha.length; k++){
            if(alpha[k] == 0) continue;
            sum += alpha[k] * y[k] * kernelValue(k, i);
        }
        return sum + b;
    }

    private double kernelValue(int i, int j){
        return classifier.kernel(x[i],x[j]);
    }

}
