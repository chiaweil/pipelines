/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2019 Skymind, Inc.
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

package ai.skymind.pipelines.util;

import ai.skymind.pipelines.output.types.BatchOutput;
import ai.skymind.pipelines.output.types.NDArrayOutput;
import ai.skymind.pipelines.serving.Output;
import ai.skymind.pipelines.util.MultiOutputBinaryUtils;
import io.vertx.core.buffer.Buffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;

public class MultiOutputBinaryUtilsTest {


    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test(timeout = 60000)

    public void binaryTests() throws Exception {
        INDArray input = Nd4j.linspace(1,4,4);
        Map<String,BatchOutput> map = new HashMap<>();
        NDArrayOutput ndArrayOutput = NDArrayOutput.builder()
                .ndArray(input).batchId(UUID.randomUUID().toString()).build();
        map.put("1",ndArrayOutput);
        Buffer numpy = MultiOutputBinaryUtils.convertBatchOutput(ndArrayOutput,Output.DataType.NUMPY);

        // TODO: nd4j and arrow unused
        Buffer nd4j = MultiOutputBinaryUtils.convertBatchOutput(ndArrayOutput, Output.DataType.ND4J);
        Buffer arrow = MultiOutputBinaryUtils.convertBatchOutput(ndArrayOutput, Output.DataType.ARROW);

        Buffer zip = MultiOutputBinaryUtils.zipBuffer(map, Output.DataType.NUMPY);
        File tmpFile  = temporary.newFile("tmp.zip");
        FileUtils.writeByteArrayToFile(tmpFile,zip.getBytes());
        ZipFile read = new ZipFile(tmpFile);
        INDArray npyFromByteArray = Nd4j.createNpyFromByteArray(numpy.getBytes());
        ZipEntry entry = read.getEntry("1");
        try(InputStream is = read.getInputStream(entry)) {
            byte[] content = IOUtils.toByteArray(is);
            INDArray npyFromByteArray2 = Nd4j.createNpyFromByteArray(content);
            assertEquals(npyFromByteArray,npyFromByteArray2);
        }

    }

}
