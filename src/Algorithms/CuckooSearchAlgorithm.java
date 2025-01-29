package Algorithms;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;


public class CuckooSearchAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
        int numNests = 25; // Number of nests (solutions)
        int maxIterations = 100; // Number of iterations
        double pa = 0.25; // Discovery rate of alien eggs
        int numCloudlets = cloudletList.size();
        int numVms = vmList.size();

        double[][] nests = new double[numNests][numCloudlets];
        double[] fitness = new double[numNests];

        // Initialize nests randomly
        Random random = new Random();
        for (int i = 0; i < numNests; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                nests[i][j] = random.nextInt(numVms);
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Generate new solutions (cuckoo eggs)
            double[][] newNests = new double[numNests][numCloudlets];
            for (int i = 0; i < numNests; i++) {
                for (int j = 0; j < numCloudlets; j++) {
                    newNests[i][j] = nests[i][j] + random.nextGaussian();
                    newNests[i][j] = Math.abs(Math.round(newNests[i][j])) % numVms; // Map to valid VM index
                }
            }

            // Evaluate fitness of both nests and new nests
            for (int i = 0; i < numNests; i++) {
                double newFitness = calculateFitness(newNests[i], vmList, cloudletList);
                if (newFitness < fitness[i]) {
                    nests[i] = newNests[i];
                    fitness[i] = newFitness;
                }
            }

            // Abandon some nests with probability pa
            for (int i = 0; i < numNests; i++) {
                if (random.nextDouble() < pa) {
                    for (int j = 0; j < numCloudlets; j++) {
                        nests[i][j] = random.nextInt(numVms);
                    }
                    fitness[i] = calculateFitness(nests[i], vmList, cloudletList);
                }
            }
        }

        // Find the best solution
        int bestNestIndex = findBestNest(fitness);
        double[] bestSolution = nests[bestNestIndex];

        // Assign tasks based on the best solution
        for (int i = 0; i < numCloudlets; i++) {
            int vmIndex = (int) bestSolution[i];
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(vmIndex).getId());
        }
    }

    private static double calculateFitness(double[] nest, List<Vm> vmList, List<Cloudlet> cloudletList) {
        double[] vmCompletionTimes = new double[vmList.size()];

        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = (int) nest[i];
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

    private static int findBestNest(double[] fitness) {
        int bestIndex = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] < fitness[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
