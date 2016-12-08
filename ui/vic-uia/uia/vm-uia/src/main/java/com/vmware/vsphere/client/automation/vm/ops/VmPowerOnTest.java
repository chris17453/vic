/* Copyright 2014 VMware, Inc. All rights reserved. -- VMware Confidential */
package com.vmware.vsphere.client.automation.vm.ops;

import org.testng.annotations.Test;

import com.vmware.client.automation.common.spec.TaskSpec;
import com.vmware.client.automation.common.step.VerifyTaskByUiStep;
import com.vmware.client.automation.workflow.common.WorkflowSpec;
import com.vmware.client.automation.workflow.common.WorkflowStepsSequence;
import com.vmware.client.automation.workflow.explorer.TestBedBridge;
import com.vmware.client.automation.workflow.explorer.TestbedSpecConsumer;
import com.vmware.client.automation.workflow.test.TestWorkflowStepContext;
import com.vmware.vsphere.client.automation.common.workflow.NGCTestWorkflow;
import com.vmware.vsphere.client.automation.components.navigator.NGCNavigator;
import com.vmware.vsphere.client.automation.components.navigator.spec.DatacenterLocationSpec;
import com.vmware.vsphere.client.automation.components.navigator.spec.VmLocationSpec;
import com.vmware.vsphere.client.automation.components.navigator.step.DatacenterNavigationStep;
import com.vmware.vsphere.client.automation.components.navigator.step.VmNavigationStep;
import com.vmware.vsphere.client.automation.provider.commontb.CommonTestBedProvider;
import com.vmware.vsphere.client.automation.srv.common.spec.DatacenterSpec;
import com.vmware.vsphere.client.automation.srv.common.spec.DatastoreSpec;
import com.vmware.vsphere.client.automation.srv.common.spec.HostSpec;
import com.vmware.vsphere.client.automation.srv.common.spec.SpecFactory;
import com.vmware.vsphere.client.automation.srv.common.spec.VcSpec;
import com.vmware.vsphere.client.automation.srv.common.spec.VmSpec;
import com.vmware.vsphere.client.automation.srv.common.step.CreateVmByApiStep;
import com.vmware.vsphere.client.automation.vm.common.messages.VmTaskMessages;
import com.vmware.vsphere.client.automation.vm.lib.ops.model.VmOpsModel.VmPowerState;
import com.vmware.vsphere.client.automation.vm.lib.ops.spec.VmPowerStateSpec;
import com.vmware.vsphere.client.automation.vm.ops.step.InvokeVmPowerOperationUiStep;
import com.vmware.vsphere.client.automation.vm.ops.step.VerifyVmPowerStateOnVmsViewStep;
import com.vmware.vsphere.client.automation.vm.lib.ops.step.VerifyVmPowerStateViaApiStep;
import com.vmware.vsphere.client.test.i18n.I18n;

/**
 * Test class for power on VM in the NGC client.
 * Executes the following test work-flow:
 * 1. Open a browser
 * 2. Login as admin user
 * 3. Navigate to the VM
 * 4. Power on the VM
 * 5. Verify via the API that the VM has been powered on
 * 6. Verify via UI that the power on VM task completes successfully
 * 7. Verify via UI that the VM has been powered on
 */
public class VmPowerOnTest extends NGCTestWorkflow {

   /**
    * {@inheritDoc}
    */
   @Override
   public void initSpec(WorkflowSpec testSpec, TestBedBridge testbedBridge) {
      TestbedSpecConsumer testBed = testbedBridge.requestTestbed(
            CommonTestBedProvider.class, true);

      // Spec for the VC
      VcSpec requestedVcSpec = testBed
            .getPublishedEntitySpec(CommonTestBedProvider.VC_ENTITY);

      // Spec for the datacenter
      DatacenterSpec requestedDatacenterSpec = testBed
            .getPublishedEntitySpec(CommonTestBedProvider.DC_ENTITY);

      // Spec for the host
      HostSpec requestedHostSpec = testBed
            .getPublishedEntitySpec(CommonTestBedProvider.CLUSTER_HOST_ENTITY);

      // Spec for the datastore
      DatastoreSpec requestedDastartoreSpec = testBed
            .getPublishedEntitySpec(CommonTestBedProvider.CLUSTER_HOST_DS_ENTITY);

      // Spec for the VM
      VmSpec vmSpec = SpecFactory.getSpec(VmSpec.class, requestedHostSpec);
      vmSpec.datastore.set(requestedDastartoreSpec);

      // Spec for the required VM power state
      VmPowerStateSpec vmPowerStateSpec = new VmPowerStateSpec();
      vmPowerStateSpec.vm.set(vmSpec);
      vmPowerStateSpec.powerState.set(VmPowerState.POWER_ON);

      // Spec for the location to the VM
      VmLocationSpec vmLocationSpec = new VmLocationSpec(vmSpec);

      // Spec for the location to the datacenter
      DatacenterLocationSpec datacenterLocationSpec = new DatacenterLocationSpec(
            requestedDatacenterSpec,
            NGCNavigator.NID_ENTITY_PRIMARY_TAB_VMS,
            NGCNavigator.NID_DATACENTER_VMS_II_TAB_VMS);

      // Spec for the power on VM task
      TaskSpec powerOnVmTaskSpec = new TaskSpec();
      powerOnVmTaskSpec.name.set(I18n.get(VmTaskMessages.class).powerOn());
      powerOnVmTaskSpec.status.set(TaskSpec.TaskStatus.COMPLETED);
      powerOnVmTaskSpec.target.set(vmSpec);

      testSpec.add(requestedVcSpec, requestedHostSpec, vmSpec, vmPowerStateSpec,
            vmLocationSpec, powerOnVmTaskSpec, datacenterLocationSpec);
      super.initSpec(testSpec, testbedBridge);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void composePrereqSteps(WorkflowStepsSequence<TestWorkflowStepContext> flow) {
      super.composePrereqSteps(flow);

      flow.appendStep("Create new test VM through the API", new CreateVmByApiStep());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void composeTestSteps(WorkflowStepsSequence<TestWorkflowStepContext> flow) {
      super.composeTestSteps(flow);

      flow.appendStep("Navigate to VM", new VmNavigationStep());

      flow.appendStep("Power On VM", new InvokeVmPowerOperationUiStep());

      flow.appendStep("Verify Power On VM task via UI", new VerifyTaskByUiStep());

      flow.appendStep(
            "Navigate to Datacenter > VMs > Virtual Machines view.",
            new DatacenterNavigationStep());

      flow.appendStep("Verify via UI that the VM is powered on",
            new VerifyVmPowerStateOnVmsViewStep());

      flow.appendStep("Verify via API that the VM is powered on",
            new VerifyVmPowerStateViaApiStep());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   @Test
   @TestID(id = "0")
   public void execute() throws Exception {
      super.execute();
   }
}