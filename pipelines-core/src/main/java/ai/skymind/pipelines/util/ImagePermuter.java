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

package ai.skymind.pipelines.util;

import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ImagePermuter {



    static int[] determinePermuteOrder(String startingOrder, String destinationOrder) {
        startingOrder = startingOrder.toLowerCase().trim();
        destinationOrder = destinationOrder.toLowerCase().trim();
        Preconditions.checkState(startingOrder.length() == 4 && destinationOrder.length() == 4,"Orders must be of length 4");
        Preconditions.checkState(startingOrder.contains("n") && destinationOrder.contains("n"),"One order is missing n");
        Preconditions.checkState(startingOrder.contains("c") && destinationOrder.contains("c"),"One order is missing c");
        Preconditions.checkState(startingOrder.contains("h") && destinationOrder.contains("h"),"One order is missing h");
        Preconditions.checkState(startingOrder.contains("w") && destinationOrder.contains("w"),"One order is missing w");

        boolean[] done = new boolean[4];
        int[] retPermuteOrder = new int[4];
        for(int i = 0;  i < 4; i++) {
            if(startingOrder.charAt(i) == destinationOrder.charAt(i)) {
                retPermuteOrder[i]= i;
                done[i] = true;
            }
            else {
                int destinationIdxOfCurrentStartingChar = destinationOrder.indexOf(startingOrder.charAt(i));
                retPermuteOrder[destinationIdxOfCurrentStartingChar] = i;
            }
        }

        return retPermuteOrder;
    }

    static String applyPermuteOrderToString(String origin,int[] permuteOrder) {
        StringBuilder sb = new StringBuilder();
        for (int value : permuteOrder) {
            sb.append(origin.charAt(value));
        }

        return sb.toString();
    }

    public static INDArray permuteOrder(INDArray input,String startingOrder,String destinationOrder) {
        return input.permute(determinePermuteOrder(startingOrder,destinationOrder));
    }


}
