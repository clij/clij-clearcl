package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelOrder;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.junit.Assert.assertArrayEquals;

public class ClearCLImageTest {

	@Test
	public void testReadFromAndWriteTo() {
		float[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
		float[] result = new float[16];
		try (
				ClearCL clearCL = new ClearCL(new ClearCLBackendJOCL());
				ClearCLDevice device = clearCL.getBestGPUDevice();
				ClearCLContext context = device.createContext();
				ClearCLImage buffer = context.createImage(ImageChannelOrder.RG, ImageChannelDataType.Float,
						2, 2, 2)
		) {
			FloatBuffer in = ByteBuffer.allocateDirect(expected.length * Float.BYTES).asFloatBuffer();
			in.put(expected);
			buffer.readFrom(in, true);
			FloatBuffer out = ByteBuffer.allocateDirect(result.length * Float.BYTES).asFloatBuffer();
			buffer.writeTo(out, true);
			out.get(result);
		}
		assertArrayEquals(expected, result, 0);
	}
}
