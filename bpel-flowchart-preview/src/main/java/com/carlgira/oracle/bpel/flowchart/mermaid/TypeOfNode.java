package com.carlgira.oracle.bpel.flowchart.mermaid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emateo on 08/03/2016.
 */
public class TypeOfNode {

    public static String NODE_HT = "ht";
    public static String NODE_WS = "ws";
    public static String NODE_BPEL = "bpel";
    public static String NODE_INIT = "init";
    public static String NODE_FN = "fn";
    public static String NODE_OBJ = "obj";

    public static List<String> LIST_OF_TYPES;

    static {
        LIST_OF_TYPES = new ArrayList<>();
        LIST_OF_TYPES.add(NODE_HT);
        LIST_OF_TYPES.add(NODE_WS);
        LIST_OF_TYPES.add(NODE_BPEL);
        LIST_OF_TYPES.add(NODE_INIT);
        LIST_OF_TYPES.add(NODE_FN);
        LIST_OF_TYPES.add(NODE_OBJ);
    }
}
