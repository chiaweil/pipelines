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

package ai.skymind.pipelines.pipeline.handlers.array;

import ai.skymind.pipelines.pipeline.handlers.array.transform.SameDiffArrayTransform;
import ai.skymind.pipelines.pipeline.handlers.array.transform.TensorflowArrayTransform;
import ai.skymind.pipelines.util.SchemaTypeUtils;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.Record;
import org.datavec.api.writable.NDArrayWritable;
import org.datavec.api.writable.Writable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArrayTransformTests {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();


    @Test(timeout = 60000)

    public void testSameDiffEvalByteBuffer() throws Exception {
        SameDiff permuteGraph = SameDiff.create();
        SDVariable inputPlaceHolder = permuteGraph.var("default",Nd4j.linspace(1,4,4).reshape(2,2));
        SDVariable permute = permuteGraph.permute(inputPlaceHolder, 1, 0);
        ByteBuffer byteBuffer = permuteGraph.asFlatBuffers(true);
        byte[] content = new byte[byteBuffer.capacity() - byteBuffer.position()];
        byteBuffer.get(content);

        File newFile = temporary.newFile();
        permuteGraph.asFlatFile(newFile);

        byte[] bytes = FileUtils.readFileToByteArray(newFile);
        assertArrayEquals(bytes,content);


        SameDiffArrayTransform sameDiffArrayTransform = new SameDiffArrayTransform(content, Arrays.asList("default"),Arrays.asList("output"));
        Record[] transform = sameDiffArrayTransform.transform(SchemaTypeUtils.toRecords(new INDArray[]{Nd4j.linspace(1, 4, 4).reshape(2, 2)}));//input + output
        assertEquals(2,transform.length);
        INDArray assertion = Nd4j.linspace(1,4,4).reshape(2,2).transpose();
        Writable writable = transform[1].getRecord().get(0);
        NDArrayWritable ndArrayWritable = (NDArrayWritable) writable;
        assertEquals(assertion,ndArrayWritable.get());
    }

    @Test(timeout = 60000)

    public void testTensorflowTransform() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("transpose.pb");
        File inputFile = classPathResource.getFile();
        TensorflowArrayTransform tensorflowArrayTransform = new TensorflowArrayTransform(inputFile,Arrays.asList("x"),Arrays.asList("output"));
        INDArray arr = Nd4j.linspace(1,6,6).reshape(3,2);
        INDArray[] transform = SchemaTypeUtils.toArrays(tensorflowArrayTransform.transform(SchemaTypeUtils.toRecords(new INDArray[]{arr})));
        INDArray transposeAssertion = arr.transpose();
        assertEquals(transposeAssertion,transform[0]);
    }


    @Test(timeout = 60000)

    public void testTensorflowTransform608() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("transpose_608.pb");
        File inputFile = classPathResource.getFile();
        TensorflowArrayTransform tensorflowArrayTransform = new TensorflowArrayTransform(inputFile,Arrays.asList("x"),Arrays.asList("output"));
        INDArray arr = Nd4j.rand(new int[]{1,3,608,608});
        INDArray[] transform = SchemaTypeUtils.toArrays(tensorflowArrayTransform.transform(SchemaTypeUtils.toRecords(new INDArray[]{arr})));
        INDArray transposeAssertion = arr.permute(0,2,3,1);
        assertEquals(transposeAssertion,transform[0]);
    }



    @Test(timeout = 60000)

    public void testSameDiffEval() throws Exception {
        SameDiff permuteGraph = SameDiff.create();
        SDVariable inputPlaceHolder = permuteGraph.var("default",Nd4j.linspace(1,4,4).reshape(2,2));
        SDVariable permute = permuteGraph.permute(inputPlaceHolder, 1, 0);
        File newFile = temporary.newFile();
        permuteGraph.asFlatFile(newFile);
        ByteBuffer byteBuffer = permuteGraph.asFlatBuffers(true);
        byteBuffer.position(0);
        byte[] content = new byte[byteBuffer.capacity()];
        byteBuffer.get(content);
        SameDiffArrayTransform sameDiffArrayTransform = new SameDiffArrayTransform(newFile, Arrays.asList("default"));
        Record[] records = SchemaTypeUtils.toRecords(new INDArray[]{Nd4j.linspace(1,4,4).reshape(2,2)});
        Record[] transform = sameDiffArrayTransform.transform(records);
        //input + output
        assertEquals(2,transform.length);
        INDArray assertion = Nd4j.linspace(1,4,4).reshape(2,2).transpose();
        Writable writable = transform[1].getRecord().get(0);
        NDArrayWritable ndArrayWritable = (NDArrayWritable) writable;
        assertEquals(assertion,ndArrayWritable.get());
    }

}
