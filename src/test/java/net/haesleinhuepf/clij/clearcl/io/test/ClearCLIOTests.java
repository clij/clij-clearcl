package net.haesleinhuepf.clij.clearcl.io.test;

import java.io.File;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.clearcl.ClearCLProgram;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import net.haesleinhuepf.clij.clearcl.enums.BuildStatus;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelOrder;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.io.TiffWriter;
import net.haesleinhuepf.clij.clearcl.test.ClearCLBasicTests;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

/**
 * Test for File input/output
 *
 * @author haesleinhuepf
 */
public class ClearCLIOTests
{
  @Test
  public void testTiffWriter() throws Throwable
  {
    ClearCLBackendInterface lClearCLBackend =
                                            ClearCLBackends.getBestBackend();

    ClearCL lClearCL = new ClearCL(lClearCLBackend);

    ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

    System.out.println(lBestGPUDevice.getInfoString());

    ClearCLContext lContext = lBestGPUDevice.createContext();

    ClearCLProgram lProgram =
                            lContext.createProgram(ClearCLBasicTests.class,
                                                   "test.cl");

    lProgram.addDefine("CONSTANT", "1");

    BuildStatus lBuildStatus = lProgram.buildAndLog();

    ClearCLImage lImageSrc =
                           lContext.createImage(HostAccessType.WriteOnly,
                                                KernelAccessType.ReadWrite,
                                                ImageChannelOrder.Intensity,
                                                ImageChannelDataType.Float,
                                                100,
                                                100,
                                                100);

    ClearCLKernel lKernel = lProgram.createKernel("fillimagexor");

    lKernel.setArgument("image", lImageSrc);
    lKernel.setArgument("u", 1f);
    lKernel.setGlobalSizes(100, 100, 100);
    lKernel.run();

    ClearCLImage lImageDst =
                           lContext.createImage(HostAccessType.ReadOnly,
                                                KernelAccessType.WriteOnly,
                                                ImageChannelOrder.Intensity,
                                                ImageChannelDataType.Float,
                                                100,
                                                100,
                                                100);

    lImageSrc.copyTo(lImageDst, new long[]
    { 0, 0, 0 }, new long[]
    { 0, 0, 0 }, new long[]
    { 100, 100, 100 }, true);

    TiffWriter lTiffWriter = new TiffWriter(NativeTypeEnum.Byte,
                                            1f,
                                            0f);
    File lFile8 = File.createTempFile(this.getClass().getSimpleName(),
                                      "test8");
    File lFile16 =
                 File.createTempFile(this.getClass().getSimpleName(),
                                     "test16");
    File lFile32 =
                 File.createTempFile(this.getClass().getSimpleName(),
                                     "test32");
    lFile8.deleteOnExit();
    lFile16.deleteOnExit();
    lFile32.deleteOnExit();

    lTiffWriter.setOverwrite(true);
    lTiffWriter.setBytesPerPixel(8);
    lTiffWriter.write(lImageDst, lFile8);
    lTiffWriter.setBytesPerPixel(16);
    lTiffWriter.write(lImageDst, lFile16);
    lTiffWriter.setBytesPerPixel(32);
    lTiffWriter.write(lImageDst, lFile32);

  }
}
