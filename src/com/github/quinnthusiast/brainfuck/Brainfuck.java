package com.github.quinnthusiast.brainfuck;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Brainfuck interpreter, licensed under the MIT license
 * 
 * @author Quinnthusiast
 */
public class Brainfuck implements Runnable
{
    private static final int  CELLS = 30_000; // @formatter:off
    private static final char GET   = ',',
                              PUT   = '.',
                              LEFT  = '<',
                              RIGHT = '>',
                              ADD   = '+',
                              SUB   = '-',
                              OPEN  = '[',
                              CLOSE = ']';      // @formatter:on

    private final InputStreamReader  input;
    private final OutputStreamWriter output;
    private final String             program;

    private final AtomicBoolean lock = new AtomicBoolean(false);
    
    private final char[]         cells = new char[CELLS];
    private final Deque<Integer> stack = new ArrayDeque<>();
    private int                  ptr;

    public Brainfuck(InputStreamReader input, OutputStreamWriter output, String program)
    {
        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    public void run()
    {
        if (lock.getAndSet(true))
        {
            throw new RuntimeException("Brainfuck instances may not be re-used!");
        }
        
        try
        {
            int ip = 0;
            while (ip < program.length())
            {
                char opcode = program.charAt(ip);
                switch (opcode)
                {
                    case GET:
                        int in = input.read();
                        if (in != -1)
                        {
                            cells[ptr] = (char) in;
                        }
                        break;

                    case PUT:
                        output.write(cells[ptr]);
                        break;

                    case LEFT: // if pointing to zero, wrap around
                        if (ptr-- == 0)
                        {
                            ptr += CELLS;
                        }
                        break;

                    case RIGHT: // if pointing to the end, wrap around
                        if (++ptr == CELLS)
                        {
                            ptr = 0;
                        }
                        break;

                    case ADD:
                        cells[ptr]++;
                        break;

                    case SUB:
                        cells[ptr]--;
                        break;

                    case OPEN:
                        if (cells[ptr] != 0)
                        {
                            stack.push(ip);
                        }
                        else
                        {
                            int loops = 0; // counts the number of open braces
                            for (int i = ip; i < program.length(); ++i)
                            { // iterate over the program, once loops == 0, we have found the matching
                              // parenthesis
                                char j = program.charAt(i); // for the first iteration this will be '['
                                if (j == OPEN)              // so we don't have to worry about it not counting properly
                                {
                                    loops++;
                                }
                                else if (j == CLOSE)
                                {
                                    loops--;
                                }

                                if (loops == 0)
                                {
                                    loops = i; // set the location of the matching brace in loops
                                    break;
                                }
                            }
                            ip = loops; // jump to the brace (we always add 1 after every iteration, so we'll be after
                                        // the brace)
                        }
                        break;

                    case CLOSE:
                        if (cells[ptr] != 0)
                        {
                            ip = stack.peek();
                        }
                        else // (ip == 0), let the ip get past this loop, but "remove" this loop by popping
                        {
                            stack.pop();
                        }
                        break;

                    default: // ignore the character
                }

                ip++; // increment the ip
            }
            
            output.flush(); // make sure everything is written to stdout
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
