package com.carlgira.oracle.bpel.test.flowchart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by emateo on 07/03/2016.
 */
public class Node {
    public String id;
    public String type;
    public String message;
    public String originalLine;
    public Boolean active = false;
    public Integer state = 1;

    private static Pattern patternRoundedNode = Pattern.compile("(^[obj_|ht_|bpel_|ws_|init_|fn_]*)([^\\(\\[\\{>]+)(>|\\[|\\{|\\(|\\(\\()([^\\(\\)\\[\\]\\}]+)(]|\\]|\\}|\\)\\)|\\))");

    public Node(String id, String type, String message, String originalLine) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.originalLine = originalLine;
    }

    public Node() {
    }

    public static Node parseNode(String nodeString){

        Matcher matcher = patternRoundedNode.matcher(nodeString);

        if(matcher.find()){
            String type = matcher.group(1);
            return new Node(matcher.group(2),type.substring(0,type.length()-1), matcher.group(4),nodeString);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return !(id != null ? !id.equals(node.id) : node.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}