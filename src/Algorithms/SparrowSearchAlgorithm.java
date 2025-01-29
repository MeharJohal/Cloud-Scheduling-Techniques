package Algorithms;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

public class SparrowSearchAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
        int numSparrows = 25; // Number of sparrows (solutions)
        int maxIterations = 100; // Number of iterations
        int numCloudlets = cloudletList.size();
        int numVms = vmList.size();

        double[][] positions = new double[numSparrows][numCloudlets];
        double[] fitness = new double[numSparrows];

        // Initialize sparrow positions randomly
        Random random = new Random();
        for (int i = 0; i < numSparrows; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                positions[i][j] = random.nextInt(numVms);
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Evaluate fitness for all sparrows
            for (int i = 0; i < numSparrows; i++) {
                fitness[i] = calculateFitness(positions[i], vmList, cloudletList);
            }

            // Find best and worst sparrows
            int bestIndex = findBestSparrow(fitness);
            int worstIndex = findWorstSparrow(fitness);
            double[] bestPosition = positions[bestIndex];

            // Update positions of sparrows
            for (int i = 0; i < numSparrows; i++) {
                for (int j = 0; j < numCloudlets; j++) {
                    if (random.nextDouble() < 0.8) {
                        // Followers update
                        positions[i][j] = bestPosition[j] + random.nextGaussian();
                    } else {
                        // Awareness update (avoid predators)
                        positions[i][j] = positions[i][j] + random.nextGaussian() * (positions[i][j] - positions[worstIndex][j]);
                    }
                    positions[i][j] = Math.abs(Math.round(positions[i][j])) % numVms; // Map to valid VM index
                }
            }
        }

        // Find the best solution
        int bestSparrowIndex = findBestSparrow(fitness);
        double[] bestSolution = positions[bestSparrowIndex];

        // Assign tasks based on the best solution
        for (int i = 0; i < numCloudlets; i++) {
            int vmIndex = (int) bestSolution[i];
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(vmIndex).getId());
        }
    }

    private static double calculateFitness(double[] position, List<Vm> vmList, List<Cloudlet> cloudletList) {
        double[] vmCompletionTimes = new double[vmList.size()];

        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = (int) position[i];
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

    private static int findBestSparrow(double[] fitness) {
        int bestIndex = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] < fitness[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static int findWorstSparrow(double[] fitness) {
        int worstIndex = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] > fitness[worstIndex]) {
                worstIndex = i;
            }
        }
        return worstIndex;
    }
}
