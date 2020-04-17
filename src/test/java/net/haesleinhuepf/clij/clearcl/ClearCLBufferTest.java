package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import org.junit.Test;

import java.nio.FloatBuffer;

import static org.junit.Assert.assertArrayEquals;

public class ClearCLBufferTest {

	@Test
	public void testReadFromAndWriteTo() {
		float[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
		float[] result = new float[16];
		try (
				ClearCL clearCL = new ClearCL(new ClearCLBackendJOCL());
				ClearCLDevice device = clearCL.getBestGPUDevice();
				ClearCLContext context = device.createContext();
				ClearCLBuffer buffer = context.createBuffer(MemAllocMode.Best, HostAccessType.ReadWrite,
						KernelAccessType.ReadWrite, 2, NativeTypeEnum.Float, 2, 2, 2);
		) {
			buffer.readFrom(FloatBuffer.wrap(expected), true);
			buffer.writeTo(FloatBuffer.wrap(result), true);
		}
		assertArrayEquals(expected, result, 0);
	}
}
