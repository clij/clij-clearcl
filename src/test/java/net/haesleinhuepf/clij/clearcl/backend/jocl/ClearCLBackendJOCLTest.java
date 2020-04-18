package net.haesleinhuepf.clij.clearcl.backend.jocl;

import org.jocl.CL;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ClearCLBackendJOCL}
 */
public class ClearCLBackendJOCLTest {

	@Test
	public void testMultiThreadedGetNumberOfPlatforms() throws InterruptedException {
		// NB: This test failed, on Ubuntu 16.04, with NVIDIA GTX960M, Driver Version 410.129, when the calls
		//     to CL.clGetPlatformIDs() where not synchronized.
		int numThreads = 10;
		CountDownLatch finishSignal = new CountDownLatch(numThreads);
		List<Exception> exceptions = new LinkedList<>();
		for (int i = 0; i < 10; i++) {
			new Thread(() -> {
				try {
					new ClearCLBackendJOCL().getNumberOfPlatforms();
				} catch (Exception exception) {
					exceptions.add(exception);
				}
				finishSignal.countDown();
			}).start();
		}
		// wait for all threads to finish
		finishSignal.await();
		// make sure there where no exceptions
		assertEquals(Collections.emptyList(), exceptions);
	}
}
