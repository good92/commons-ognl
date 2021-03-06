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

import java.util.HashMap;
import java.util.Map;

/**
 * Default class resolution. Uses Class.forName() to look up classes by name. It also looks in the "java.lang" package
 * if the class named does not give a package specifier, allowing easier usage of these classes.
 * 
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class DefaultClassResolver
    implements ClassResolver
{
    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>( 101 );

    /**
     * {@inheritDoc}
     */
    public Class<?> classForName( String className, Map<String, Object> context )
        throws ClassNotFoundException
    {
        Class<?> result = null;

        if ( ( result = classes.get( className ) ) == null )
        {
            try
            {
                result = Class.forName( className );
            }
            catch ( ClassNotFoundException ex )
            {
                if ( className.indexOf( '.' ) == -1 )
                {
                    result = Class.forName( "java.lang." + className );
                    classes.put( "java.lang." + className, result );
                }
            }
            classes.put( className, result );
        }
        return result;
    }
}
