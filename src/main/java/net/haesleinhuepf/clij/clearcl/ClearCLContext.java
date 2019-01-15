package net.haesleinhuepf.clij.clearcl;

import java.io.IOException;

import net.haesleinhuepf.clij.clearcl.abs.ClearCLBase;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackendInterface;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelOrder;
import net.haesleinhuepf.clij.clearcl.enums.ImageType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.clearcl.exceptions.OpenCLException;
import coremem.enums.NativeTypeEnum;
import coremem.rgc.Cleanable;
import coremem.rgc.Cleaner;
import coremem.rgc.RessourceCleaner;

/**
 * ClearCLContext is the ClearCL abstraction for OpenCl contexts.
 *
 * @author royer
 */
public class ClearCLContext extends ClearCLBase implements Cleanable
{
  private boolean mDebugNotifyAllocation = false;

  private final ClearCLDevice mDevice;

  private final ClearCLQueue mDefaultQueue;

  // This will register this buffer for GC cleanup
  {
    RessourceCleaner.register(this);
  }

  /**
   * Construction of this object is done from within a ClearClDevice.
   * 
   * @param pClearCLDevice
   *          device
   * @param pContextPointer
   *          context peer pointer
   */
  ClearCLContext(final ClearCLDevice pClearCLDevice,
                 ClearCLPeerPointer pContextPointer)
  {
    super(pClearCLDevice.getBackend(), pContextPointer);
    mDevice = pClearCLDevice;

    mDefaultQueue = createQueue();
  }

  /**
   * Returns context's device
   * 
   * @return device
   */
  public ClearCLDevice getDevice()
  {
    return mDevice;
  }

  /**
   * Returns the default queue. All devices are created with a default queue.
   * 
   * @return default queue
   */
  public ClearCLQueue getDefaultQueue()
  {
    return mDefaultQueue;
  }

  /**
   * Creates a queue.
   * 
   * @return queue
   */
  public ClearCLQueue createQueue()
  {
    final ClearCLPeerPointer lQueuePointer =
                                           getBackend().getQueuePeerPointer(getDevice().getPeerPointer(),
                                                                            getPeerPointer(),
                                                                            true);
    final ClearCLQueue lClearCLQueue =
                                     new ClearCLQueue(this,
                                                      lQueuePointer);
    return lClearCLQueue;
  }

  /**
   * Creates an OpenCL buffer with a given memory allocation mode, host and
   * kernel access and a template image to match for dimensions, data type and
   * number of channels.
   *
   * @param pMemAllocMode
   *          allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pTemplate
   *          image to use as template
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final MemAllocMode pMemAllocMode,
                                    final HostAccessType pHostAccessType,
                                    final KernelAccessType pKernelAccessType,
                                    final ClearCLImage pTemplate)
  {
    return createBuffer(pMemAllocMode,
                        pHostAccessType,
                        pKernelAccessType,
                        pTemplate.getNumberOfChannels(),
                        pTemplate.getNativeType(),
                        pTemplate.getDimension());
  }

  /**
   * Creates an OpenCL buffer with a given data type and length. The host and
   * kernel access policy is read and write access for both.
   * 
   * @param pNativeType
   *          native type
   * @param pBufferLengthInElements
   *          length in elements
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final NativeTypeEnum pNativeType,
                                    final long pBufferLengthInElements)
  {
    return createBuffer(MemAllocMode.Best,
                        HostAccessType.ReadWrite,
                        KernelAccessType.ReadWrite,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given access policy, data type and length.
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNativeType
   *          data type
   * @param pBufferLengthInElements
   *          length in elements
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final HostAccessType pHostAccessType,
                                    final KernelAccessType pKernelAccessType,
                                    final NativeTypeEnum pNativeType,
                                    final long pBufferLengthInElements)
  {
    return createBuffer(MemAllocMode.Best,
                        pHostAccessType,
                        pKernelAccessType,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given access policy, data type, memory
   * allocation mode and length. The host and kernel access policy is read and
   * write access for both.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pNativeType
   *          native type
   * @param pBufferLengthInElements
   *          length in elements
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final MemAllocMode pMemAllocMode,
                                    final NativeTypeEnum pNativeType,
                                    final long pBufferLengthInElements)
  {
    return createBuffer(pMemAllocMode,
                        HostAccessType.ReadWrite,
                        KernelAccessType.ReadWrite,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given data type, access policy, memory
   * allocation mode, native type, and length.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNativeType
   *          native data type
   * @param pBufferLengthInElements
   *          length in elements
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final MemAllocMode pMemAllocMode,
                                    final HostAccessType pHostAccessType,
                                    final KernelAccessType pKernelAccessType,
                                    final NativeTypeEnum pNativeType,
                                    final long pBufferLengthInElements)
  {
    return createBuffer(pMemAllocMode,
                        pHostAccessType,
                        pKernelAccessType,
                        1,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given data type, memory allocation mode and
   * access policy, memory allocation mode, native type, and dimensions. In this
   * case the buffer can be interpreted as an image.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNumberOfChannels
   *          number of channels per
   * @param pNativeType
   *          native type per channel per pixel/voxel
   * @param pDimensions
   *          image buffer dimensions
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(final MemAllocMode pMemAllocMode,
                                    final HostAccessType pHostAccessType,
                                    final KernelAccessType pKernelAccessType,
                                    final long pNumberOfChannels,
                                    final NativeTypeEnum pNativeType,
                                    final long... pDimensions)
  {
    notifyMemoryAllocation();

    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];

    final long lBufferSizeInBytes = lVolume * pNumberOfChannels
                                    * pNativeType.getSizeInBytes();

    if (lBufferSizeInBytes < 0)
      throw new OpenCLException(-61);

    final ClearCLPeerPointer lBufferPointer =
                                            getBackend().getBufferPeerPointer(getDevice().getPeerPointer(),
                                                                              getPeerPointer(),
                                                                              pMemAllocMode,
                                                                              pHostAccessType,
                                                                              pKernelAccessType,
                                                                              lBufferSizeInBytes);

    final ClearCLBuffer lClearCLBuffer =
                                       new ClearCLBuffer(this,
                                                         lBufferPointer,
                                                         pMemAllocMode,
                                                         pHostAccessType,
                                                         pKernelAccessType,
                                                         pNumberOfChannels,
                                                         pNativeType,
                                                         pDimensions);
    return lClearCLBuffer;
  }

  /**
   * Creates an image with same parameters as the given image,
   * 
   * @param pImage
   *          template image to use
   * @return created image
   */
  public ClearCLImage createImage(ClearCLImage pImage)
  {
    return createImage(pImage, null);
  }

