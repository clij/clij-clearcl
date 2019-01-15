package clearcl;

import clearcl.backend.jocl.ClearCLBackendJOCL;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClearCLTest {
    @Test
    public void getAllDevicesTest() {
        ClearCL clearCL = new ClearCL(new ClearCLBackendJOCL());
        for (ClearCLDevice device : clearCL.getAllDevices()) {
            System.out.println("Device name :" + device.getName());
        }
    }
}