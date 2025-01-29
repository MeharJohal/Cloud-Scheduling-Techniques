package Algorithms;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

public class ParticleSwarmOptimizationAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
        int swarmSize = 30; // Number of particles
        int maxIterations = 100; // Number of iterations
        int numCloudlets = cloudletList.size();
        int numVms = vmList.size();

        double[][] positions = new double[swarmSize][numCloudlets];
        double[][] velocities = new double[swarmSize][numCloudlets];
        double[][] personalBestPositions = new double[swarmSize][numCloudlets];
        double[] personalBestFitness = new double[swarmSize];

        double[] globalBestPosition = new double[numCloudlets];
        double globalBestFitness = Double.MAX_VALUE;

        Random random = new Random();

        // Initialize particles
        for (int i = 0; i < swarmSize; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                positions[i][j] = random.nextInt(numVms);
                velocities[i][j] = random.nextDouble();
                personalBestPositions[i][j] = positions[i][j];
            }
            personalBestFitness[i] = calculateFitness(personalBestPositions[i], vmList, cloudletList);

            if (personalBestFitness[i] < globalBestFitness) {
                globalBestFitness = personalBestFitness[i];
                System.arraycopy(personalBestPositions[i], 0, globalBestPosition, 0, numCloudlets);
            }
        }

        // PSO main loop
        for (int iter = 0; iter < maxIterations; iter++) {
            for (int i = 0; i < swarmSize; i++) {
                for (int j = 0; j < numCloudlets; j++) {
                    // Update velocity
                    velocities[i][j] = 0.5 * velocities[i][j]
                            + 2 * random.nextDouble() * (personalBestPositions[i][j] - positions[i][j])
                            + 2 * random.nextDouble() * (globalBestPosition[j] - positions[i][j]);

                    // Update position
                    positions[i][j] += velocities[i][j];
                    positions[i][j] = Math.abs(Math.round(positions[i][j])) % numVms; // Map to valid VM index
                }

                // Evaluate fitness
                double fitness = calculateFitness(positions[i], vmList, cloudletList);
                if (fitness < personalBestFitness[i]) {
                    personalBestFitness[i] = fitness;
                    System.arraycopy(positions[i], 0, personalBestPositions[i], 0, numCloudlets);

                    if (fitness < globalBestFitness) {
                        globalBestFitness = fitness;
                        System.arraycopy(positions[i], 0, globalBestPosition, 0, numCloudlets);
                    }
                }
            }
        }

        // Assign tasks based on the best solution
        for (int i = 0; i < numCloudlets; i++) {
            int vmIndex = (int) globalBestPosition[i];
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
}
