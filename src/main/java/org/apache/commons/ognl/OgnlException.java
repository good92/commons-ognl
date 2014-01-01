package org.apache.commons.ognl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Superclass for OGNL exceptions, incorporating an optional encapsulated exception.
 */
public class OgnlException
    extends Exception
{
    private static final Logger log=Logger.getLogger(OgnlException.class.getName());

    private static final long serialVersionUID = -842845048743721078L;

    /**
     * The root evaluation of the expression when the exception was thrown
     */
    private Evaluation evaluation;

    /** Constructs an OgnlException with no message or encapsulated exception. */
    public OgnlException()
    {
        super();
    }

    /**
     * Constructs an OgnlException with the given message but no encapsulated exception.
     * 
     * @param msg the exception's detail message
     */
    public OgnlException( String msg )
    {
        super( msg );
    }

    /**
     * Constructs a new OgnlException  with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public OgnlException( String msg, Throwable cause )
    {
        super(msg,cause);
    }

    /**
     * Returns the encapsulated exception, or null if there is none.
     * 
     * @return the encapsulated exception
     * @deprecated  As of release 4.0, replaced by {@link #getCause()}
     */
    @Deprecated
    public Throwable getReason()
    {
        return getCause();
    }

    /**
     * Returns the Evaluation that was the root evaluation when the exception was thrown.
     * 
     * @return The {@link Evaluation}.
     */
    public Evaluation getEvaluation()
    {
        return evaluation;
    }

    /**
     * Sets the Evaluation that was current when this exception was thrown.
     * 
     * @param value The {@link Evaluation}.
     */
    public void setEvaluation( Evaluation value )
    {
        evaluation = value;
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on System.err.
     */
    @Override
    public void printStackTrace()
    {
        log.log(Level.WARNING,"", this);
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on the given print stream.
     */
    @Override
    public void printStackTrace( java.io.PrintStream s )
    {
        synchronized ( s )
        {
            super.printStackTrace( s );
        }
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on the given print writer.
     */
    @Override
    public void printStackTrace( java.io.PrintWriter s )
    {
        synchronized ( s )
        {
            super.printStackTrace( s );
        }
    }
}
