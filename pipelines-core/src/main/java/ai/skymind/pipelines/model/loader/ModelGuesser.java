/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2018 Skymind, Inc.
 *  *  *
 *  *  * This program and the accompanying materials are made available under the
 *  *  * terms of the Apache License, Version 2.0 which is available at
 *  *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  *  * License for the specific language governing permissions and limitations
 *  *  * under the License.
 *  *  *
 *  *  * SPDX-License-Identifier: Apache-2.0
 *  *  *****************************************************************************
 *
 *
 */

package ai.skymind.pipelines.model.loader;

import com.github.os72.protobuf351.CodedInputStream;
import com.github.os72.protobuf351.InvalidProtocolBufferException;
import com.github.os72.protobuf351.UnknownFieldSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.modelimport.keras.Hdf5Archive;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.config.KerasModelConfiguration;
import org.deeplearning4j.util.ModelGuesserException;
import org.deeplearning4j.util.ModelSerializer;
import org.json.JSONObject;
import org.nd4j.linalg.dataset.api.preprocessor.Normalizer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Guess a model from the given path
 * @author Adam Gibson
 */
@Slf4j
public class ModelGuesser {

    private static KerasModelConfiguration kerasConfig = new KerasModelConfiguration();
    static {
        // Turn off the auto-printing when failure occurs so that we can
        // handle the errors appropriately
        org.bytedeco.hdf5.Exception.dontPrint();
    }

    /**
     * A facade for {@link ModelSerializer#restoreNormalizerFromInputStream(InputStream)}
     * @param is the input stream to load form
     * @return the loaded normalizer
     * @throws IOException if an error occurs loading the normalizer
     */
    public static Normalizer<?> loadNormalizer(InputStream is) throws IOException {
        return ModelSerializer.restoreNormalizerFromInputStream(is);
    }


    /**
     * Returns true if the given file
     * is a keras file or not
     * @param path the file to check
     * @return true if the given file is a keras file,
     * faloee otherwise
     */
    public static boolean isKerasHdf5File(File path) {
        try {
            try (Hdf5Archive archive = new Hdf5Archive(path.getAbsolutePath())) {
                String kerasVersion = archive.readAttributeAsFixedLengthString(
                        kerasConfig.getFieldKerasVersion(), 5);
                if (kerasVersion != null) {
                    return true;
                }
            } catch (Throwable e) {
                log.error("Failure to load keras file", e);
                return false;
            }
        }catch(Throwable e) {
            //catch any errors related to opening non hdf5 files
            return false;
        }
        return false;
    }


    /**
     * Returns true if the given file
     * is a keras file or not
     * @param path the file to check
     * @return true if the given file is a keras file,
     * faloee otherwise
     */
    public static boolean isKerasComputationGraphFile(File path) {
        try {
            try (Hdf5Archive archive = new Hdf5Archive(path.getAbsolutePath())) {
                String className = archive.readAttributeAsFixedLengthString(
                        kerasConfig.getTrainingModelConfigAttribute(), 100);

                return className != null && !className.toLowerCase().contains("sequen");

            } catch (Throwable e) {
                log.error("Failure to load keras file", e);
                return false;
            }
        }catch(Throwable e) {
            //catch any errors related to opening non hdf5 files
            return false;
        }
    }

