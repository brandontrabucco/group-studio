package com.brandon.apps.groupstudio.assets;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Add;
import org.nfunk.jep.function.Divide;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

/**
 * Created by Brandon on 12/4/2015.
 */
public class AverageFunction extends PostfixMathCommand {
    private Add adder = new Add();
    private Divide divider = new Divide();
    public AverageFunction() {
        numberOfParameters = -1;
    }

    @Override
    public void run(Stack stack) throws ParseException {
        checkStack(stack);// check the stack

        if (curNumberOfParameters < 1) throw new ParseException("No arguments for Sum");
        // initialize the result to the first argument
        Object sum = stack.pop();
        Object param;
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < curNumberOfParameters) {
            // get the parameter from the stack
            param = stack.pop();// add it to the sum (order is important for String arguments)
            sum = adder.add(param, sum);
            i++;
        }
        Object quotient = divider.div(sum, curNumberOfParameters);
        // push the result on the inStack
        stack.push(quotient);
    }
}
