/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTAnd
    extends BooleanExpression
{
    public ASTAnd( int id )
    {
        super( id );
    }

    public ASTAnd( OgnlParser p, int id )
    {
        super( p, id );
    }

    public void jjtClose()
    {
        flattenTree();
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = null;
        int last = _children.length - 1;
        for ( int i = 0; i <= last; ++i )
        {
            result = _children[i].getValue( context, source );

            if ( i != last && !OgnlOps.booleanValue( result ) )
                break;
        }

        return result;
    }

    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        int last = _children.length - 1;

        for ( int i = 0; i < last; ++i )
        {
            Object v = _children[i].getValue( context, target );

            if ( !OgnlOps.booleanValue( v ) )
                return;
        }

        _children[last].setValue( context, target, value );
    }

    public String getExpressionOperator( int index )
    {
        return "&&";
    }

    public Class getGetterClass()
    {
        return null;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        if ( _children.length != 2 )
            throw new UnsupportedCompilationException( "Can only compile boolean expressions with two children." );

        String result = "";

        try
        {

            String first = OgnlRuntime.getChildSource( context, target, _children[0] );
            if ( !OgnlOps.booleanValue( context.getCurrentObject() ) )
            {
                throw new UnsupportedCompilationException(
                                                           "And expression can't be compiled until all conditions are true." );
            }

            if ( !OgnlRuntime.isBoolean( first ) && !context.getCurrentType().isPrimitive() )
                first = OgnlRuntime.getCompiler().createLocalReference( context, first, context.getCurrentType() );

            String second = OgnlRuntime.getChildSource( context, target, _children[1] );
            if ( !OgnlRuntime.isBoolean( second ) && !context.getCurrentType().isPrimitive() )
                second = OgnlRuntime.getCompiler().createLocalReference( context, second, context.getCurrentType() );

            result += "(org.apache.commons.ognl.OgnlOps.booleanValue(" + first + ")";

            result += " ? ";

            result += " ($w) (" + second + ")";
            result += " : ";

            result += " ($w) (" + first + ")";

            result += ")";

            context.setCurrentObject( target );
            context.setCurrentType( Object.class );
        }
        catch ( NullPointerException e )
        {

            throw new UnsupportedCompilationException( "evaluation resulted in null expression." );
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        if ( _children.length != 2 )
            throw new UnsupportedCompilationException( "Can only compile boolean expressions with two children." );

        String pre = (String) context.get( "_currentChain" );
        if ( pre == null )
            pre = "";

        String result = "";

        try
        {

            if ( !OgnlOps.booleanValue( _children[0].getValue( context, target ) ) )
            {
                throw new UnsupportedCompilationException(
                                                           "And expression can't be compiled until all conditions are true." );
            }

            String first =
                ExpressionCompiler.getRootExpression( _children[0], context.getRoot(), context ) + pre
                    + _children[0].toGetSourceString( context, target );

            _children[1].getValue( context, target );

            String second =
                ExpressionCompiler.getRootExpression( _children[1], context.getRoot(), context ) + pre
                    + _children[1].toSetSourceString( context, target );

            if ( !OgnlRuntime.isBoolean( first ) )
                result += "if(org.apache.commons.ognl.OgnlOps.booleanValue(" + first + ")){";
            else
                result += "if(" + first + "){";

            result += second;
            result += "; } ";

            context.setCurrentObject( target );
            context.setCurrentType( Object.class );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result;
    }
    
    public <R,P> R accept(NodeVisitor<? extends R, ? super P> visitor, P data) 
    {
        return visitor.visit(this, data);
    }
}