    /**
     * Returns true if the given file is
     * an onnx file.
     * @param file the file to check
     * @return true if the given file
     * is an onnx file, false otherwwise.
     */
    public static boolean isOnnxFile(File file) {
        try(InputStream is = new FileInputStream(file)) {
            CodedInputStream cis = CodedInputStream.newInstance(is);
            UnknownFieldSet.Builder unknownFields =
                    UnknownFieldSet.newBuilder();
            com.github.os72.protobuf351.ExtensionRegistryLite extensionRegistry =  com.github.os72.protobuf351.ExtensionRegistryLite.newInstance();
            int mutable_bitField0_ = 0;

            try {
                boolean done = false;
                while (!done) {
                    int tag = cis.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 8: {
                            cis.readInt64();
                            break;
                        }
                        case 18: {
                            String s = cis.readStringRequireUtf8();
                            break;
                        }
                        case 26: {
                            String s = cis.readStringRequireUtf8();
                            break;
                        }
                        case 34: {
                            String s = cis.readStringRequireUtf8();
                            break;
                        }
                        case 40: {
                            cis.readInt64();
                            break;
                        }
                        case 50: {
                            String s = cis.readStringRequireUtf8();
                            break;
                        }
                        case 58: {
                            cis.readMessage(onnx.OnnxProto3.GraphProto.parser(), extensionRegistry);
                            done = true;
                            break;
                        }
                        case 66: {
                            if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                                mutable_bitField0_ |= 0x00000002;
                            }
                            cis.readMessage(onnx.OnnxProto3.OperatorSetIdProto.parser(), extensionRegistry);
                            done = true;
                            break;
                        }
                        case 114: {
                            if (!((mutable_bitField0_ & 0x00000100) == 0x00000100)) {
                                mutable_bitField0_ |= 0x00000100;
                            }

                            cis.readMessage(onnx.OnnxProto3.StringStringEntryProto.parser(), extensionRegistry);
                            break;
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                return false;
            } catch (IOException e) {
                return false;
            } finally {
                if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                }
                if (((mutable_bitField0_ & 0x00000100) == 0x00000100)) {
                }

            }
        }catch(IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the given file is a tensorflow proto file.
     * @param file the file to check
     * @return true if the given file is a tensorflow proto file
     * false otherwise
     */
    public static boolean isTensorflowFile(File file) {
        try(InputStream is = new FileInputStream(file)) {
            CodedInputStream cis = CodedInputStream.newInstance(is);
            int size;
            int version_;
            com.github.os72.protobuf351.ExtensionRegistryLite extensionRegistry =  com.github.os72.protobuf351.ExtensionRegistryLite.newInstance();
            int mutable_bitField0_ = 0;
            try {
                boolean done = false;
                while (!done) {
                    int tag = cis.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 10: {
                            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                                mutable_bitField0_ |= 0x00000001;
                            }
                            cis.readMessage(org.tensorflow.framework.NodeDef.parser(), extensionRegistry);
                            done = true;
                            break;
                        }
                        case 18: {
                            org.tensorflow.framework.FunctionDefLibrary.Builder subBuilder = null;
                            cis.readMessage(org.tensorflow.framework.FunctionDefLibrary.parser(), extensionRegistry);
                            done = true;
                            break;
                        }
                        case 24: {
                            version_ = cis.readInt32();
                            done = true;
                            break;
                        }
                        case 34: {
                            org.tensorflow.framework.VersionDef.Builder subBuilder = null;
                            cis.readMessage(org.tensorflow.framework.VersionDef.parser(), extensionRegistry);
                            done = true;
                            break;
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                return false;
            } catch (IOException e) {
                return false;
            } finally {
                if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                }
            }


            return true;
        }catch(IOException e) {
            log.error("Unable to read proto file",e);
            return false;
        }
    }


    /**
     * Loads a dl4j zip file (either computation graph or multi layer network)
     * @param path the path to the file to load
     * @return a loaded dl4j model
     * @throws Exception if loading a dl4j model fails
     */
    public static Model loadDl4jGuess(String path) throws Exception {
        if(isZipFile(new File(path))) {
            log.debug("Loading file " + path);
            boolean compGraph = false;
            try(ZipFile zipFile = new ZipFile(path)) {
                List<String> collect = zipFile.stream().map(ZipEntry::getName)
                        .collect(Collectors.toList());
                log.debug("Entries " + collect);
                if(collect.contains(ModelSerializer.COEFFICIENTS_BIN) && collect.contains(ModelSerializer.CONFIGURATION_JSON)) {
                    ZipEntry entry = zipFile.getEntry(ModelSerializer.CONFIGURATION_JSON);
                    log.debug("Loaded configuration");
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        String configJson = IOUtils.toString(is, Charset.defaultCharset());
                        JSONObject jsonObject = new JSONObject(configJson);
                        if (jsonObject.has("vertexInputs")) {
                            log.debug("Loading computation graph.");
                            compGraph = true;
                        } else {
                            log.debug("Loading multi layer network.");
                        }

                    }
                }
            }

            if(compGraph) {
                return ModelSerializer.restoreComputationGraph(new File(path));
            }
            else {
                return ModelSerializer.restoreMultiLayerNetwork(new File(path));
            }
        }

        return null;
    }


    /**
     * Returns true if the given file is a dl4j zip file.
     * @param modelFile the model file to test
     * @return true if the model file is a dl4j zip file
     */
    public static boolean isSameDiffZipFile(File modelFile) {
        if(isZipFile(modelFile)) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(modelFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String> collect = zipFile.stream().map(ZipEntry::getName)
                    .collect(Collectors.toList());
            return false;

        }

        return false;
    }

    /**
     * Returns true if the given file is a dl4j zip file.
     * @param modelFile the model file to test
     * @return true if the model file is a dl4j zip file
     */
    public static boolean isDl4jFile(File modelFile) {
        if(isZipFile(modelFile)) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(modelFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String> collect = zipFile.stream().map(ZipEntry::getName)
                    .collect(Collectors.toList());
            return collect.contains(ModelSerializer.COEFFICIENTS_BIN) &&
                    collect.contains(ModelSerializer.CONFIGURATION_JSON);
        }

        return false;
    }

    /**
     * Returns true if the given file is a zip file
     * @param f the input file
     * @return true if the input file is a zip file
     */
    public static boolean isZipFile(File f) {
        long fileSingature = 0;
        try(RandomAccessFile raf = new RandomAccessFile(f,"r")) {
            fileSingature = raf.readInt();

        }catch(IOException e) {
            return false;
        }
        return fileSingature == 0x504B0304 || fileSingature == 0x504B0506 ||  fileSingature == 0x50B0708;
    }
    /**
     * Load the model from the given file path
     * @param path the path of the file to "guess"
     *
     * @return the loaded model
     * @throws Exception if every model load attempt fails
     */
    public static Model loadModelGuess(String path) throws Exception {
        try {
            return ModelSerializer.restoreMultiLayerNetwork(new File(path), true);
        } catch (Exception e) {
            log.warn("Tried multi layer network");
            try {
                return ModelSerializer.restoreComputationGraph(new File(path), true);
            } catch (Exception e1) {
                log.warn("Tried computation graph");
                try {
                    return ModelSerializer.restoreMultiLayerNetwork(new File(path), false);
                } catch (Exception e4) {
                    try {
                        return ModelSerializer.restoreComputationGraph(new File(path), false);
                    } catch (Exception e5) {
                        try {
                            return KerasModelImport.importKerasModelAndWeights(path);
                        } catch (Exception e2) {
                            log.warn("Tried multi layer network keras");
                            try {
                                return KerasModelImport.importKerasSequentialModelAndWeights(path);

                            } catch (Exception e3) {
                                throw new ModelGuesserException("Unable to load model from path " + path
                                        + " (invalid model file or not a known model type)");
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Load the model from the given input stream
     * @param stream the path of the file to "guess"
     *
     * @return the loaded model
     * @throws Exception if loading the model fails
     */
    public static Model loadModelGuess(InputStream stream) throws Exception {
        return loadModelGuess(stream, null);
    }

    /**
     * Load the model from the given input stream
     * @param stream the path of the file to "guess"
     * @param tempFileDirectory May be null. The directory in which to create any temporary files
     *
     * @return the loaded model
     * @throws Exception if all attempts at loading the model fail
     */
    public static Model loadModelGuess(InputStream stream, File tempFileDirectory) throws Exception {
        //Currently (Nov 2017): KerasModelImport doesn't support loading from input streams
        //Simplest solution here: write to a temporary file
        File f = File.createTempFile("loadModelGuess",".bin",tempFileDirectory);
        f.deleteOnExit();

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            IOUtils.copy(stream, os);
            os.flush();
            return loadModelGuess(f.getAbsolutePath());
        } catch (ModelGuesserException e){
            throw new ModelGuesserException("Unable to load model from input stream (invalid model file not a known model type)");
        } finally {
            f.delete();
        }
    }



}
