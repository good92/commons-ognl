package org.apache.commons.ognl.internal;

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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;

/**
 * A class to support Commons OGNL Logging conversion from system out and err based logging.
 */
public class LoggingSupport
{
	private LoggingSupport(){}
	
	/**
	 * A logging Level to be used by logging statements which were commented 
	 * out. It is a lower value than Level.FINEST and will only show when 
	 * the filters are set accordingly.
	 */
	public static class LevelCommentedOut extends Level
	{
		private static final long serialVersionUID = -5563682889713594595L;
		private LevelCommentedOut(){super("OGNLCOMMENTEDOUT", FINEST.intValue()-1);}		
	}

	/**
	 * The logging Level constant for commented out log statements. 
	 * @see LevelCommentedOut
	 */
	public static final Level COMMENTEDOUT=new LevelCommentedOut();

	/**
	 * This is a base for expensive or dangerous parameters to logging. It will 
	 * absorb all exceptions and perform the operation only if the log message 
	 * is cleared by the filters and is presented to the log stream(s).
	 */
	public static abstract class Formatter
	{
		/**
		 * This method will allow any processing to generate the value for the 
		 * parameter place holder. It is expected that the logging framework 
		 * will use the toString method on the returned object.
		 * 
		 * @return the object to be formatted, in a parameter place holder.
		 * @throws Throwable
		 */
		public abstract Object format() throws Throwable;
		@Override
		public String toString()
		{
			Object v;
			try {v = format();} catch (Throwable e){v=e.toString();}
			return v==null?null:v.toString();
		}
	}
	/**
	 * A formatter to log a Node's value based on the OgnlContext.
	 */
	public static class FormatGetValue extends Formatter
	{
		private Node node;
		private OgnlContext context;
		public FormatGetValue(Node node, OgnlContext context){this.node=node;this.context=context;}
		
		/**
		 * node.getValue(context, context.getRoot()).getClass()
		 */
		@Override
		public Object format() throws OgnlException
		{
			return node.getValue(context, context.getRoot()).getClass();
		}
	}
}
