package fix.core;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;


public class VMAttacher {

    static String agentFile = Constant.agentFile;

    // can invoked by self app or remote app.
    public static void doAgentReload(String serverPid) {
        try {

            //HotSpotVirtualMachine hotSpotVirtualMachine
            VirtualMachineDescriptor gameVMDescriptor = null;
            if (serverPid.equals("")) {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                serverPid = name.split("@")[0];
            }

            System.err.println(VirtualMachine.class.getClassLoader());

            List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();

            for (VirtualMachineDescriptor vmDescriptor : descriptors) {
                String pid = vmDescriptor.id();
                System.err.println("list vm:" + pid);
                if (serverPid.equals(pid)) {
                    gameVMDescriptor = vmDescriptor;
                    break;
                }
            }

            if (gameVMDescriptor != null) {
                File file = new File(agentFile);
                if (file.exists()) {
                    VirtualMachine virtualMachine = VirtualMachine.attach(gameVMDescriptor);
                    virtualMachine.loadAgent(file.getAbsolutePath(), file.getAbsolutePath());
                    System.err.println("Agent Reload execute success!");
                } else {
                    System.err.println("file not exist:" + agentFile);
                }
            } else {
                System.err.println("not found vm:" + serverPid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0) {
            String pid = args[0];
            VMAttacher.doAgentReload(pid);
        } else {
            System.err.println("wrong args:" + args);
        }
    }

}
