package com.carlgira.oracle.bpel.flowchart.mermaid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlgira on 07/03/2016.
 * Mermaid.js Link object class
 */
public class Link {
    public String beginNode;
    public String endNode;
    public String message;
    public String subGraph;

    private static Pattern patternLink1 = Pattern.compile("(.*)-->\\|(.*)\\|(.*)");
    private static Pattern patternLink2 = Pattern.compile("(.*)-->(.*)");

    public Link(String beginNode, String endNode, String message) {
        this.beginNode = beginNode;
        this.endNode = endNode;
        this.message = message;
    }

    public Link(String beginNode, String endNode) {
        this.beginNode = beginNode;
        this.endNode = endNode;
    }

    public Link() {
    }

    public static Link parseLink(String linkString) {
        Matcher matcherLink1 = patternLink1.matcher(linkString);
        Matcher matcherLink2 = patternLink2.matcher(linkString);

        if(matcherLink1.find()){
            return new Link(matcherLink1.group(1), matcherLink1.group(3), matcherLink1.group(2) );
        }
        else if(matcherLink2.find()){
            return new Link(matcherLink2.group(1), matcherLink2.group(2));
        }

        return null;
    }

    @Override
    public String toString() {
        return "Link{" +
                "beginNode='" + beginNode + '\'' +
                ", endNode='" + endNode + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}