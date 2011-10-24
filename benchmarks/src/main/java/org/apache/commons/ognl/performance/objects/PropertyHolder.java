/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.ognl.performance.objects;

/**
 * Simple class used to test various kind of property resolutions.
 */
public class PropertyHolder
{

    String _value = "";

    String _search = "foo";

    public String getValue()
    {
        return _value;
    }

    public void setValue( String value )
    {
        _value = value;
    }

    public boolean hasValue()
    {
        return _value != null && _value.length() > 0;
    }

    public void setSearch( String value )
    {
        _search = value;
    }

    public String getSearch()
    {
        return _search;
    }

    public void search()
    {
    }
}
