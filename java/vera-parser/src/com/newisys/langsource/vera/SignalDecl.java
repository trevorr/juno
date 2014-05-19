/*
 * Parser and Source Model for the OpenVera (TM) language
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * VERA and OpenVera are trademarks or registered trademarks of Synopsys, Inc.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.langsource.vera;

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.vera.VeraSignalDirection;
import com.newisys.langschema.vera.VeraSignalKind;
import com.newisys.langschema.vera.VeraVCAKind;
import com.newisys.langschema.vera.VeraVCAQValue;
import com.newisys.verilog.EdgeSet;

/**
 * Interface signal declaration.
 * 
 * @author Trevor Robinson
 */
public final class SignalDecl
    extends VeraSourceObjectImpl
{
    private VeraSignalDirection direction;
    private ExpressionDecl highBitExpr;
    private String identifier;
    private VeraSignalKind kind;
    private EdgeSet sampleEdges = EdgeSet.NO_EDGE;
    private EdgeSet driveEdges = EdgeSet.NO_EDGE;
    private final List skews = new LinkedList(); // List<SignalSkewDecl>
    private ExpressionDecl depthExpr;
    private VeraVCAKind vcaKind = VeraVCAKind.NONE;
    private VeraVCAQValue vcaQValue = VeraVCAQValue.HOLD;
    private String hdlNode;

    public VeraSignalDirection getDirection()
    {
        return direction;
    }

    public void setDirection(VeraSignalDirection direction)
    {
        this.direction = direction;
    }

    public ExpressionDecl getHighBitExpr()
    {
        return highBitExpr;
    }

    public void setHighBitExpr(ExpressionDecl highBitExpr)
    {
        this.highBitExpr = highBitExpr;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public VeraSignalKind getKind()
    {
        return kind;
    }

    public void setKind(VeraSignalKind kind)
    {
        this.kind = kind;
    }

    public EdgeSet getSampleEdges()
    {
        return sampleEdges;
    }

    public void setSampleEdges(EdgeSet sampleEdges)
    {
        this.sampleEdges = sampleEdges;
    }

    public EdgeSet getDriveEdges()
    {
        return driveEdges;
    }

    public void setDriveEdges(EdgeSet driveEdges)
    {
        this.driveEdges = driveEdges;
    }

    public void addSkew(SignalSkewDecl skew)
    {
        skews.add(skew);
    }

    public List getSkews()
    {
        return skews;
    }

    public ExpressionDecl getDepthExpr()
    {
        return depthExpr;
    }

    public void setDepthExpr(ExpressionDecl depthExpr)
    {
        this.depthExpr = depthExpr;
    }

    public VeraVCAKind getVCAKind()
    {
        return vcaKind;
    }

    public void setVCAKind(VeraVCAKind vcaKind)
    {
        this.vcaKind = vcaKind;
    }

    public VeraVCAQValue getVCAQValue()
    {
        return vcaQValue;
    }

    public void setVCAQValue(VeraVCAQValue vcaQValue)
    {
        this.vcaQValue = vcaQValue;
    }

    public String getHDLNode()
    {
        return hdlNode;
    }

    public void setHDLNode(String hdlNode)
    {
        this.hdlNode = hdlNode;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
