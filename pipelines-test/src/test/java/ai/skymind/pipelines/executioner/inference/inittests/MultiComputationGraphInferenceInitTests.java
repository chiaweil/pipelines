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

package ai.skymind.pipelines.executioner.inference.inittests;

import ai.skymind.pipelines.executioner.inference.MultiComputationGraphInferenceExecutioner;
import ai.skymind.pipelines.model.loader.dl4j.cg.InMemoryComputationGraphModelLoader;
import ai.skymind.pipelines.serving.ParallelInferenceConfig;
import ai.skymind.pipelines.train.TrainUtils;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import java.io.File;


public class MultiComputationGraphInferenceInitTests {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @Test(timeout = 60000)

    public void testComputationGraph() throws Exception {
        MultiComputationGraphInferenceExecutioner multiLayerNetworkInferenceExecutioner = new MultiComputationGraphInferenceExecutioner();

        ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();


        File arrayFolder = folder.newFolder();
        File testLabels = new File(arrayFolder,"test-labels.npy");
        File testInput = new File(arrayFolder,"test-input.npy");

        DataSet dataSet = new IrisDataSetIterator(150,150).next();
        Nd4j.writeAsNumpy(dataSet.getFeatures(),testInput);
        Nd4j.writeAsNumpy(dataSet.getLabels(),testLabels);

        Pair<MultiLayerNetwork, DataNormalization> trainedNetwork = TrainUtils.getTrainedNetwork();
        trainedNetwork.getSecond().transform(dataSet.getFeatures());


        InMemoryComputationGraphModelLoader memoryMultiLayernetworkModelLoader = new InMemoryComputationGraphModelLoader(trainedNetwork.getFirst().toComputationGraph());
        multiLayerNetworkInferenceExecutioner.initialize(memoryMultiLayernetworkModelLoader, parallelInferenceConfig);

    }

}
