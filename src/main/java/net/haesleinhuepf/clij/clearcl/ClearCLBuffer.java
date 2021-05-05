package net.haesleinhuepf.clij.clearcl;

import java.nio.Buffer;
import java.util.Arrays;

import net.haesleinhuepf.clij.clearcl.abs.ClearCLMemBase;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.clearcl.exceptions.ClearCLException;
import net.haesleinhuepf.clij.clearcl.exceptions.ClearCLHostAccessException;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLMemInterface;
import net.haesleinhuepf.clij.clearcl.util.Region3;
import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.coremem.util.Size;

/**
 * ClearCLBuffer is the ClearCL abstraction for OpenCL buffers.
 *
 * @author royer
 */
public class ClearCLBuffer extends ClearCLMemBase implements
                           ClearCLMemInterface,
                           ClearCLImageInterface

{

  private final ClearCLContext mClearCLContext;
  private final NativeTypeEnum mNativeType;
  private long mNumberOfChannels;
  private final long[] mDimensions;

  /**
   * This constructor is called internally from an OpenCl context.
   *
   * @param pClearCLContext
   *          context
   * @param pBufferPointer
   *          buffer pointer
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNativeType
   *          data type
   * @param pDimensions
   *          dimensions
   */
  ClearCLBuffer(ClearCLContext pClearCLContext,
                ClearCLPeerPointer pBufferPointer,
                MemAllocMode pMemAllocMode,
                HostAccessType pHostAccessType,
                KernelAccessType pKernelAccessType,
                long pNumberOfChannels,
                NativeTypeEnum pNativeType,
                long... pDimensions)
  {
    super(pClearCLContext.getBackend(),
          pBufferPointer,
          pMemAllocMode,
          pHostAccessType,
          pKernelAccessType);
    mClearCLContext = pClearCLContext;
    mNumberOfChannels = pNumberOfChannels;
    mNativeType = pNativeType;
    mDimensions = pDimensions;
  }

  /**
   * Fills the buffer with a given byte.
   *
   * @param pByte
   *          byte to fill buffer with
   * @param pBlockingFill
   *          true blocking call, false asynchronous call
   */
  public void fill(byte pByte, boolean pBlockingFill)
  {
    byte[] lPattern =
    { pByte };
    fill(lPattern, pBlockingFill);
  }

  /**
   * Fills the buffer with a given byte pattern.
   *
   * @param pPattern
   *          pattern as a sequence of bytes
   * @param pBlockingFill
   *          true blocking call, false asynchronous call
   */
  public void fill(byte[] pPattern, boolean pBlockingFill)
  {
    fill(pPattern, 0, getVolume(), pBlockingFill);
  }

  /**
   * Fills the buffer with a given byte pattern, from a given starting offset,
   * and for a certain length. This call can be required to block until
   * operation is finished.
   *
   * @param pPattern
   *          pattern as a sequence of bytes
   * @param pOffsetInBuffer
   *          offset in buffer in elements
   * @param pLengthInBuffer
   *          length in buffer in elements
   * @param pBlockingFill
   *          true blocking call, false asynchronous call
   */
  public void fill(byte[] pPattern,
                   long pOffsetInBuffer,
                   long pLengthInBuffer,
                   boolean pBlockingFill)
  {
    if (!(pOffsetInBuffer + pLengthInBuffer <= getVolume()))
      throw new ClearCLException("Incompatible length");

    getBackend().enqueueFillBuffer(mClearCLContext.getDefaultQueue()
                                                  .getPeerPointer(),
                                   getPeerPointer(),
                                   pBlockingFill,
                                   pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                   pLengthInBuffer * getNativeType().getSizeInBytes(),
                                   pPattern);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this OpenCl buffer into another OpenCl buffer of same length.
   *
   * @param pDstBuffer
   *          destination buffer
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer, boolean pBlockingCopy)
  {
    copyTo(pDstBuffer,
           0,
           0,
           getLength() * getNumberOfChannels(),
           pBlockingCopy);
  }

  /**
   * Copies a linear region of this OpenCl buffer into a linear region of same
   * length of another OpenCl buffer.
   *
   * @param pDstBuffer
   *          destination buffer
   * @param pOffsetInSrcBuffer
   *          source buffer offset in elements
   * @param pOffsetInDstBuffer
   *          destination buffer offset in elements
   * @param pLengthInElements
   *          copy length in elements
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer,
                     long pOffsetInSrcBuffer,
                     long pOffsetInDstBuffer,
                     long pLengthInElements,
                     boolean pBlockingCopy)
  {
    if ((pOffsetInSrcBuffer + pLengthInElements)
        * getNativeType().getSizeInBytes() > getSizeInBytes()
        || (pOffsetInDstBuffer + pLengthInElements)
           * getNativeType().getSizeInBytes() > pDstBuffer.getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    getBackend().enqueueCopyBuffer(mClearCLContext.getDefaultQueue()
                                                  .getPeerPointer(),
                                   getPeerPointer(),
                                   pDstBuffer.getPeerPointer(),
                                   pBlockingCopy,
                                   pOffsetInSrcBuffer
                                                  * getNativeType().getSizeInBytes(),
                                   pOffsetInDstBuffer * getNativeType().getSizeInBytes(),
                                   pLengthInElements * getNativeType().getSizeInBytes());
    pDstBuffer.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies a 3D region of this OpenCl buffer into a 3D region of same
   * dimensions of another OpenCl buffer.
   *
   * @param pDstBuffer
   *          destination buffer
   * @param pOriginInSrcBuffer
   *          source buffer origin
   * @param pOriginInDstBuffer
   *          destination buffer origin
   * @param pRegion
   *          region to copy
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer,
                     long[] pOriginInSrcBuffer,
                     long[] pOriginInDstBuffer,
                     long[] pRegion,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyBufferRegion(mClearCLContext.getDefaultQueue()
                                                        .getPeerPointer(),
                                         getPeerPointer(),
                                         pDstBuffer.getPeerPointer(),
                                         pBlockingCopy,
                                         Region3.origin(pOriginInSrcBuffer),
                                         Region3.origin(pOriginInDstBuffer),
                                         Region3.region(pRegion));
    pDstBuffer.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this OpenCl buffer to an OpenCl image.
   *
   * @param pDstImage
   *          destination image
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage, boolean pBlockingCopy)
  {
    copyTo(pDstImage,
           0,
           Region3.originZero(),
           Region3.region(pDstImage.getDimensions()),
           pBlockingCopy);
  }

  /**
   * Copies a 3D region of this OpenCl buffer into a 3D region of same
   * dimensions of an OpenCl image.
   *
   * @param pDstImage
   *          destination image
   * @param pOffsetInSrcBuffer
   *          source buffer offset in elements
   * @param pDstOrigin
   *          destination origin
   * @param pDstRegion
   *          destination region
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage,
                     long pOffsetInSrcBuffer,
                     long[] pDstOrigin,
                     long[] pDstRegion,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyBufferToImage(mClearCLContext.getDefaultQueue()
                                                         .getPeerPointer(),
                                          getPeerPointer(),
                                          pDstImage.getPeerPointer(),
                                          pBlockingCopy,
                                          pOffsetInSrcBuffer
                                                         * getNativeType().getSizeInBytes(),
                                          Region3.origin(pDstOrigin),
                                          Region3.region(pDstRegion));
    pDstImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this image into a host image.
   *
   * @param pClearCLHostImage
   *          host image.
   * @param pBlockingCopy
   *          true blocking call, false asynchronous call
   */
  public void copyTo(ClearCLHostImageBuffer pClearCLHostImage,
                     boolean pBlockingCopy)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    if (getSizeInBytes() != pClearCLHostImage.getSizeInBytes())
      throw new ClearCLException("Incompatible sizes");

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingCopy,
                                       0,
                                       getSizeInBytes(),
                                       getBackend().wrap(pClearCLHostImage.getContiguousMemory()));
    pClearCLHostImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Writes the contents of this OpenCl buffer into CoreMem buffer.
   *
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  @Override
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      boolean pBlockingWrite)
  {
    writeTo(pContiguousMemory,
            0,
            getLength() * getNumberOfChannels(),
            pBlockingWrite);
  }

  /**
   * Writes the contents of this OpenCl buffer into a linear region of a CoreMem
   * buffer.
   *
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pOffsetInBuffer
   *          offset in destination buffer in elements
   * @param pLengthInBuffer
   *          length to write in elements
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    if ((pOffsetInBuffer + pLengthInBuffer)
        * getNativeType().getSizeInBytes() > pContiguousMemory.getSizeInBytes()
        || (pOffsetInBuffer + pLengthInBuffer)
           * getNativeType().getSizeInBytes() > getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingWrite,
                                       pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                       pLengthInBuffer * getNativeType().getSizeInBytes(),
                                       lHostMemPointer);
  }

  /**
   * Writes a NIO buffer into this OpenCl buffer.
   *
   * @param pBuffer
   *          destination NIO buffer
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  @Override
  public void writeTo(Buffer pBuffer, boolean pBlockingWrite)
  {
    writeTo(pBuffer,
            0,
            getLength() * getNumberOfChannels(),
            pBlockingWrite);
  }

  /**
   * Writes a linear region of a NIO buffer into this OpenCl buffer.
   *
   * @param pBuffer
   *          destination NIO buffer
   * @param pOffsetInBuffer
   *          offset in destination buffer in elements
   * @param pLengthInBuffer
   *          length to write in elements
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    if ((pOffsetInBuffer + pLengthInBuffer)
        * getNativeType().getSizeInBytes() > Size.ofBuffer(pBuffer)
        || (pOffsetInBuffer + pLengthInBuffer)
           * getNativeType().getSizeInBytes() > getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingWrite,
                                       pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                       pLengthInBuffer * getNativeType().getSizeInBytes(),
                                       lHostMemPointer);
  }

  /**
   * Reads from a linear region of a CoreMem buffer into this OpenCl buffer.
   *
   * @param pContiguousMemory
   *          source NIO buffer
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  @Override
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       boolean pBlockingRead)
  {
    readFrom(pContiguousMemory,
             0,
             getLength() * getNumberOfChannels(),
             pBlockingRead);
  }

  /**
   * Reads from a linear region of a CoreMem buffer into this OpenCl buffer.
   *
   * @param pContiguousMemory
   *          source CoreMem buffer
   * @param pOffsetInBuffer
   *          offset in source buffer in elements
   * @param pLengthInBuffer
   *          length to read in elements
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       long pOffsetInBuffer,
                       long pLengthInBuffer,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    if ((pOffsetInBuffer + pLengthInBuffer)
        * getNativeType().getSizeInBytes() > pContiguousMemory.getSizeInBytes()
        || (pOffsetInBuffer + pLengthInBuffer)
           * getNativeType().getSizeInBytes() > getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                      pLengthInBuffer * getNativeType().getSizeInBytes(),
                                      lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a linear region of a NIO buffer into this OpenCl buffer.
   *
   * @param pBuffer
   *          source NIO buffer
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  @Override
  public void readFrom(Buffer pBuffer, boolean pBlockingRead)
  {
    readFrom(pBuffer,
             0,
             getLength() * getNumberOfChannels(),
             pBlockingRead);
  }

  /**
   * Reads from a linear region of a NIO buffer into this OpenCl buffer.
   *
   * @param pBuffer
   *          source NIO buffer
   * @param pOffsetInBuffer
   *          offset in source buffer in elements
   * @param pLengthInBuffer
   *          length to read in elements
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  public void readFrom(Buffer pBuffer,
                       long pOffsetInBuffer,
                       long pLengthInBuffer,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    if ((pOffsetInBuffer + pLengthInBuffer)
        * getNativeType().getSizeInBytes() > Size.ofBuffer(pBuffer)
        || (pOffsetInBuffer + pLengthInBuffer)
           * getNativeType().getSizeInBytes() > getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                      pLengthInBuffer * getNativeType().getSizeInBytes(),
                                      lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Writes to a 3D region of a CoreMem buffer into a 3D region of this OpenCl
   * buffer.
   *
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pSourceOrigin
   *          origin in destination buffer
   * @param pDestinationOrigin
   *          origin in source buffer
   * @param pRegion
   *          region to write
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBufferRegion(mClearCLContext.getDefaultQueue()
                                                            .getPeerPointer(),
                                             getPeerPointer(),
                                             pBlockingWrite,
                                             Region3.origin(pSourceOrigin),
                                             Region3.origin(pDestinationOrigin),
                                             Region3.region(pRegion),
                                             lHostMemPointer);
  }

  /**
   * Writes to a 3D region of a NIO buffer into a 3D region of this OpenCl
   * buffer.
   *
   * @param pBuffer
   *          destination NIO buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param pRegion
   *          region to write
   * @param pBlockingWrite
   *          true blocking call, false asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBufferRegion(mClearCLContext.getDefaultQueue()
                                                            .getPeerPointer(),
                                             getPeerPointer(),
                                             pBlockingWrite,
                                             Region3.origin(pSourceOrigin),
                                             Region3.origin(pDestinationOrigin),
                                             Region3.region(pRegion),
                                             lHostMemPointer);
  }

  /**
   * Reads from a 3D region of a CoreMem buffer into a 3D region of this OpenCl
   * buffer.
   *
   * @param pContiguousMemory
   *          source CoreMem buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param pRegion
   *          region to read
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       long[] pSourceOrigin,
                       long[] pDestinationOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            Region3.origin(pDestinationOrigin),
                                            Region3.origin(pSourceOrigin),
                                            Region3.region(pRegion),
                                            lHostMemPointer);

    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a 3D region of a NIO buffer into a 3D region of this OpenCl
   * buffer.
   *
   * @param pBuffer
   *          source NIO buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param pRegion
   *          region to read
   * @param pBlockingRead
   *          true blocking call, false asynchronous call
   */
  public void readFrom(Buffer pBuffer,
                       long[] pSourceOrigin,
                       long[] pDestinationOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            Region3.origin(pDestinationOrigin),
                                            Region3.origin(pSourceOrigin),
                                            Region3.region(pRegion),
                                            lHostMemPointer);

    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /* (non-Javadoc)
   * @see ClearCLMemInterface#getContext()
   */
  @Override
  public ClearCLContext getContext()
  {
    return mClearCLContext;
  }

  /**
   * Returns data type.
   *
   * @return data type
   */
  @Override
  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  /**
   * Returns length in elements, an element is composed of potentially several
   * channels and is certainly not equal to the size in bytes!
   *
   * @return length in elements
   */
  public long getLength()
  {
    return getVolume();
  }

  @Override
  public long getNumberOfChannels()
  {
    return mNumberOfChannels;
  }

  @Override
  public long[] getDimensions()
  {
    return mDimensions;
  }

  /**
   * Returns the size in bytes.
   *
   * @return size in bytes
   */
  @Override
  public long getSizeInBytes()
  {
    return getLength() * getNumberOfChannels()
           * mNativeType.getSizeInBytes();
  }

  protected String name = "";

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString()
  {
    return String.format(name + " ClearCLBuffer [mClearCLContext=%s, mNativeType=%s, mNumberOfChannels=%s, mDimensions=%s, getMemAllocMode()=%s, getHostAccessType()=%s, getKernelAccessType()=%s, getBackend()=%s, getPeerPointer()=%s]",
                         mClearCLContext,
                         mNativeType,
                         mNumberOfChannels,
                         Arrays.toString(mDimensions),
                         getMemAllocMode(),
                         getHostAccessType(),
                         getKernelAccessType(),
                         getBackend(),
                         getPeerPointer());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    mClearCLContext.removeImage(this);
    if (getPeerPointer() != null)
    {
      getBackend().releaseBuffer(getPeerPointer());
      setPeerPointer(null);
    }
  }
}
