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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTAdd
    extends NumericExpression
{
    public ASTAdd( int id )
    {
        super( id );
    }

    public ASTAdd( OgnlParser p, int id )
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
        Object result = _children[0].getValue( context, source );

        for ( int i = 1; i < _children.length; ++i )
            result = OgnlOps.add( result, _children[i].getValue( context, source ) );

        return result;
    }

    public String getExpressionOperator( int index )
    {
        return "+";
    }

    boolean isWider( NodeType type, NodeType lastType )
    {
        if ( lastType == null )
            return true;

        // System.out.println("checking isWider(" + type.getGetterClass() + " , " + lastType.getGetterClass() + ")");

        if ( String.class.isAssignableFrom( lastType.getGetterClass() ) )
            return false;

        if ( String.class.isAssignableFrom( type.getGetterClass() ) )
            return true;

        if ( _parent != null && String.class.isAssignableFrom( type.getGetterClass() ) )
            return true;

        if ( String.class.isAssignableFrom( lastType.getGetterClass() ) && Object.class == type.getGetterClass() )
            return false;

        if ( _parent != null && String.class.isAssignableFrom( lastType.getGetterClass() ) )
            return false;
        else if ( _parent == null && String.class.isAssignableFrom( lastType.getGetterClass() ) )
            return true;
        else if ( _parent == null && String.class.isAssignableFrom( type.getGetterClass() ) )
            return false;

        if ( BigDecimal.class.isAssignableFrom( type.getGetterClass() )
            || BigInteger.class.isAssignableFrom( type.getGetterClass() ) )
            return true;

        if ( BigDecimal.class.isAssignableFrom( lastType.getGetterClass() )
            || BigInteger.class.isAssignableFrom( lastType.getGetterClass() ) )
            return false;

        if ( Double.class.isAssignableFrom( type.getGetterClass() ) )
            return true;

        if ( Integer.class.isAssignableFrom( type.getGetterClass() )
            && Double.class.isAssignableFrom( lastType.getGetterClass() ) )
            return false;

        if ( Float.class.isAssignableFrom( type.getGetterClass() )
            && Integer.class.isAssignableFrom( lastType.getGetterClass() ) )
            return true;

        return true;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        try
        {
            String result = "";
            NodeType lastType = null;

            // go through once to determine the ultimate type

            if ( ( _children != null ) && ( _children.length > 0 ) )
            {
                Class currType = context.getCurrentType();
                Class currAccessor = context.getCurrentAccessor();

                Object cast = context.get( ExpressionCompiler.PRE_CAST );

                for ( int i = 0; i < _children.length; ++i )
                {
                    _children[i].toGetSourceString( context, target );

                    if ( NodeType.class.isInstance( _children[i] )
                        && ( (NodeType) _children[i] ).getGetterClass() != null
                        && isWider( (NodeType) _children[i], lastType ) )
                    {
                        lastType = (NodeType) _children[i];
                    }
                }

                context.put( ExpressionCompiler.PRE_CAST, cast );

                context.setCurrentType( currType );
                context.setCurrentAccessor( currAccessor );
            }

            // reset context since previous children loop would have changed it

            context.setCurrentObject( target );

            if ( ( _children != null ) && ( _children.length > 0 ) )
            {

                for ( int i = 0; i < _children.length; ++i )
                {
                    if ( i > 0 )
                        result += " " + getExpressionOperator( i ) + " ";

                    String expr = _children[i].toGetSourceString( context, target );

                    if ( ( expr != null && "null".equals( expr ) )
                        || ( !ASTConst.class.isInstance( _children[i] ) && ( expr == null || expr.trim().length() <= 0 ) ) )
                    {
                        expr = "null";
                    }

                    // System.out.println("astadd child class: " + _children[i].getClass().getName() +
                    // " and return expr: " + expr);

                    if ( ASTProperty.class.isInstance( _children[i] ) )
                    {
                        expr = ExpressionCompiler.getRootExpression( _children[i], context.getRoot(), context ) + expr;
                        context.setCurrentAccessor( context.getRoot().getClass() );
                    }
                    else if ( ASTMethod.class.isInstance( _children[i] ) )
                    {
                        String chain = (String) context.get( "_currentChain" );
                        String rootExpr =
                            ExpressionCompiler.getRootExpression( _children[i], context.getRoot(), context );

                        // System.out.println("astadd chains is >>" + chain + "<< and rootExpr is >>" + rootExpr +
                        // "<<");

                        // dirty fix for overly aggressive casting dot operations
                        if ( rootExpr.endsWith( "." ) && chain != null && chain.startsWith( ")." ) )
                        {
                            chain = chain.substring( 1, chain.length() );
                        }

                        expr = rootExpr + ( chain != null ? chain + "." : "" ) + expr;
                        context.setCurrentAccessor( context.getRoot().getClass() );

                    }
                    else if ( ExpressionNode.class.isInstance( _children[i] ) )
                    {
                        expr = "(" + expr + ")";
                    }
                    else if ( ( _parent == null || !ASTChain.class.isInstance( _parent ) )
                        && ASTChain.class.isInstance( _children[i] ) )
                    {
                        String rootExpr =
                            ExpressionCompiler.getRootExpression( _children[i], context.getRoot(), context );

                        if ( !ASTProperty.class.isInstance( _children[i].jjtGetChild( 0 ) ) && rootExpr.endsWith( ")" )
                            && expr.startsWith( ")" ) )
                            expr = expr.substring( 1, expr.length() );

                        expr = rootExpr + expr;
                        context.setCurrentAccessor( context.getRoot().getClass() );

                        String cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
                        if ( cast == null )
                            cast = "";

                        expr = cast + expr;
                    }

                    // turn quoted characters into quoted strings

                    if ( context.getCurrentType() != null && context.getCurrentType() == Character.class
                        && ASTConst.class.isInstance( _children[i] ) )
                    {
                        expr = expr.replaceAll( "'", "\"" );
                        context.setCurrentType( String.class );
                    }
                    else
                    {

                        if ( !ASTVarRef.class.isAssignableFrom( _children[i].getClass() )
                            && !ASTProperty.class.isInstance( _children[i] )
                            && !ASTMethod.class.isInstance( _children[i] )
                            && !ASTSequence.class.isInstance( _children[i] )
                            && !ASTChain.class.isInstance( _children[i] )
                            && !NumericExpression.class.isAssignableFrom( _children[i].getClass() )
                            && !ASTStaticField.class.isInstance( _children[i] )
                            && !ASTStaticMethod.class.isInstance( _children[i] )
                            && !ASTTest.class.isInstance( _children[i] ) )
                        {
                            if ( lastType != null && String.class.isAssignableFrom( lastType.getGetterClass() ) )
                            {
                                // System.out.println("Input expr >>" + expr + "<<");
                                expr = expr.replaceAll( "&quot;", "\"" );
                                expr = expr.replaceAll( "\"", "'" );
                                expr = "\"" + expr + "\"";
                                // System.out.println("Expr now >>" + expr + "<<");
                            }
                        }
                    }

                    result += expr;

                    // hanlde addition for numeric types when applicable or just string concatenation

                    if ( ( lastType == null || !String.class.isAssignableFrom( lastType.getGetterClass() ) )
                        && !ASTConst.class.isAssignableFrom( _children[i].getClass() )
                        && !NumericExpression.class.isAssignableFrom( _children[i].getClass() ) )
                    {
                        if ( context.getCurrentType() != null
                            && Number.class.isAssignableFrom( context.getCurrentType() )
                            && !ASTMethod.class.isInstance( _children[i] ) )
                        {
                            if ( ASTVarRef.class.isInstance( _children[i] )
                                || ASTProperty.class.isInstance( _children[i] )
                                || ASTChain.class.isInstance( _children[i] ) )
                                result += ".";

                            result += OgnlRuntime.getNumericValueGetter( context.getCurrentType() );
                            context.setCurrentType( OgnlRuntime.getPrimitiveWrapperClass( context.getCurrentType() ) );
                        }
                    }

                    if ( lastType != null )
                        context.setCurrentAccessor( lastType.getGetterClass() );
                }
            }

            if ( _parent == null || ASTSequence.class.isAssignableFrom( _parent.getClass() ) )
            {
                if ( _getterClass != null && String.class.isAssignableFrom( _getterClass ) )
                    _getterClass = Object.class;
            }
            else
            {
                context.setCurrentType( _getterClass );
            }

            try
            {
                Object contextObj = getValueBody( context, target );
                context.setCurrentObject( contextObj );
            }
            catch ( Throwable t )
            {
                throw OgnlOps.castToRuntime( t );
            }

            return result;

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }
    }
    
    public <R,P> R accept(NodeVisitor<? extends R, ? super P> visitor, P data) 
    {
        return visitor.visit(this, data);
    }
}