  /**
   * Creates an image with same parameters as the given image,
   * 
   * @param pImage
   *          template image to use
   * @param pChannelDataType
   *          Overrides channel data type, if null the original channel data
   *          type is used
   * @return created image
   */
  public ClearCLImage createImage(ClearCLImage pImage,
                                  ImageChannelDataType pChannelDataType)
  {
    return createImage(pImage.getMemAllocMode(),
                       pImage.getHostAccessType(),
                       pImage.getKernelAccessType(),
                       pImage.getChannelOrder(),
                       pChannelDataType == null ? pImage.getChannelDataType()
                                                : pChannelDataType,
                       pImage.getDimensions());
  }

  /**
   * Creates 1D, 2D, or 3D single channel images with a given channel data type,
   * and dimensions. The host and kernel access policy is read and write access
   * for both.
   * 
   * 
   * @param pImageChannelType
   *          channel data type
   * @param pDimensions
   *          dimensions
   * 
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createSingleChannelImage(final ImageChannelDataType pImageChannelType,
                                               final long... pDimensions)
  {
    return createImage(MemAllocMode.Best,
                       HostAccessType.ReadWrite,
                       KernelAccessType.ReadWrite,
                       getDevice().getType()
                                  .isCPU() ? ImageChannelOrder.Intensity
                                           : ImageChannelOrder.R,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given channel order, channel data type,
   * and dimensions. The host and kernel access policy is read and write access
   * for both.
   * 
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pDimensions
   *          dimensions
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(final ImageChannelOrder pImageChannelOrder,
                                  final ImageChannelDataType pImageChannelType,
                                  final long... pDimensions)
  {
    return createImage(MemAllocMode.Best,
                       HostAccessType.ReadWrite,
                       KernelAccessType.ReadWrite,
                       pImageChannelOrder,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D single channel image with a given memory allocation
   * and access policy, channel data type, and dimensions.
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelType
   *          channel data type
   * @param pDimensions
   *          dimensions
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createSingleChannelImage(final HostAccessType pHostAccessType,
                                               final KernelAccessType pKernelAccessType,
                                               final ImageChannelDataType pImageChannelType,
                                               final long... pDimensions)
  {
    return createImage(MemAllocMode.Best,
                       pHostAccessType,
                       pKernelAccessType,
                       getDevice().getType()
                                  .isCPU() ? ImageChannelOrder.Intensity
                                           : ImageChannelOrder.R,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given memory allocation and access
   * policy, channel order, channel data type, and dimensions.
   * 
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pDimensions
   *          dimensions
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(final HostAccessType pHostAccessType,
                                  final KernelAccessType pKernelAccessType,
                                  final ImageChannelOrder pImageChannelOrder,
                                  final ImageChannelDataType pImageChannelType,
                                  final long... pDimensions)
  {
    return createImage(MemAllocMode.Best,
                       pHostAccessType,
                       pKernelAccessType,
                       pImageChannelOrder,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given memory allocation and access
   * policy, channel order, channel data type, and dimensions.
   * 
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pDimensions
   *          dimensions
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(final MemAllocMode pMemAllocMode,
                                  final HostAccessType pHostAccessType,
                                  final KernelAccessType pKernelAccessType,
                                  final ImageChannelOrder pImageChannelOrder,
                                  final ImageChannelDataType pImageChannelType,
                                  final long... pDimensions)
  {
    notifyMemoryAllocation();

    final ImageType lImageType =
                               ImageType.fromDimensions(pDimensions);

    final ClearCLPeerPointer lImage =
                                    getBackend().getImagePeerPointer(getDevice().getPeerPointer(),
                                                                     getPeerPointer(),
                                                                     pMemAllocMode,
                                                                     pHostAccessType,
                                                                     pKernelAccessType,
                                                                     lImageType,
                                                                     pImageChannelOrder,
                                                                     pImageChannelType,
                                                                     pDimensions);

    final ClearCLImage lClearCLImage = new ClearCLImage(this,
                                                        lImage,
                                                        pMemAllocMode,
                                                        pHostAccessType,
                                                        pKernelAccessType,
                                                        lImageType,
                                                        pImageChannelOrder,
                                                        pImageChannelType,
                                                        pDimensions);

    return lClearCLImage;
  }

  /**
   * Creates a program, with optional source code
   * 
   * @param pSourceCode
   *          optional varargs of source code strings.
   * @return program
   */
  public ClearCLProgram createProgram(final String... pSourceCode)
  {
    final ClearCLProgram lClearCLProgram =
                                         new ClearCLProgram(getDevice(),
                                                            this,
                                                            null);
    for (final String lSourceCode : pSourceCode)
      lClearCLProgram.addSource(lSourceCode);

    return lClearCLProgram;
  }

