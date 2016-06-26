package com.brandon.apps.groupstudio.assets;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Add;
import org.nfunk.jep.function.Divide;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * Created by Brandon on 12/4/2015.
 */
public class MedianFunction extends PostfixMathCommand {
    private Add adder = new Add();
    private Divide divider = new Divide();
    public MedianFunction() {
        numberOfParameters = -1;
    }

    @Override
    public void run(Stack stack) throws ParseException {
        checkStack(stack);// check the stack

        if (curNumberOfParameters < 1) throw new ParseException("No arguments for Sum");
        // initialize the result to the first argument
        List<Double> list = new ArrayList<Double>();
        list.add((Double) stack.pop());
        int i = 1;

        // repeat summation for each one of the current parameters
        while (i < curNumberOfParameters) {
            // get the parameter from the stack
            list.add((Double) stack.pop());
            i++;
        }
        Collections.sort(list);
        Collections.reverse(list);
        if ( (list.size() & 1) == 0 ) {
            Object sum = adder.add(list.get(list.size() / 2), list.get((list.size() / 2) + 1));
            Object median =  divider.div(sum, 2);
            stack.push(median);
        } else {
            Object median =  list.get((int)Math.ceil(list.size() / 2));
            stack.push(median);
        }

    }
}
