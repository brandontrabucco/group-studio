package com.brandon.apps.groupstudio.assets;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.Subtract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by Brandon on 12/4/2015.
 */
public class MaxFunction extends PostfixMathCommand {
    public MaxFunction() {
        numberOfParameters = -1;
    }

    @Override
    public void run(Stack stack) throws ParseException {
        checkStack(stack);// check the stack

        if (curNumberOfParameters < 1) throw new ParseException("No arguments for Sum");
        // initialize the result to the first argument
        List<Double> list = new ArrayList<Double>();
        list.add((Double)stack.pop());
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < curNumberOfParameters) {
            // get the parameter from the stack
            list.add((Double) stack.pop());
            i++;
        }
        Collections.sort(list);
        Collections.reverse(list);
        // push the result on the inStack
        stack.push(list.get(0));
    }
}