  /**
   * Creates a program given a list of resources locate relative to a reference
   * class.
   * 
   * @param pClassForRessource
   *          reference class to locate resources
   * @param pRessourceNames
   *          Resource file names (relative to reference class)
   * @return program
   * @throws IOException
   *           if IO problem while accessing resources
   */
  public ClearCLProgram createProgram(final Class<?> pClassForRessource,
                                      final String... pRessourceNames) throws IOException
  {
    final ClearCLProgram lClearCLProgram = createProgram();

    for (final String lRessourceName : pRessourceNames)
      lClearCLProgram.addSource(pClassForRessource, lRessourceName);

    return lClearCLProgram;
  }

  /**
   * Returns the boolean flag that decides whether to print a message with a
   * stack trace every time a buffer or image is allocated. this is practical to
   * detect OpenCl memory trashing/leaking
   * 
   * @return true -> debug messages, false -> no debug messages
   */
  public boolean isDebugNotifyAllocation()
  {
    return mDebugNotifyAllocation;
  }

  /**
   * Sets the debug allocation notification flag.
   * 
   * @param pDebugNotifyAllocation
   *          true -> debug messages, false -> no debug messages
   */
  public void setDebugNotifyAllocation(boolean pDebugNotifyAllocation)
  {
    mDebugNotifyAllocation = pDebugNotifyAllocation;
  }

  private void notifyMemoryAllocation()
  {
    if (isDebugNotifyAllocation())
    {
      System.out.println("!!CLEARCL ALLOCATION!!");
      Thread.dumpStack();
    }
  }

  @Override
  public String toString()
  {
    return String.format("ClearCLContext [device=%s]",
                         getDevice().toString());
  }

  @Override
  public void close()
  {
    if (getPeerPointer() != null)
    {
      if (mContextCleaner != null)
        mContextCleaner.mClearCLPeerPointer = null;
      getBackend().releaseContext(getPeerPointer());
      setPeerPointer(null);
    }
  }

  // NOTE: this _must_ be a static class, otherwise instances of this class will
  // implicitely hold a reference of this image...
  private static class ContextCleaner implements Cleaner
  {
    public ClearCLBackendInterface mBackend;
    public ClearCLPeerPointer mClearCLPeerPointer;

    public ContextCleaner(ClearCLBackendInterface pBackend,
                          ClearCLPeerPointer pClearCLPeerPointer)
    {
      mBackend = pBackend;
      mClearCLPeerPointer = pClearCLPeerPointer;
    }

    @Override
    public void run()
    {
      try
      {
        if (mClearCLPeerPointer != null)
          mBackend.releaseContext(mClearCLPeerPointer);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  ContextCleaner mContextCleaner =
                                 new ContextCleaner(getBackend(),
                                                    getPeerPointer());

  @Override
  public Cleaner getCleaner()
  {
    return mContextCleaner;
  }

}
