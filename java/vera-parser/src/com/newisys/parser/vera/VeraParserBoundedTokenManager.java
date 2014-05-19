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

package com.newisys.parser.vera;

import com.newisys.parser.util.Token;

/**
 * Bounded buffer implementation of VeraParserTokenManager. Designed to be used
 * between separate preprocessor and parser threads.
 * 
 * @author Trevor Robinson
 */
public class VeraParserBoundedTokenManager
    implements VeraParserTokenManager
{
    private static final int BUF_SIZE = 1024;

    private final VeraToken[] buffer = new VeraToken[BUF_SIZE];
    private int count;
    private int readIndex;
    private int writeIndex;

    public void pushToken(VeraToken t)
    {
        while (true)
        {
            synchronized (buffer)
            {
                // write to next position in buffer if not full
                if (count < BUF_SIZE)
                {
                    buffer[writeIndex++] = t;
                    if (writeIndex >= BUF_SIZE)
                    {
                        writeIndex = 0;
                    }
                    // if buffer was empty, notify potentially waiting reader
                    if (count == 0)
                    {
                        buffer.notify();
                    }
                    ++count;
                    break;
                }

                // buffer is full; wait for notification from reader
                try
                {
                    buffer.wait();
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Token getNextToken()
    {
        VeraToken t;
        while (true)
        {
            synchronized (buffer)
            {
                // read from next position in buffer if not empty
                if (count > 0)
                {
                    t = buffer[readIndex++];
                    if (readIndex >= BUF_SIZE)
                    {
                        readIndex = 0;
                    }
                    // if buffer was full, notify potentially waiting writer
                    if (count == BUF_SIZE)
                    {
                        buffer.notify();
                    }
                    --count;
                    break;
                }

                // buffer is empty; wait for notification from writer
                try
                {
                    buffer.wait();
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return t;
    }
}
