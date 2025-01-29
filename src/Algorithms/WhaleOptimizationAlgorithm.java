package Algorithms;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;


public class WhaleOptimizationAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
        int numWhales = 30; // Number of whales (solutions)
        int maxIterations = 100; // Number of iterations
        int numCloudlets = cloudletList.size();
        int numVms = vmList.size();

        double[][] whales = new double[numWhales][numCloudlets];
        double[] fitness = new double[numWhales];

        // Initialize whales randomly
        Random random = new Random();
        for (int i = 0; i < numWhales; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                whales[i][j] = random.nextInt(numVms);
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Evaluate fitness of each whale
            for (int i = 0; i < numWhales; i++) {
                fitness[i] = calculateFitness(whales[i], vmList, cloudletList);
            }

            // Find the best whale (prey)
            int bestWhaleIndex = findBestWhale(fitness);
            double[] bestWhale = whales[bestWhaleIndex];

            // Update positions of whales
            for (int i = 0; i < numWhales; i++) {
                if (i == bestWhaleIndex) continue;

                for (int j = 0; j < numCloudlets; j++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();
                    double a = 2.0 - (2.0 * iter / maxIterations);
                    double c = 2.0 * r2;

                    if (r1 < 0.5) {
                        // Bubble-net attacking mechanism
                        double d = Math.abs(c * bestWhale[j] - whales[i][j]);
                        whales[i][j] = bestWhale[j] - a * d;
                    } else {
                        // Spiral updating position
                        double l = random.nextDouble() * 2 - 1; // Random number in [-1, 1]
                        double distance = Math.abs(bestWhale[j] - whales[i][j]);
                        whales[i][j] = distance * Math.exp(l) * Math.cos(2 * Math.PI * l) + bestWhale[j];
                    }
                    
                    whales[i][j] = Math.round(whales[i][j]);
                    whales[i][j] = Math.abs(Math.round(whales[i][j] % numVms)); // Map to valid VM index
                }
            }
        }

        // Assign tasks based on the best whale (prey)
        int bestWhaleIndex = findBestWhale(fitness);
        double[] bestSolution = whales[bestWhaleIndex];

        for (int i = 0; i < numCloudlets; i++) {
            int vmIndex = (int) bestSolution[i];
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(vmIndex).getId());
        }
    }

    private static double calculateFitness(double[] whale, List<Vm> vmList, List<Cloudlet> cloudletList) {
        double[] vmCompletionTimes = new double[vmList.size()];

        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = (int) whale[i];
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

    private static int findBestWhale(double[] fitness) {
        int bestIndex = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] < fitness[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
