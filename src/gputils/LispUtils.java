package gputils;

import ec.gp.GPNode;
import ec.gp.GPTree;
import gputils.terminal.ConstantTerminal;
import gputils.terminal.PrimitiveSet;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The utility methods for io expressions.
 */

public class LispUtils {

    /**
     * Read a io expression as a string, and return a corresponding GP tree.
     * @param expression the io expression as a string.
     * @param primitiveSet the primitive set.
     * @return the GP tree.
     */
    public static GPTree parseExpression(String expression,
                                         PrimitiveSet primitiveSet) {
        GPTree tree = new GPTree();

        expression = expression.trim();

        tree.child = parseNode(expression, primitiveSet);

        return tree;
    }

    private static GPNode parseNode(String expression,
                                    PrimitiveSet primitiveSet) {
        GPNode node;

        if (expression.charAt(0) == '(') {
            int nextWhiteSpaceIdx = expression.indexOf(' ');
            String func = expression.substring(1, nextWhiteSpaceIdx);
            String argsString = expression.substring(nextWhiteSpaceIdx + 1,
                    expression.length() - 1);
            List<String> args = LispUtils.splitArguments(argsString);

            node = primitiveSet.get(func).lightClone();

            if (func.equals("+") || func.equals("-") || func.equals("*") ||
                    func.equals("/") || func.equals("max") || func.equals("min")) {
                node.children[0] = parseNode(args.get(0), primitiveSet);
                node.children[1] = parseNode(args.get(1), primitiveSet);
                node.children[0].parent = node;
                node.children[1].parent = node;
                node.children[0].argposition = 0;
                node.children[1].argposition = 1;
            }
            else if (func.equals("if")) {
                node.children[0] = parseNode(args.get(0), primitiveSet);
                node.children[1] = parseNode(args.get(1), primitiveSet);
                node.children[2] = parseNode(args.get(2), primitiveSet);
                node.children[0].parent = node;
                node.children[1].parent = node;
                node.children[2].parent = node;
                node.children[0].argposition = 0;
                node.children[1].argposition = 1;
                node.children[2].argposition = 2;
            }
        }
        else {
            if (NumberUtils.isNumber(expression)) {
                // only keep two digits
                String chunked = String.format("%.2f", Double.valueOf(expression));
                node = new ConstantTerminal(Double.valueOf(chunked));
            }
            else {
                node = primitiveSet.get(expression).lightClone();
            }
            node.children = new GPNode[0];
        }

        return node;
    }

