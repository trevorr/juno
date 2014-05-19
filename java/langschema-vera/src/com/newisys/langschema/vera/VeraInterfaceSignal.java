/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.langschema.vera;

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NamedObject;
import com.newisys.verilog.EdgeSet;

/**
 * Represents a signal declared in a Vera interface.
 * 
 * @author Trevor Robinson
 */
public final class VeraInterfaceSignal
    extends VeraSchemaObjectImpl
    implements NamedObject, VeraStructuredTypeMember
{
    private static final long serialVersionUID = 3832904355660903475L;

    private final VeraName name;
    private final VeraSignalKind kind;
    private final VeraSignalDirection direction;
    private final int width;
    private final VeraType type;
    private EdgeSet sampleEdges;
    private int sampleSkew;
    private int sampleDepth;
    private EdgeSet driveEdges;
    private int driveSkew;
    private final List<VeraSurrXParams> surrXParamsList;
    private VeraVCAKind vcaKind;
    private VeraVCAQValue vcaQValue;
    private String hdlNode;
    private VeraInterfaceType intf;

    public VeraInterfaceSignal(
        VeraSchema schema,
        VeraName name,
        VeraSignalKind kind,
        VeraSignalDirection direction,
        int width)
    {
        super(schema);
        this.name = name;
        this.kind = kind;
        this.direction = direction;
        this.width = width;
        this.type = new VeraFixedBitVectorType(schema, width);
        this.sampleEdges = EdgeSet.NO_EDGE;
        this.driveEdges = EdgeSet.NO_EDGE;
        this.surrXParamsList = new LinkedList<VeraSurrXParams>();
        this.sampleDepth = 0;
        this.vcaKind = VeraVCAKind.NONE;
        this.vcaQValue = VeraVCAQValue.HOLD;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraSignalKind getKind()
    {
        return kind;
    }

    public VeraSignalDirection getDirection()
    {
        return direction;
    }

    public int getWidth()
    {
        return width;
    }

    public VeraType getType()
    {
        return type;
    }

    public EdgeSet getSampleEdges()
    {
        return sampleEdges;
    }

    public void setSampleEdges(EdgeSet sampleEdges)
    {
        this.sampleEdges = sampleEdges;
    }

    public int getSampleSkew()
    {
        return sampleSkew;
    }

    public void setSampleSkew(int sampleSkew)
    {
        this.sampleSkew = sampleSkew;
    }

    public int getSampleDepth()
    {
        return sampleDepth;
    }

    public void setSampleDepth(int depth)
    {
        this.sampleDepth = depth;
    }

    public EdgeSet getDriveEdges()
    {
        return driveEdges;
    }

    public void setDriveEdges(EdgeSet driveEdges)
    {
        this.driveEdges = driveEdges;
    }

    public int getDriveSkew()
    {
        return driveSkew;
    }

    public void setDriveSkew(int driveSkew)
    {
        this.driveSkew = driveSkew;
    }

    public List<VeraSurrXParams> getSurrXParamsList()
    {
        return surrXParamsList;
    }

    public void addSurrXParams(VeraSurrXParams surrXParams)
    {
        surrXParamsList.add(surrXParams);
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

    public VeraInterfaceType getStructuredType()
    {
        return intf;
    }

    public void setInterface(VeraInterfaceType intf)
    {
        this.intf = intf;
    }

    public VeraVisibility getVisibility()
    {
        return VeraVisibility.PUBLIC;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "interface signal " + name;
    }
}
