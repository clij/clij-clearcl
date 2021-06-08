package net.haesleinhuepf.clij.clearcl.util;

import net.haesleinhuepf.clij.clearcl.*;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.exceptions.OpenCLException;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.clearcl.util.ElapsedTime;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.io.IOException;
import java.util.*;

/**
 * This executor can call OpenCL files. It
 * uses some functionality adapted from FastFuse, to make .cl file handling
 * easier. For example, it ensures that the right
 * image_read/image_write methods are called depending on the image
 * type.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class CLKernelExecutor {
    public static int MAX_ARRAY_SIZE = 1000;
    ClearCLContext context;
    Class anchorClass;
    String programSourceCode;
    String programFilename;
    String kernelName;
    Map<String, Object> parameterMap;
    Map<String, Object> constantsMap;
    long[] globalSizes;
    long[] localSizes;

    boolean imageSizeIndependentCompilation = false;

    private final HashMap<String, ClearCLProgram> programCacheMap = new HashMap();
    ClearCLProgram currentProgram = null;

    public CLKernelExecutor(ClearCLContext context) {
        super();
        this.context = context;
    }

    public static void getOpenCLDefines(Map<String, Object> defines, String key, ImageChannelDataType imageChannelDataType, int dimension) {

        String sizeParameters = "const long image_size_" + key + "_width, " +
                                "const long image_size_" + key + "_height, " +
                                "const long image_size_" + key + "_depth, ";

        if (key.contains("dst") || key.contains("destination") || key.contains("output")) {
            if (dimension == 3) {
                defines.put("IMAGE_" + key + "_TYPE", sizeParameters + "__write_only image3d_t");
            } else if (dimension == 2) {
                defines.put("IMAGE_" + key + "_TYPE", sizeParameters + "__write_only image2d_t");
            }
        } else {
            if (dimension == 3) {
                defines.put("IMAGE_" + key + "_TYPE", sizeParameters + "__read_only image3d_t");
            } else if (dimension == 2) {
                defines.put("IMAGE_" + key + "_TYPE", sizeParameters + "__read_only image2d_t");
            }
        }

        defines.put("READ_" + key + "_IMAGE", imageChannelDataType.isInteger() ? "read_imageui" : "read_imagef");
        defines.put("WRITE_" + key + "_IMAGE", imageChannelDataType.isInteger() ? "write_imageui" : "write_imagef");

        switch (imageChannelDataType)
        {
            case Float:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "float");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE", "clij_convert_float_sat");
                break;
            case UnsignedInt8:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "uchar");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_uchar_sat(parameter)");
                break;
            case SignedInt8:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "char");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_char_sat(parameter)");
                break;
            case UnsignedInt16:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "ushort");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_ushort_sat(parameter)");
                break;
            case SignedInt16:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "short");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_short_sat(parameter)");
                break;
            case UnsignedInt32:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "uint");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_uint_sat(parameter)");
                break;
            case SignedInt32:
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "int");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_int_sat(parameter)");
                break;
            default: // UnsignedInt16, TODO: throw exception if different
                defines.put("IMAGE_" + key + "_PIXEL_TYPE", "ushort");
                defines.put("CONVERT_" + key + "_PIXEL_TYPE(parameter)", "clij_convert_ushort_sat(parameter)");
                break;
        }
    }

    private static void getPositionDefineOpenCLDefines(Map<String, Object> defines, String key, int dimension) {
        if (dimension < 3) {
            defines.put("POS_" + key + "_TYPE", "int2");
            defines.put("POS_" + key + "_INSTANCE(pos0, pos1, pos2, pos3)", "((int2)(pos0, pos1))");
        } else {
            defines.put("POS_" + key + "_TYPE", "int4");
            defines.put("POS_" + key + "_INSTANCE(pos0, pos1, pos2, pos3)", "((int4)(pos0, pos1, pos2, pos3))");
        }
    }

    public static void getOpenCLDefines(Map<String, Object> defines, String key, NativeTypeEnum nativeTypeEnum, int dimension) {
        String typeName = nativeTypeToOpenCLTypeName(nativeTypeEnum);
        String typeId = nativeTypeToOpenCLTypeShortName(nativeTypeEnum);

        String sizeParameters = "long image_size_" + key + "_width, long image_size_" + key + "_height, long image_size_" + key + "_depth, ";

        String sat = "_sat"; //typeName.compareTo("float")==0?"":"_sat";

        //if (key.contains("dst") || key.contains("destination") || key.contains("output")) {
            defines.put("IMAGE_" + key + "_PIXEL_TYPE", typeName);
        //} else {
        //    defines.put("IMAGE_" + key + "_PIXEL_TYPE", "const " + typeName);
        //}
        defines.put("CONVERT_" + key + "_PIXEL_TYPE", "clij_convert_" + typeName + sat);
        defines.put("IMAGE_" + key + "_TYPE", sizeParameters + "__global " + typeName + "*");

        if (dimension == 2) {
            defines.put("READ_" + key + "_IMAGE(a,b,c)", "read_buffer2d" + typeId + "(GET_IMAGE_WIDTH(a),GET_IMAGE_HEIGHT(a),GET_IMAGE_DEPTH(a),a,b,c)");
            defines.put("WRITE_" + key + "_IMAGE(a,b,c)", "write_buffer2d" + typeId + "(GET_IMAGE_WIDTH(a),GET_IMAGE_HEIGHT(a),GET_IMAGE_DEPTH(a),a,b,c)");
        } else if (dimension == 3) {
            defines.put("READ_" + key + "_IMAGE(a,b,c)", "read_buffer3d" + typeId + "(GET_IMAGE_WIDTH(a),GET_IMAGE_HEIGHT(a),GET_IMAGE_DEPTH(a),a,b,c)");
            defines.put("WRITE_" + key + "_IMAGE(a,b,c)", "write_buffer3d" + typeId + "(GET_IMAGE_WIDTH(a),GET_IMAGE_HEIGHT(a),GET_IMAGE_DEPTH(a),a,b,c)");
        }
    }

    private static String nativeTypeToOpenCLTypeName(NativeTypeEnum pDType) {
        if (pDType == NativeTypeEnum.Byte) {
            return "char";
        } else if (pDType == NativeTypeEnum.UnsignedByte) {
            return "uchar";
        } else if (pDType == NativeTypeEnum.Short) {
            return "short";
        } else if (pDType == NativeTypeEnum.UnsignedShort) {
            return "ushort";
        } else if (pDType == NativeTypeEnum.Int) {
            return "int";
        } else if (pDType == NativeTypeEnum.UnsignedInt) {
            return "uint";
        } else if (pDType == NativeTypeEnum.Long) {
            return "long";
        } else if (pDType == NativeTypeEnum.UnsignedLong) {
            return "ulong";
        } else if (pDType == NativeTypeEnum.Float) {
            return "float";
        } else {
            System.out.println("No type name available for " + pDType);
            return "";
        }
    }

    private static String nativeTypeToOpenCLTypeShortName(NativeTypeEnum pDType) {
        if (pDType == NativeTypeEnum.Byte) {
            return "c";
        } else if (pDType == NativeTypeEnum.UnsignedByte) {
            return "uc";
        } else if (pDType == NativeTypeEnum.Short) {
            return "s";
        } else if (pDType == NativeTypeEnum.UnsignedShort) {
            return "us";
        } else if (pDType == NativeTypeEnum.Int) {
            return "i";
        } else if (pDType == NativeTypeEnum.UnsignedInt) {
            return "ui";
        } else if (pDType == NativeTypeEnum.Long) {
            return "l";
        } else if (pDType == NativeTypeEnum.UnsignedLong) {
            return "ul";
        } else if (pDType == NativeTypeEnum.Float) {
            return "f";
        } else {
            return "";
        }
    }

    /**
     * Map of all parameters. It is recommended that input and output
     * images are given with the names "src" and "dst", respectively.
     *
     * @param parameterMap ?
     */
    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }
    public void setConstantsMap(Map<String, Object> constantsMap) {
        this.constantsMap = constantsMap;
    }

    public ClearCLKernel enqueue(boolean waitToFinish) {
        return enqueue(waitToFinish, null);
    }

    public ClearCLKernel enqueue(boolean waitToFinish, ClearCLKernel kernel) {

        if (kernel == null) {
            long time = System.currentTimeMillis();

            Map<String, Object> openCLDefines = new HashMap();
            openCLDefines.put("MAX_ARRAY_SIZE", MAX_ARRAY_SIZE); // needed for median. Median is limited to a given array length to be sorted

            if (constantsMap != null) {
                openCLDefines.putAll(constantsMap);
            }

            // deal with image width/height/depth for all images and buffers
            ArrayList<String> definedParameterKeys = new ArrayList<String>();
            for (String key : parameterMap.keySet()) {
                Object object = parameterMap.get(key);

                if (object instanceof  ClearCLImageInterface) {
                    if (!imageSizeIndependentCompilation) {
                        ClearCLImageInterface image = (ClearCLImageInterface) object;
                        openCLDefines.put("IMAGE_SIZE_" + key + "_WIDTH", image.getWidth());
                        openCLDefines.put("IMAGE_SIZE_" + key + "_HEIGHT", image.getHeight());
                        openCLDefines.put("IMAGE_SIZE_" + key + "_DEPTH", image.getDepth());
                    }
                    if (object instanceof ClearCLImage) {
                        ClearCLImage image = (ClearCLImage) object;
                        getOpenCLDefines(openCLDefines, key, image.getChannelDataType(), (int) image.getDimension());
                        getPositionDefineOpenCLDefines(openCLDefines, key, (int) image.getDimension());
                    } else if (object instanceof ClearCLBuffer) {
                        ClearCLBuffer image = (ClearCLBuffer) object;
                        getOpenCLDefines(openCLDefines, key, image.getNativeType(), (int) image.getDimension());
                        getPositionDefineOpenCLDefines(openCLDefines, key, (int) image.getDimension());
                    }
                }
                definedParameterKeys.add(key);
            }

            if (imageSizeIndependentCompilation) {
                openCLDefines.put("GET_IMAGE_WIDTH(image_key)", "image_size_ ## image_key ## _width");
                openCLDefines.put("GET_IMAGE_HEIGHT(image_key)", "image_size_ ## image_key ## _height");
                openCLDefines.put("GET_IMAGE_DEPTH(image_key)", "image_size_ ## image_key ## _depth");
            } else {
                openCLDefines.put("GET_IMAGE_WIDTH(image_key)", "IMAGE_SIZE_ ## image_key ## _WIDTH");
                openCLDefines.put("GET_IMAGE_HEIGHT(image_key)", "IMAGE_SIZE_ ## image_key ## _HEIGHT");
                openCLDefines.put("GET_IMAGE_DEPTH(image_key)", "IMAGE_SIZE_ ## image_key ## _DEPTH");
            }



            try {

                if (openCLDefines != null) {
                    kernel = getKernel(context, kernelName, openCLDefines);
                } else {
                    kernel = getKernel(context, kernelName);
                }

            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        if (kernel != null) {
            if (globalSizes != null) {
                kernel.setGlobalSizes(globalSizes);
            }

            if (localSizes != null) {
                kernel.setLocalSizes(localSizes);
            }

            final ClearCLKernel workaround = kernel;
            ElapsedTime.measure("Setting arguments", () -> {
                if (parameterMap != null) {
                    for (String key : parameterMap.keySet()) {
                        Object obj = parameterMap.get(key);
                        workaround.setArgument(key, obj);
                        if (obj instanceof ClearCLImageInterface) {
                            if(workaround.hasArgument("image_size_" + key + "_width"))
                                workaround.setArgument("image_size_" + key + "_width", ((ClearCLImageInterface) obj).getWidth());
                            if(workaround.hasArgument("image_size_" + key + "_height"))
                                workaround.setArgument("image_size_" + key + "_height", ((ClearCLImageInterface) obj).getHeight());
                            if(workaround.hasArgument("image_size_" + key + "_depth"))
                                workaround.setArgument("image_size_" + key + "_depth", ((ClearCLImageInterface) obj).getDepth());
                        }
                    }
                }
            });

            ElapsedTime.measure("Pure kernel execution", () -> {
               workaround.run(waitToFinish);
            });
        }

        return kernel;
    }

    public void setAnchorClass(Class anchorClass) {
        this.anchorClass = anchorClass;
    }

    public void setProgramFilename(String programFilename) {
        this.programSourceCode = null;
        this.programFilename = programFilename;
    }

    public void setProgramSourceCode(String programSourceCode) {
        this.programSourceCode = programSourceCode;
        this.programFilename = null;
    }

    public void setKernelName(String kernelName) {
        this.kernelName = kernelName;
    }

    public void setGlobalSizes(long[] globalSizes) {
        this.globalSizes = globalSizes;
    }

    public void setLocalSizes(long[] localSizes) {
        this.localSizes = localSizes;
    }

    protected ClearCLKernel getKernel(ClearCLContext context, String kernelName) throws IOException {
        return this.getKernel(context, kernelName, (Map) null);
    }

    protected ClearCLKernel getKernel(ClearCLContext context, String kernelName, Map<String, Object> defines) throws IOException {

        StringBuilder builder = new StringBuilder();

        if (programFilename != null) {
            builder.append(anchorClass.getCanonicalName() + " " + programFilename);
        } else {
            builder.append(anchorClass.getCanonicalName() + " " + programSourceCode.hashCode());
        }
        for (String key : defines.keySet()) {
            builder.append(" " + (key + " = " + defines.get(key)));
        }
        String programCacheKey = builder.toString();

        ClearCLProgram clProgram = this.programCacheMap.get(programCacheKey);
        currentProgram = clProgram;
        if (clProgram == null) {
            if (programFilename != null) {
                clProgram = context.createProgram(this.anchorClass, new String[]{this.programFilename});
            } else {
                clProgram = context.createProgram(programSourceCode);
            }
            if (defines != null) {
                Iterator iterator = defines.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry) iterator.next();
                    if (entry.getValue() instanceof String) {
                        clProgram.addDefine((String) entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Number) {
                        clProgram.addDefine((String) entry.getKey(), (Number) entry.getValue());
                    } else if (entry.getValue() == null) {
                        clProgram.addDefine((String) entry.getKey());
                    }
                }
            }

            clProgram.addBuildOptionAllMathOpt();
            clProgram.buildAndLog();
            //System.out.println("status: " + mProgram.getBuildStatus());
            //System.out.println("LOG: " + this.mProgram.getBuildLog());

            programCacheMap.put(programCacheKey, clProgram);
        }
        //System.out.println(clProgram.getSourceCode());
        //System.out.println(pKernelName);


        try {
            return clProgram.createKernel(kernelName);
        } catch (OpenCLException e) {
            System.out.println("Error when trying to create kernel " + kernelName);
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        for (String key : programCacheMap.keySet()) {
            ClearCLProgram program = programCacheMap.get(key);
            try {
                program.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentProgram != null) {
            try {
                currentProgram.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentProgram = null;
        }

        programCacheMap.clear();
    }


    public boolean isImageSizeIndependentCompilation() {
        return imageSizeIndependentCompilation;
    }

    public void setImageSizeIndependentCompilation(boolean imageSizeIndependentCompilation) {
        this.imageSizeIndependentCompilation = imageSizeIndependentCompilation;
    }
}
