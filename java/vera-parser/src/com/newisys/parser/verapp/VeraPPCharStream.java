/*
 * JavaCC (TM) parser definition for the OpenVera (TM) language
 * Copyright (C) 2003 Trevor A. Robinson
 * JavaCC is a trademark or registered trademark of Sun Microsystems, Inc. in
 * the U.S. or other countries.
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

package com.newisys.parser.verapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * CharStream implementation that can have arbitrary data pushed into it.
 * Used by VeraPPParserTokenManager to handle macro expansions.
 * 
 * @author Trevor Robinson
 */
public final class VeraPPCharStream
    implements CharStream
{
    public abstract class Chunk
        implements Cloneable
    {
        Chunk prev;
        Chunk next;
        protected char[] buf;
        protected int first;
        protected int last;
        protected int pos;
        protected Object marker;

        public Chunk()
        {
            first = 0;
            last = pos = -1;
        }

        public int available()
            throws IOException
        {
            return last - pos;
        }

        public char read()
            throws IOException
        {
            assert (pos < last);
            ++pos;
            assert (pos >= first);
            ++currentPos;
            return buf[pos];
        }

        public int getImageLength()
        {
            return pos - first + 1;
        }

        public void backup(int count)
        {
            assert (count <= getImageLength());
            pos -= count;
            currentPos -= count;
        }

        public void appendImage(StringBuffer buf, int count)
        {
            assert (count <= getImageLength());
            buf.append(this.buf, pos - count + 1, count);
        }

        public void copyImage(char[] buf, int offset, int count)
        {
            assert (count <= getImageLength());
            System.arraycopy(this.buf, pos - count + 1, buf, offset, count);
        }

        public void split()
        {
            // do nothing if before first or at last character
            if (pos >= first && pos < last)
            {
                try
                {
                    Chunk newChunk = (Chunk) clone();

                    // insert new chunk after this chunk
                    if (next != null)
                    {
                        next.prev = newChunk;
                        newChunk.next = next;
                    }
                    newChunk.prev = this;
                    next = newChunk;

                    // set buffer ranges
                    last = pos;
                    newChunk.first = pos + 1;
                }
                catch (CloneNotSupportedException e)
                {
                }
            }
        }

        public abstract int getLine();

        public abstract int getColumn();

        public Object getMarker()
        {
            return marker;
        }
    }

    private final class ReaderChunk
        extends Chunk
    {
        private final int[] lines;
        private final int[] columns;
        private int lcPos;

        public ReaderChunk()
        {
            buf = new char[bufferSize];
            lines = new int[bufferSize];
            columns = new int[bufferSize];
            lcPos = -1;
        }

        private void checkFill()
            throws IOException
        {
            if (last < 0 && !eof)
            {
                int count = reader.read(buf);
                if (count > 0)
                {
                    last = count - 1;
                }
                else
                {
                    eof = true;
                }
            }
        }

        public int available()
            throws IOException
        {
            // fill on first call
            checkFill();

            return super.available();
        }

        public char read()
            throws IOException
        {
            // fill on first call
            checkFill();

            if (eof)
            {
                throw new IOException("End of file");
            }

            char c = super.read();

            // set line/column if not already set
            if (lcPos < pos)
            {
                lines[pos] = currentLine;
                columns[pos] = currentColumn;
                lcPos = pos;

                boolean newLine = false;
                switch (c)
                {
                case '\r':
                    newLine = true;
                    break;
                case '\n':
                    if (prevChar != '\r')
                    {
                        newLine = true;
                    }
                    break;
                }
                if (newLine)
                {
                    ++currentLine;
                    currentColumn = 1;
                }
                else
                {
                    ++currentColumn;
                }
                prevChar = c;
            }

            return c;
        }

        public int getLine()
        {
            return pos >= 0 ? lines[pos] : currentLine;
        }

        public int getColumn()
        {
            return pos >= 0 ? columns[pos] : currentColumn;
        }
    }

    private final class InsertChunk
        extends Chunk
    {
        private final int line;
        private final int column;

        public InsertChunk(
            char[] buf,
            int offset,
            int length,
            int line,
            int column,
            Object marker)
        {
            this.buf = buf;
            this.first = offset;
            this.last = offset + length - 1;
            this.line = line;
            this.column = column;
            this.marker = marker;
        }

        public int getLine()
        {
            return line;
        }

        public int getColumn()
        {
            return column;
        }
    }

    private static final int DEF_BUF_SIZE = 4096;

    // default access for efficient access by inner class
    Reader reader;
    int bufferSize;

    int currentPos;
    int currentColumn;
    int currentLine;

    private Chunk startChunk;
    private int startChunkSkip;
    private int startPos;
    private int startColumn;
    private int startLine;

    private Chunk curChunk;
    char prevChar;
    boolean eof;

    public VeraPPCharStream(
        Reader dstream,
        int startline,
        int startcolumn,
        int buffersize)
    {
        this.reader = dstream;
        this.bufferSize = buffersize;

        this.currentPos = -1;
        this.currentColumn = startcolumn;
        this.currentLine = startline;

        this.startChunkSkip = -1;
        this.startPos = -1;
        this.startColumn = -1;
        this.startLine = -1;

        this.curChunk = new ReaderChunk();
    }

    public VeraPPCharStream(Reader dstream, int startline, int startcolumn)
    {
        this(dstream, startline, startcolumn, DEF_BUF_SIZE);
    }

    public VeraPPCharStream(Reader dstream)
    {
        this(dstream, 1, 1, DEF_BUF_SIZE);
    }

    public VeraPPCharStream(
        InputStream dstream,
        int startline,
        int startcolumn,
        int buffersize)
    {
        this(new InputStreamReader(dstream), startline, startcolumn, buffersize);
    }

    public VeraPPCharStream(InputStream dstream, int startline, int startcolumn)
    {
        this(dstream, startline, startcolumn, DEF_BUF_SIZE);
    }

    public VeraPPCharStream(InputStream dstream)
    {
        this(dstream, 1, 1, DEF_BUF_SIZE);
    }

    public char readChar()
        throws IOException
    {
        while (true)
        {
            // try to read from current chunk
            if (curChunk.available() > 0)
            {
                return curChunk.read();
            }

            // move to next chunk if present
            if (curChunk.next != null)
            {
                curChunk = curChunk.next;
                continue;
            }

            // check for EOF
            if (eof)
            {
                throw new IOException("End of file");
            }

            // create new chunk from reader
            Chunk newChunk = new ReaderChunk();

            // add chunk to doubly-linked chain
            curChunk.next = newChunk;
            newChunk.prev = curChunk;

            // move to new chunk
            curChunk = newChunk;
        }
    }

    public int getColumn()
    {
        return curChunk.getColumn();
    }

    public int getLine()
    {
        return curChunk.getLine();
    }

    public int getEndColumn()
    {
        return curChunk.getColumn();
    }

    public int getEndLine()
    {
        return curChunk.getLine();
    }

    public int getBeginColumn()
    {
        return startColumn >= 0 ? startColumn : currentColumn;
    }

    public int getBeginLine()
    {
        return startLine >= 0 ? startLine : currentLine;
    }

    public void backup(int amount)
    {
        while (amount > 0)
        {
            int imageLength = curChunk.getImageLength();
            int delta;
            boolean goToPrev;
            if (imageLength <= amount)
            {
                delta = imageLength;
                goToPrev = true;
            }
            else
            {
                delta = amount;
                goToPrev = false;
            }
            curChunk.backup(delta);
            amount -= delta;
            if (goToPrev)
            {
                if (curChunk.prev == null)
                {
                    throw new IllegalArgumentException(
                        "Cannot backup past first chunk");
                }
                curChunk = curChunk.prev;
            }
        }
    }

    public char BeginToken()
        throws IOException
    {
        // clear token start information
        startChunk = null;
        startChunkSkip = -1;
        startPos = -1;
        startLine = -1;
        startColumn = -1;

        // drop any previous chunks
        curChunk.prev = null;

        // read the first character
        char c = readChar();

        // set token start information
        startChunk = curChunk;
        startChunkSkip = curChunk.getImageLength() - 1;
        startPos = currentPos;
        startLine = curChunk.getLine();
        startColumn = curChunk.getColumn();

        return c;
    }

    public String GetImage()
    {
        // check that a token has been started
        if (startPos >= 0)
        {
            // create string buffer of proper length
            int length = currentPos - startPos + 1;
            StringBuffer buf = new StringBuffer(length);

            // copy chunks into image buffer
            Chunk imageChunk = startChunk;
            int appendCount = startChunk.getImageLength() - startChunkSkip;
            while (length > 0)
            {
                imageChunk.appendImage(buf, appendCount);

                length -= appendCount;
                if (length == 0)
                {
                    break;
                }

                imageChunk = imageChunk.next;
                assert (imageChunk != null);

                appendCount = imageChunk.getImageLength();
            }

            return buf.toString();
        }
        else
        {
            // return empty string if token has not been started
            return "";
        }
    }

    public char[] GetSuffix(int len)
    {
        // create char buffer of requested length
        char[] buf = new char[len];

        // check that a token has been started
        if (startPos >= 0)
        {
            // check that len <= image length
            int imageLength = currentPos - startPos + 1;
            assert (len <= imageLength);

            // copy chunks into image buffer
            Chunk imageChunk = startChunk;
            int appendCount = startChunk.getImageLength() - startChunkSkip;
            int imageSkip = imageLength - len;
            int offset = 0;
            while (len > 0)
            {
                if (imageSkip > 0)
                {
                    int skip = Math.min(imageSkip, appendCount);
                    imageSkip -= skip;
                    appendCount -= skip;
                }
                if (appendCount > 0)
                {
                    imageChunk.copyImage(buf, offset, appendCount);
                    offset += appendCount;
                }

                len -= appendCount;
                if (len == 0)
                {
                    break;
                }

                imageChunk = imageChunk.next;
                assert (imageChunk != null);

                appendCount = imageChunk.getImageLength();
            }
        }

        return buf;
    }

    public void Done()
    {
        try
        {
            reader.close();
        }
        catch (IOException ignored)
        {
        }

        curChunk = null;
    }

    public Chunk insert(char[] chars, int offset, int length, Object marker)
    {
        return insertAfter(null, chars, offset, length, marker);
    }

    public Chunk insert(String s, Object marker)
    {
        return insert(s.toCharArray(), 0, s.length(), marker);
    }

    public Chunk insertAfter(
        Chunk chunk,
        char[] chars,
        int offset,
        int length,
        Object marker)
    {
        if (chunk == null)
        {
            // split current chunk at current position (if necessary)
            curChunk.split();
            chunk = curChunk;
        }

        // create chunk for inserted characters
        InsertChunk newChunk = new InsertChunk(chars, offset, length, chunk
            .getLine(), chunk.getColumn(), marker);

        // insert new chunk after current chunk
        if (chunk.next != null)
        {
            chunk.next.prev = newChunk;
            newChunk.next = chunk.next;
        }
        newChunk.prev = chunk;
        chunk.next = newChunk;

        return newChunk;
    }

    public Chunk insertAfter(Chunk chunk, String s, Object marker)
    {
        return insertAfter(chunk, s.toCharArray(), 0, s.length(), marker);
    }

    public Object getBeginMarker()
    {
        // return null if token has not been started
        return startPos >= 0 ? startChunk.getMarker() : null;
    }

    public Object getEndMarker()
    {
        // return null if token has not been started
        return startPos >= 0 ? curChunk.getMarker() : null;
    }
}
