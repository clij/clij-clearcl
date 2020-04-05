package net.haesleinhuepf.clij.clearcl;

import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This class keeps track of all ClearCLImages and ClearCLBuffers which are created by a
 * ClearCLContext. In that way we can release them all by request or if the context is closed.
 *
 * Author: @haesleinhuepf
 *         April 2020
 */
class ClearCLImageInterfaceMemoryManager extends ConcurrentSkipListSet<ClearCLImageInterface> {

    @Override
    public boolean remove(Object o) {
        if (o instanceof ClearCLImageInterface) {
            ((ClearCLImageInterface) o).close();
        }
        removeReleasedImages();
        return super.remove(o);
    }

    /**
     * Internal method for removing objects from the list without releasing memory behind.
     * This is called from ClearCLImage and ClearCLBuffers when their close() method was called.
     * @param o
     */
    void removeWithoutClosing(Object o) {
        super.remove(o);
    }

    @Override
    public void clear() {
        for (ClearCLImageInterface image : this) {
            image.close();
        }
        super.clear();
    }

    @Override
    public boolean add(ClearCLImageInterface clearCLImageInterface) {
        removeReleasedImages();
        return super.add(clearCLImageInterface);
    }

    /**
     * This method is called to release objects from the list which were called earlier.
     * TODO: We might remove it.
     */
    private void removeReleasedImages() {
        for (ClearCLImageInterface buffer : this) {
            if (buffer.getPeerPointer() == null) {
                remove(buffer);
            }
        }
    }

    /**
     * Build up a human-readable list of all ClearCLImages and ClearCLBuffer which are
     * managed here.
     * @return list of objects and their allocated memory
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        long bytesSum = 0;
        stringBuilder.append("GPU contains " + size() + " images.\n");
        boolean wasClosedAlready = false;
        for (ClearCLImageInterface buffer : this) {
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


    private String humanReadableBytes(double bytesSum) {
        if (bytesSum > 1024) {
            bytesSum = bytesSum / 1024;
            if (bytesSum > 1024) {
                bytesSum = bytesSum / 1024;
                if (bytesSum > 1024) {
                    bytesSum = bytesSum / 1024;
                    return (Math.round(bytesSum * 10.0) / 10.0 + " Gb");
                } else {
                    return (Math.round(bytesSum * 10.0) / 10.0 + " Mb");
                }
            } else {
                return (Math.round(bytesSum * 10.0) / 10.0 + " kb");
            }
        }
        return Math.round(bytesSum * 10.0) / 10.0 + " b";
    }
}
