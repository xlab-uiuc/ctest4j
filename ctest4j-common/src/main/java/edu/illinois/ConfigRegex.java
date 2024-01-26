package edu.illinois;

import java.util.ArrayList;
import java.util.Stack;


/**
 * Author: Shuai Wang
 * Date:  11/14/23
 */
public class ConfigRegex {
    private String regexString;

    public ConfigRegex() {
        regexString = "";
    }

    public ConfigRegex(String regexString) {
        this.regexString = regexString;
    }

    public String getRegexString() {
        return regexString;
    }

    public void setRegexString(String regexString) {
        this.regexString = regexString;
    }

    public ArrayList<String> getParameters() {
        return extractParameters(regexString);
    }

    private static ArrayList<String> extractParameters(String regex) {
        ArrayList<String> parameters = new ArrayList<>();

        for (String part : splitTopLevelOr(regex)) {
            generateCombinations("", part, parameters);
        }

        return parameters;
    }

    private static ArrayList<String> splitTopLevelOr(String regex) {
        ArrayList<String> parts = new ArrayList<>();
        int start = 0;
        Stack<Integer> bracketStack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            if (regex.charAt(i) == '(') bracketStack.push(i);
            else if (regex.charAt(i) == ')') {
                if (!bracketStack.isEmpty()) bracketStack.pop();
            } else if (bracketStack.isEmpty() && regex.charAt(i) == '|') {
                parts.add(regex.substring(start, i));
                start = i + 1;
            }
        }

        parts.add(regex.substring(start));
        return parts;
    }

    private static void generateCombinations(String prefix, String remainingRegex, ArrayList<String> parameters) {
        if (!remainingRegex.contains("(")) {
            parameters.add(prefix + remainingRegex);
            return;
        }

        int openBracketIndex = remainingRegex.indexOf('(');
        int closeBracketIndex = findClosingBracketIndex(remainingRegex, openBracketIndex);
        String before = remainingRegex.substring(0, openBracketIndex);
        String choicePart = remainingRegex.substring(openBracketIndex + 1, closeBracketIndex);
        String after = remainingRegex.substring(closeBracketIndex + 1);

        for (String choice : choicePart.split("\\|")) {
            generateCombinations(prefix + before, choice + after, parameters);
        }
    }

    private static int findClosingBracketIndex(String s, int openIndex) {
        int balance = 1;
        for (int i = openIndex + 1; i < s.length(); i++) {
            if (s.charAt(i) == '(') balance++;
            else if (s.charAt(i) == ')') {
                balance--;
                if (balance == 0) return i;
            }
        }
        return -1; // Not found
    }

}