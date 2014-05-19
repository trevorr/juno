/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.juno.runtime;

import java.io.ByteArrayOutputStream;

import com.newisys.dv.DV;
import com.newisys.random.PRNG;
import com.newisys.randsolver.InvalidConstraintException;
import com.newisys.randsolver.InvalidRandomVarException;
import com.newisys.randsolver.RandomHooks;
import com.newisys.randsolver.Solver;
import com.newisys.randsolver.UnsolvableConstraintException;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Base class for all translated Vera classes. Implements the built-in methods
 * callable on all Vera objects.
 * 
 * @author Trevor Robinson
 */
@Randomizable
public class JunoObject
    implements Cloneable, RandomHooks
{
    public static final int FAIL = 0;
    public static final int OK = 1;

    public static final int OFF = 0;
    public static final int ON = 1;
    public static final int REPORT = 2;

    public static final int RAND_MODE_FAIL = -1;
    public static final int CONSTRAINT_MODE_FAIL = -1;

    // Get the random stream from our thread. This is different from Vera which
    // creates a new random stream for each Object.
    private PRNG prng = DV.simulation.getRandom();

    /**
     * Creates a new JunoObject.
     */
    public JunoObject()
    {
        // do nothing
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this JunoObject
     * @see Object#clone
     */
    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error(e);
        }
    }

    /**
     * Randomize this JunoObject. If the constraints on this object are such
     * that there is no legal combination of random variables to satify those
     * constraints, {@link #FAIL} is returned. Otherwise {@link #OK} is returned.
     *
     * @return <code>OK</code> if the randomization was successful or
     *      <code>FAIL</code> if there was an error during randomization.
     */
    public final int randomize()
    {
        try
        {
            Solver.randomize(this, prng);
        }
        catch (UnsolvableConstraintException e)
        {
            // TODO: throw an exception if the object cannot be randomized
            return FAIL;
        }

        return OK;
    }

    /**
     * Allows code to be run just prior to this object being randomized. This
     * method calls <code>{@link #pre_randomize pre_randomize}</code>, which
     * should be overridden by subclasses needing this functionality.
     */
    public final void preRandomize()
    {
        pre_randomize();
    }

    /**
     * Allows code to be run just after this object is randomized. This
     * method calls <code>{@link #post_randomize post_randomize}</code>, which
     * should be overridden by subclasses needing this functionality.
     */
    public final void postRandomize()
    {
        post_randomize();
    }

    /**
     * Allows code to be run just prior to this object being randomized.
     * This method should be overridden by subclasses that wish to execute some
     * code just before this object is randomized. If this object contains
     * subobjects that are being randomized, the order in which each object's
     * <code>pre_randomize</code> method is called is undefined.
     *
     */
    public void pre_randomize()
    {
        // do nothing
    }

    /**
     * Allows code to be run just after this object has been randomized.
     * This method should be overridden by subclasses that wish to execute some
     * code just after this object is randomized. If this object contains
     * subobjects that are being randomized, the order in which each object's
     * <code>post_randomize</code> method is called is undefined.
     *
     */
    public void post_randomize()
    {
        // do nothing
    }

    /**
     * Sets the random number generator for this object.
     *
     * @param prng the random number generator to be used by this object
     */
    public final void setRandom(PRNG prng)
    {
        this.prng = prng;
    }

    /**
     * Toggles the randomization mode of all random variables in this object.
     *
     * If <code>action</code> is <code>{@link #ON ON}</code>, all random
     * variables associated with this object will be enabled. If
     * <code>action</code> is <code>{@link #OFF OFF}</code>, all random
     * variables associated with this object will be disabled. If
     * <code>action</code> is <code>{@link #REPORT REPORT}</code> a warning is
     * printed and <code>{@link #RAND_MODE_FAIL RAND_MODE_FAIL}</code> is returned.
     *
     * @param action one of <code>ON</code>, <code>OFF</code> or <code>REPORT</code>
     * @return <code>action</code> is returned, unless there is a failure or
     *      <code>action</code> is <code>REPORT</code>, in which case
     *      <code>RAND_MODE_FAIL</code> is returned
     */
    public final int rand_mode(int action)
    {
        assert (action == ON || action == OFF || action == REPORT);

        int result = RAND_MODE_FAIL;

        try
        {
            switch (action)
            {
            case ON:
                Solver.enableAllRand(this);
                result = ON;
                break;
            case OFF:
                Solver.disableAllRand(this);
                result = OFF;
                break;
            case REPORT:
                // TODO: throw an exception if rand_mode(REPORT) is called
                Juno
                    .warning("WARNING: runtime: rand_mode requires a specific name as a 2nd argument for REPORT\n");
                result = RAND_MODE_FAIL;
                break;
            default:
                throw new RuntimeException("Illegal action: " + action);
            }
        }
        catch (InvalidRandomVarException e)
        {
            result = RAND_MODE_FAIL;
        }
        return result;
    }

    /**
     * Toggles the randomization mode of the specified random variable in this
     * object.
     *
     * If <code>action</code> is <code>{@link #ON ON}</code>, randomization of
     * <code>var</code> in this instance will be enabled. If <code>action</code>
     * is <code>{@link #OFF OFF}</code>, randomization of <code>var</code> in
     * this instance will be disabled. If <code>action</code> is
     * <code>{@link #REPORT REPORT}</code> <code>ON</code> will be returned if
     * <code>var</code> is enabled, <code>OFF</code> otherwise.
     *
     * @param action one of <code>ON</code>, <code>OFF</code> or <code>REPORT</code>
     * @param var the variable to check
     * @return <code>action</code> is returned, unless there is a failure in which
     *      case <code>RAND_MODE_FAIL</code> is returned
     */
    public final int rand_mode(int action, String var)
    {
        return rand_mode(action, var, -1);
    }

    /**
     * Toggles the randomization mode of the specified random variable in this
     * object.
     *
     * If <code>action</code> is <code>{@link #ON ON}</code>, randomization of
     * <code>var</code> in this instance will be enabled. If <code>action</code>
     * is <code>{@link #OFF OFF}</code>, randomization of <code>var</code> in
     * this instance will be disabled. If <code>action</code> is
     * <code>{@link #REPORT REPORT}</code> <code>ON</code> will be
     * returned if <code>var</code> is enabled, <code>OFF</code> otherwise.
     *
     * The <code>index</code> parameter is used if <code>var</code> is an array.
     * This feature is currently unsupported and <code>index</code> must be
     * <code>-1</code>.
     *
     * @param action one of <code>ON</code>, <code>OFF</code> or <code>REPORT</code>
     * @param var the variable to check
     * @param index the array index to check. This currently must be -1.
     * @return <code>action</code> is returned, unless there is a failure in which
     *      case <code>RAND_MODE_FAIL</code> is returned
     */
    public final int rand_mode(int action, String var, int index)
    {
        assert (action == ON || action == OFF || action == REPORT);

        if (index != -1)
        {
            throw new UnsupportedOperationException(
                "Array indices are not supported");
        }

        int result = RAND_MODE_FAIL;

        try
        {
            switch (action)
            {
            case ON:
                Solver.enableRand(this, var);
                result = ON;
                break;
            case OFF:
                Solver.disableRand(this, var);
                result = OFF;
                break;
            case REPORT:
                boolean isEnabled = Solver.isRandEnabled(this, var);
                result = isEnabled ? ON : OFF;
                break;
            default:
                throw new RuntimeException("Illegal action: " + action);
            }
        }
        catch (InvalidRandomVarException e)
        {
            result = RAND_MODE_FAIL;
        }

        return result;
    }

    /**
     * Toggles the randomization mode of all constraints on this object.
     *
     * If <code>action</code> is <code>{@link #ON ON}</code>, all constraints
     * associated with this object will be enabled. If <code>action</code> is
     * <code>{@link #OFF OFF}</code>, all constraints associated with this object
     * will be disabled. If <code>action</code> is
     * <code>{@link #REPORT REPORT}</code> a warning is printed and
     * <code>{@link #CONSTRAINT_MODE_FAIL CONSTRAINT_MODE_FAIL}</code> is returned.
     *
     * @param action one of <code>ON</code>, <code>OFF</code> or <code>REPORT</code>
     * @return <code>action</code> is returned, unless there is a failure or
     *      <code>action</code> is <code>REPORT</code>, in which case
     *      <code>CONSTRAINT_MODE_FAIL</code> is returned
     */
    public final int constraint_mode(int action)
    {
        assert (action == ON || action == OFF || action == REPORT);

        int result = CONSTRAINT_MODE_FAIL;
        try
        {
            switch (action)
            {
            case ON:
                Solver.enableAllConstraints(this);
                result = ON;
                break;
            case OFF:
                Solver.disableAllConstraints(this);
                result = OFF;
                break;
            case REPORT:
                // TODO: throw an exception if constraint_mode(REPORT) is called.
                Juno
                    .warning("WARNING: runtime: constraint_mode requires a specific name as a 2nd argument for REPORT\n");
                result = CONSTRAINT_MODE_FAIL;
                break;
            default:
                throw new RuntimeException("Illegal action: " + action);
            }
        }
        catch (InvalidConstraintException e)
        {
            result = CONSTRAINT_MODE_FAIL;
        }
        return result;
    }

    /**
     * Toggles the randomization mode of the specified constraint on this object.
     *
     * If <code>action</code> is <code>{@link #ON ON}</code>, <code>cons</code>
     * will be enabled when randomizing this instance. If action is
     * <code>{@link #OFF OFF}</code>, all constraints associated with this
     * object will be disabled. If action is <code>{@link #REPORT REPORT}</code>
     * a warning is printed and
     * <code>{@link #CONSTRAINT_MODE_FAIL CONSTRAINT_MODE_FAIL}</code> is returned.
     *
     * @param action one of <code>ON</code>, <code>OFF</code> or <code>REPORT</code>
     * @param cons the constraint to check
     * @return <code>action</code> is returned, unless there is a failure, in
     *      which case <code>CONSTRAINT_MODE_FAIL</code> is returned
     */
    public final int constraint_mode(int action, String cons)
    {
        assert (action == ON || action == OFF || action == REPORT);

        int result = CONSTRAINT_MODE_FAIL;
        switch (action)
        {
        case ON:
            Solver.enableConstraint(this, cons);
            result = ON;
            break;
        case OFF:
            Solver.disableConstraint(this, cons);
            result = OFF;
            break;
        case REPORT:
            boolean isEnabled = Solver.isConstraintEnabled(this, cons);
            result = isEnabled ? ON : OFF;
            break;
        default:
            throw new RuntimeException("Illegal action: " + action);
        }
        return result;
    }

    /**
     * Prints the object instance hierarchy of this object to
     * {@link Juno#STDOUT}.
     *
     * This is equivalent to calling
     * <P>
     * <code>object_print(Vera.STDOUT,"")</code>
     * <P>
     * This method is currently unsupported.
     */
    public final void object_print()
    {
        // TODO: object_print
        throw new UnsupportedOperationException("object_print()");
    }

    /**
     * Prints the object instance hierarchy of this object to the specified
     * Vera file descriptor.
     *
     * This is equivalent to calling
     * <P>
     * <code>object_print(fd,"")</code>
     * <P>
     * This method is currently unsupported.
     *
     * @param fd the file descriptor to which output will be written
     */
    public final void object_print(int fd)
    {
        // TODO: object_print
        throw new UnsupportedOperationException("object_print(int)");
    }

    /**
     * Prints the object instance hierarchy of this object to the specified
     * Vera file descriptor with the specified attributes.
     *
     * <code>attrs</code> is a string of space-delimited key=value pairs. Valid
     * attributes are shown in the table below along with their default values.
     * <P>
     * <table border=1>
     * <tr>
     *      <td><b>key</b></td>
     *      <td><b>default value</b></td>
     *      <td><b>description</b></td>
     * </tr>
     * <tr>
     *      <td><code>depth</code></td>
     *      <td>0</td>
     *      <td>the number of levels of the hierarchy to print</td>
     * </tr>
     * <tr>
     *      <td><code>indent</code></td>
     *      <td>4</td>
     *      <td>the number of spaces to indent members and array elements</td>
     * </tr>
     * <tr>
     *      <td><code>severity</code></td>
     *      <td><code>low</code></td>
     *      <td>one of <code>low</code> or <code>high</code>. <code>low</code>
     *          ignores errors encountered during printing</td>
     * </tr>
     * <tr>
     *      <td><code>port</code></td>
     *      <td><code>yes</code></td>
     *      <td>one of <code>yes</code> or <code>no</code>. <code>yes</code> will
     *          print port signals, <code>no</code> will omit them.</td>
     * </tr>
     * <tr>
     *      <td><code>format</code></td>
     *      <td>see description</td>
     *      <td>one of <code>bin</code>, <code>dec</code>, or <code>hex</code>.
     *          <code>bin</code> will have an underscore frequency of 4. the
     *          default is to print integers in decimal, bit vectors and signals
     *          without X/Z values in hexadecimal, and bit vectors and signals
     *          with X/Z values in binary</td>
     * </tr>
     * <tr>
     *      <td><code>array_depth</code></td>
     *      <td>20</td>
     *      <td>a decimal value specifying the maximum number of array elements
     *          to print</td>
     * </tr>
     * </table>
     * <P>
     * This method is currently unsupported.
     *
     * @param fd the file descriptor to which output will be written
     * @param attrs an attribute string as defined above
     */
    public final void object_print(int fd, String attrs)
    {
        // TODO: object_print
        throw new UnsupportedOperationException("object_print(int, String)");
    }

    /**
     * Performs a deep compare of this object with the specified object. The
     * comparison includes both super objects and contained objects. Comparisons
     * are made based on object contents, not based on object references.
     * <P>
     * This method is currently unsupported.
     *
     * @param other the JunoObject to compare against
     * @return 1 if the two objects are equal, 0 otherwise
     */
    public int object_compare(JunoObject other)
    {
        // TODO: object_compare
        throw new UnsupportedOperationException("object_compare(JunoObject)");
    }

    /**
     * Performs a deep copy of this JunoObject and returns the new JunoObject.
     * The new JunoObject contains deep copies of this JunoObject's super objects
     * and contained objects.
     * <P>
     * This method is currently unsupported.
     *
     * @return a new deep copy of this JunoObject
     */
    public final JunoObject object_copy()
    {
        // TODO: object_copy
        throw new UnsupportedOperationException("object_copy()");
    }

    public static final class PackingPosition
    {
        public long index;
        public int left;
        public int right;

        public PackingPosition()
        {
            this.index = 0;
            this.left = 0;
            this.right = 0;
        }

        public PackingPosition(long index, int left, int right)
        {
            this.index = index;
            this.left = left;
            this.right = right;
        }
    }

    public final int pack(BitBitAssocArray array, PackingPosition pos)
    {
        pre_pack();
        int result = doPack(array, pos);
        post_pack();
        return result;
    }

    protected int doPack(BitBitAssocArray array, PackingPosition pos)
    {
        // overridden by classes with packed fields
        return 0;
    }

    protected static int packBits(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        BitVector value)
    {
        final int wordSize = array.getValueLength();

        final int valueSize = value.length();
        int valueWritten = 0;

        long index = pos.index;
        BitVector indexBV = new BitVector(64, index);
        int left = pos.left;
        int right = pos.right;

        while (true)
        {
            int valueRemaining = valueSize - valueWritten;
            if (valueRemaining == 0) break;

            int wordRemaining = wordSize - left - right;

            int writeSize = Math.min(wordRemaining, valueRemaining);
            if (writeSize > 0)
            {
                BitVector word = array.get(indexBV);

                BitVector writeValue;
                if (bigEndian ^ bitReverse)
                {
                    writeValue = value.getBits(valueSize - valueWritten - 1,
                        valueSize - valueWritten - writeSize);
                }
                else
                {
                    writeValue = value.getBits(valueWritten + writeSize - 1,
                        valueWritten);
                }
                valueWritten += writeSize;

                if (bitReverse)
                {
                    writeValue = writeValue.reverse();
                }

                if (bigEndian)
                {
                    word = word.setBits(wordSize - left - 1, wordSize - left
                        - writeSize, writeValue);
                    left += writeSize;
                }
                else
                {
                    word = word.setBits(right + writeSize - 1, right,
                        writeValue);
                    right += writeSize;
                }
                wordRemaining -= writeSize;

                array.put(indexBV, word);
            }

            if (wordRemaining == 0)
            {
                ++index;
                indexBV = new BitVector(64, index);
                left = 0;
                right = 0;
            }
        }

        pos.index = index;
        pos.left = left;
        pos.right = right;

        return valueSize;
    }

    protected static int packInteger(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        Integer value)
    {
        return packBits(array, pos, bigEndian, bitReverse, IntegerOp
            .toBitVector(value));
    }

    protected static int packEnum(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        JunoEnum< ? > value)
    {
        return packBits(array, pos, bigEndian, bitReverse, IntegerOp
            .toBitVector(value.toInteger(), value.getPackedSize()));
    }

    protected static int packString(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        String s)
    {
        int size = 0;
        byte[] bytes = s.getBytes();
        for (int i = 0; i < bytes.length; ++i)
        {
            BitVector bv = new BitVector(8, bytes[i]);
            size += packBits(array, pos, bigEndian, bitReverse, bv);
        }
        BitVector bv = new BitVector(8, 0);
        size += packBits(array, pos, bigEndian, bitReverse, bv);
        return size;
    }

    public void pre_pack()
    {
        // do nothing
    }

    public void post_pack()
    {
        // do nothing
    }

    public final int unpack(BitBitAssocArray array, PackingPosition pos)
    {
        pre_unpack();
        int result = doUnpack(array, pos);
        post_unpack();
        return result;
    }

    protected int doUnpack(BitBitAssocArray array, PackingPosition pos)
    {
        // overridden by classes with packed fields
        return 0;
    }

    protected static BitVector unpackBits(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        int valueSize)
    {
        final int wordSize = array.getValueLength();

        final BitVectorBuffer value = new BitVectorBuffer(valueSize);
        int valueRead = 0;

        long index = pos.index;
        BitVector indexBV = new BitVector(64, index);
        int left = pos.left;
        int right = pos.right;

        while (true)
        {
            int valueRemaining = valueSize - valueRead;
            if (valueRemaining == 0) break;

            int wordRemaining = wordSize - left - right;

            int readSize = Math.min(wordRemaining, valueRemaining);
            if (readSize > 0)
            {
                BitVector word = array.get(indexBV);

                BitVector readValue;
                if (bigEndian)
                {
                    readValue = word.getBits(wordSize - left - 1, wordSize
                        - left - readSize);
                    left += readSize;
                }
                else
                {
                    readValue = word.getBits(right + readSize - 1, right);
                    right += readSize;
                }
                wordRemaining -= readSize;

                if (bitReverse)
                {
                    readValue = readValue.reverse();
                }

                if (bigEndian ^ bitReverse)
                {
                    value.setBits(valueSize - valueRead - 1, valueSize
                        - valueRead - readSize, readValue);
                }
                else
                {
                    value.setBits(valueRead + readSize - 1, valueRead,
                        readValue);
                }
                valueRead += readSize;
            }

            if (wordRemaining == 0)
            {
                ++index;
                indexBV = new BitVector(64, index);
                left = 0;
                right = 0;
            }
        }

        pos.index = index;
        pos.left = left;
        pos.right = right;

        return value.toBitVector();
    }

    protected static Integer unpackInteger(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse)
    {
        return IntegerOp.toInteger(unpackBits(array, pos, bigEndian,
            bitReverse, 32));
    }

    protected static <E extends Enum<E> & JunoEnum<E>> E unpackEnum(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse,
        E enumInstance)
    {
        Integer i = IntegerOp.toInteger(unpackBits(array, pos, bigEndian,
            bitReverse, enumInstance.getPackedSize()), enumInstance.isSigned());
        return enumInstance.getForValue(i, false);
    }

    protected static String unpackString(
        BitBitAssocArray array,
        PackingPosition pos,
        boolean bigEndian,
        boolean bitReverse)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(64);
        while (true)
        {
            BitVector bv = unpackBits(array, pos, bigEndian, bitReverse, 8);
            if (bv.containsXZ())
            {
                throw new IllegalArgumentException(
                    "Found X/Z bits while unpacking string");
            }
            if (bv.isZero()) break;
            stream.write(bv.intValue());
        }
        return stream.toString();
    }

    public void pre_unpack()
    {
        // do nothing
    }

    public void post_unpack()
    {
        // do nothing
    }
    
    @Override
    public void finalize()
    {
        // this method overrides java.lang.Object.finalize() in order
        // to remove the checked exception
    }
}
