package Algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

public class MinMinAlgorithm {
	public void runAlgorithm(DatacenterBroker broker, List<Vm> vmList, List<Cloudlet> cloudletList) {
		List<Cloudlet> unscheduledCloudlets = new ArrayList<>(cloudletList);
        Map<Vm, Double> vmReadyTimes = new HashMap<>();

        // Initialize ready times for all VMs
        for (Vm vm : vmList) {
            vmReadyTimes.put(vm, 0.0);
        }

        while (!unscheduledCloudlets.isEmpty()) {
            Cloudlet selectedCloudlet = null;
            Vm selectedVm = null;
            double minCompletionTime = Double.MAX_VALUE;

            for (Cloudlet cloudlet : unscheduledCloudlets) {
                for (Vm vm : vmList) {
                    double readyTime = vmReadyTimes.get(vm);
                    double completionTime = readyTime + (cloudlet.getCloudletTotalLength() / vm.getMips());
                    if (completionTime < minCompletionTime) {
                        minCompletionTime = completionTime;
                        selectedCloudlet = cloudlet;
                        selectedVm = vm;
                    }
                }
            }

            if (selectedCloudlet != null && selectedVm != null) {
                broker.bindCloudletToVm(selectedCloudlet.getCloudletId(), selectedVm.getId());
                unscheduledCloudlets.remove(selectedCloudlet);

                // Update the ready time of the selected VM
                double newReadyTime = vmReadyTimes.get(selectedVm) + (selectedCloudlet.getCloudletTotalLength() / selectedVm.getMips());
                vmReadyTimes.put(selectedVm, newReadyTime);
            }
        }
	}
}