    /**
     * Simplify a io expression by applying algebreic simplification rules,
     * such as a/a = 1, a-a = 0, max(a,a) = a, etc.
     * @param expression the expression to be simplified.
     * @return the simplified expression.
     */
    public static String simplifyExpression(String expression) {
        expression = expression.trim();

        if (expression.charAt(0) == '(') {
            int nextWhiteSpaceIdx = expression.indexOf(' ');
            String func = expression.substring(1, nextWhiteSpaceIdx);
            String argsString = expression.substring(nextWhiteSpaceIdx + 1,
                    expression.length() - 1);
            List<String> args = LispUtils.splitArguments(argsString);

            for (int i = 0; i < args.size(); i++) {
                String simplifiedArg = simplifyExpression(args.get(i));
                args.set(i, simplifiedArg);
            }

            if (func.equals("-")) {
                if (args.get(0).equals(args.get(1))) {
                    // a - a = 0
                    return "0";
                }

                if (args.get(1).equals("0")) {
                    // a - 0 = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));
                    return String.valueOf(a - b);
                }
            }
            else if (func.equals("/")) {
                if (args.get(0).equals(args.get(1))) {
                    // a / a = 1
                    return "1";
                }

                if (args.get(1).equals("0")) {
                    // a / 0 = 1 (protective division)
                    return "1";
                }

                if (args.get(1).equals("1")) {
                    // a / 1 = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));

                    if (Double.compare(b, 0d) == 0)
                        return "" + 1;

                    return String.valueOf(a / b);
                }
            }
            else if (func.equals("+")) {
                if (args.get(0).equals("0")) {
                    // 0 + a = a
                    return args.get(1);
                }

                if (args.get(1).equals("0")) {
                    // a + 0 = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));
                    return String.valueOf(a + b);
                }
            }
            else if (func.equals("*")) {
                if (args.get(0).equals("1")) {
                    // 1 * a = a
                    return args.get(1);
                }

                if (args.get(1).equals("1")) {
                    // a * 1 = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));
                    return String.valueOf(a * b);
                }
            }
            else if (func.equals("max")) {
                if (args.get(0).equals(args.get(1))) {
                    // max(a, a) = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));
                    double c = a;
                    if (c < b)
                        c = b;
                    return String.valueOf(c);
                }
            }
            else if (func.equals("min")) {
                if (args.get(0).equals(args.get(1))) {
                    // min(a, a) = a
                    return args.get(0);
                }

                if (NumberUtils.isNumber(args.get(0)) && NumberUtils.isNumber(args.get(1))) {
                    // do the calculation
                    double a = Double.valueOf(args.get(0));
                    double b = Double.valueOf(args.get(1));
                    double c = a;
                    if (c > b)
                        c = b;
                    return String.valueOf(c);
                }
            }
            else if (func.equals("if")) {
                if (args.get(1).equals(args.get(2))) {
                    // the second and third arguments are the same
                    return args.get(1);
                }

                if (NumberUtils.isNumber(args.get(0))) {
                    // the first argument is a number
                    double num = Double.valueOf(args.get(0));
                    if (num > 0) {
                        // always positive
                        return args.get(1);
                    }
                    else {
                        // always non-positive
                        return args.get(2);
                    }
                }
            }

            String simplifiedExpression = "(" + func;
            for (String arg : args) {
                simplifiedExpression += " " + arg;
            }
            simplifiedExpression += ")";

            return simplifiedExpression;
        }
        else {
            return expression;
        }
    }

    /**
     * Split a io string to get a list of arguments
     * @param argsString the io string
     * @return a list of arguments of the string
     */
    public static List<String> splitArguments(String argsString) {
        List<String> args = new ArrayList<>();
        int head = 0;

        while (head < argsString.length()) {
            if (argsString.charAt(head) == '(') {
                int unbalance = 1;
                for (int i = head + 1; i < argsString.length(); i++) {
                    if (argsString.charAt(i) == '(') {
                        unbalance ++;
                    }

                    if (argsString.charAt(i) == ')') {
                        unbalance --;

                        if (unbalance == 0) {
                            args.add(argsString.substring(head, i + 1));
                            head = i + 2;
                            break;
                        }
                    }
                }
            }
            else {
                int tail = argsString.indexOf(' ', head);
                if (tail == -1)
                    tail = argsString.length();
                args.add(argsString.substring(head, tail));
                head = tail + 1;
            }
        }

        return args;
    }

    /**
     * Find all the terminals of a Lisp expression.
     * @param expression the Lisp expression.
     * @return all the terminals as a list.
     */
    public static List<String> terminals(String expression) {
        List<String> terminals = new ArrayList<>();

        if (expression.charAt(0) == '(') {
            int nextWhiteSpaceIdx = expression.indexOf(' ');
            String func = expression.substring(1, nextWhiteSpaceIdx);
            String argsString = expression.substring(nextWhiteSpaceIdx + 1,
                    expression.length() - 1);
            List<String> args = LispUtils.splitArguments(argsString);

            for (String arg : args) {
                List<String> argTerminals = terminals(arg);

                terminals.addAll(argTerminals);
            }
        }
        else {
            // it is a terminal itself
            if (!NumberUtils.isNumber(expression)) {
                terminals.add(expression);
            }
        }

        return terminals;
    }
}
