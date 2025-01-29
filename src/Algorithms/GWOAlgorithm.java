package Algorithms;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GWOAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
        int numWolves = 30; // Number of wolves (solutions)
        int maxIterations = 100; // Number of iterations
        int numCloudlets = cloudletList.size();
        int numVms = vmList.size();

        double[][] wolves = new double[numWolves][numCloudlets];
        double[] fitness = new double[numWolves];

        // Initialize wolves randomly
        Random random = new Random();
        for (int i = 0; i < numWolves; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                wolves[i][j] = random.nextInt(numVms);
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Evaluate fitness of each wolf
            for (int i = 0; i < numWolves; i++) {
                fitness[i] = calculateFitness(wolves[i], vmList, cloudletList);
            }

            // Find alpha, beta, and delta wolves
            int alphaIndex = findBestWolf(fitness);
            int betaIndex = findSecondBestWolf(fitness, alphaIndex);
            int deltaIndex = findThirdBestWolf(fitness, alphaIndex, betaIndex);

            double[] alphaWolf = wolves[alphaIndex];
            double[] betaWolf = wolves[betaIndex];
            double[] deltaWolf = wolves[deltaIndex];

            // Update positions of wolves
            for (int i = 0; i < numWolves; i++) {
                if (i == alphaIndex || i == betaIndex || i == deltaIndex) continue;

                for (int j = 0; j < numCloudlets; j++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();

                    double a1 = 2 * r1 - 1;
                    double c1 = 2 * r2;

                    double dAlpha = Math.abs(c1 * alphaWolf[j] - wolves[i][j]);
                    double x1 = alphaWolf[j] - a1 * dAlpha;

                    r1 = random.nextDouble();
                    r2 = random.nextDouble();

                    double a2 = 2 * r1 - 1;
                    double c2 = 2 * r2;

                    double dBeta = Math.abs(c2 * betaWolf[j] - wolves[i][j]);
                    double x2 = betaWolf[j] - a2 * dBeta;

                    r1 = random.nextDouble();
                    r2 = random.nextDouble();

                    double a3 = 2 * r1 - 1;
                    double c3 = 2 * r2;

                    double dDelta = Math.abs(c3 * deltaWolf[j] - wolves[i][j]);
                    double x3 = deltaWolf[j] - a3 * dDelta;

                    wolves[i][j] = (x1 + x2 + x3) / 3;
                    wolves[i][j] = Math.round(wolves[i][j]);
                    wolves[i][j] = Math.abs(Math.round(wolves[i][j] % numVms)); // Map to valid VM index
                }
            }
        }

        // Assign tasks based on the best wolf (alpha)
        int bestWolfIndex = findBestWolf(fitness);
        double[] bestSolution = wolves[bestWolfIndex];

        for (int i = 0; i < numCloudlets; i++) {
            int vmIndex = (int) bestSolution[i];
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(vmIndex).getId());
        }
    }

    private static double calculateFitness(double[] wolf, List<Vm> vmList, List<Cloudlet> cloudletList) {
        double[] vmCompletionTimes = new double[vmList.size()];

        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = (int) wolf[i];
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(vmIndex);

            double executionTime = cloudlet.getCloudletTotalLength() / vm.getMips();
            vmCompletionTimes[vmIndex] += executionTime;
        }

        double maxCompletionTime = 0;
        for (double time : vmCompletionTimes) {
            maxCompletionTime = Math.max(maxCompletionTime, time);
        }

        return maxCompletionTime; // Minimize max completion time
    }

    private static int findBestWolf(double[] fitness) {
        int bestIndex = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] < fitness[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static int findSecondBestWolf(double[] fitness, int bestIndex) {
        int secondBestIndex = -1;
        for (int i = 0; i < fitness.length; i++) {
            if (i != bestIndex && (secondBestIndex == -1 || fitness[i] < fitness[secondBestIndex])) {
                secondBestIndex = i;
            }
        }
        return secondBestIndex;
    }

    private static int findThirdBestWolf(double[] fitness, int bestIndex, int secondBestIndex) {
        int thirdBestIndex = -1;
        for (int i = 0; i < fitness.length; i++) {
            if (i != bestIndex && i != secondBestIndex && (thirdBestIndex == -1 || fitness[i] < fitness[thirdBestIndex])) {
                thirdBestIndex = i;
            }
        }
        return thirdBestIndex;
    }
}
