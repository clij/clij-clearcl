package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class keeps track of all ClearCLImages and ClearCLBuffers which are created by a
 * ClearCLContext. In that way we can release them all by request or if the context is closed.
 *
 * Author: @haesleinhuepf
 *         April 2020
 */
class ClearCLImageInterfaceMemoryManager  {

    private Set<ClearCLImageInterface> images = ConcurrentHashMap.newKeySet();

	public void add(ClearCLImageInterface image) {
	    images.add(image);
    }

    public void remove(ClearCLImageInterface image) {
	    images.remove(image);
    }

    public void closeAll() {
	    images.forEach(ClearCLImageInterface::close);
    }

    /**
     * Build up a human-readable list of all ClearCLImages and ClearCLBuffer which are
     * managed here.
     */
    public String reportMemory(String deviceName) {
        StringBuilder stringBuilder = new StringBuilder();
        long bytesSum = 0;
        stringBuilder.append(deviceName + " contains " + images.size() + " images.\n");
        boolean wasClosedAlready = false;
        for (ClearCLImageInterface buffer : images) {
            String star = "";
            if (buffer.getPeerPointer() == null) {
                star = "*";
                wasClosedAlready = true;
            } else {
                bytesSum = bytesSum + buffer.getSizeInBytes();
            }

            stringBuilder.append("- " + buffer.getName() + star + " " + humanReadableBytes(buffer.getSizeInBytes()) + " [" + buffer.toString() + "] " + humanReadableBytes(buffer.getSizeInBytes()) + "\n");
        }
        stringBuilder.append("= " + humanReadableBytes(bytesSum) +"\n");
        if (wasClosedAlready) {
            stringBuilder.append("  * Some images/buffers were closed already.\n");
        }
        return stringBuilder.toString();
    }


    private String humanReadableBytes(long numberOfBytes) {
    	if (numberOfBytes < 1024)
            return numberOfBytes + " bytes";
        double value = numberOfBytes / 1024;
        if (value < 1024)
            return format(value) + " kB";
        value = value / 1024;
        if (value < 1024)
            return format(value) + " MB";
        value = value / 1024;
        return format(value) + " GB";
    }

    private String format(double bytesSum) {
        return Double.toString(Math.round(bytesSum * 10.0) / 10.0);
    }
}
