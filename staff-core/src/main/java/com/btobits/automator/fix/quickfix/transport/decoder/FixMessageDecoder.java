/*
  * Copyright © 2011-2014 EPAM Systems/B2BITS® (http://www.b2bits.com).
 *
 * This file is part of STAFF.
 *
 * STAFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * STAFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with STAFF. If not, see <http://www.gnu.org/licenses/>.
 */

package com.btobits.automator.fix.quickfix.transport.decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quickfixj.CharsetSupport;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ByteBuffer;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;

import quickfix.mina.CriticalProtocolCodecException;

/**
 * @author Volodymyr_Biloshkurskyi
 */
public class FixMessageDecoder implements MessageDecoder {
    private static final String FIELD_DELIMITER = "\001";

    private Logger log = LoggerFactory.getLogger(getClass());

    private final byte[] HEADER_PATTERN;
    private final byte[] CHECKSUM_PATTERN;
    private final byte[] LOGON_PATTERN;

    // Parsing states
    private static final int SEEKING_HEADER = 1;
    private static final int SEEKING_END = 2;

    private int state;    
    private int position;
    private final String charsetEncoding;

    private void resetState() {
        state = SEEKING_HEADER;        
        position = 0;
    }

    public FixMessageDecoder() throws UnsupportedEncodingException {
        this(CharsetSupport.getCharset(), FIELD_DELIMITER);
    }

    public FixMessageDecoder(String charset) throws UnsupportedEncodingException {
        this(charset, FIELD_DELIMITER);
    }

    public FixMessageDecoder(String charset, String delimiter) throws UnsupportedEncodingException {
        charsetEncoding = CharsetSupport.validate(charset);
        HEADER_PATTERN = getBytes("8=FIX.?.?" + delimiter + "9=");
        CHECKSUM_PATTERN = getBytes("10=???" + delimiter);
        LOGON_PATTERN = getBytes("\00135=A" + delimiter);
        resetState();
    }

    public MessageDecoderResult decodable(IoSession session, ByteBuffer in) {
        int headerOffset = indexOf(in, in.position(), HEADER_PATTERN);
        return headerOffset != -1 ? MessageDecoderResult.OK : MessageDecoderResult.NEED_DATA;
    }

    public MessageDecoderResult decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out)
            throws ProtocolCodecException {
        int messageCount = 0;
        while (parseMessage(in, out)) {
            messageCount++;
        }
        if (messageCount > 0) {
            // Mina will compact the buffer because we can't detect a header
            if (in.remaining() < HEADER_PATTERN.length) {
                position = 0;
            }
            return MessageDecoderResult.OK;
        } else {
            // Mina will compact the buffer
            position -= in.position();
            return MessageDecoderResult.NEED_DATA;
        }
    }

    /**
     * This method cannot move the buffer position until a message is found or an error
     * has occurred. Otherwise, MINA will compact the buffer and we lose data.
     */
    private boolean parseMessage(ByteBuffer in, ProtocolDecoderOutput out)
            throws ProtocolCodecException {
        try {
            boolean messageFound = false;
            while (in.hasRemaining() && !messageFound) {
                if (state == SEEKING_HEADER) {

                    int headerOffset = indexOf(in, position, HEADER_PATTERN);
                    if (headerOffset == -1) {
                        break;
                    }

                    in.position(headerOffset);

                    if (log.isDebugEnabled()) {
                        log.debug("detected header: " + getBufferDebugInfo(in));
                    }

                    position = headerOffset + HEADER_PATTERN.length;
                    state = SEEKING_END;
                }

                if (state == SEEKING_END) {
                    boolean finded = startsWith(in, position, CHECKSUM_PATTERN);
                    while(!finded && in.limit() > position) {
                        position += 1;
                        finded = startsWith(in, position, CHECKSUM_PATTERN);

                    }

                    if(!finded) {                        
                        break;
                    }

                    position += CHECKSUM_PATTERN.length;                    
                    state = SEEKING_HEADER;
                    
                    String messageString = getMessageString(in);
                    out.write(messageString.replace("\r", ""));
                    messageFound = true;
                    resetState();
                }
            }
            return messageFound;
        } catch (Throwable t) {
            state = SEEKING_HEADER;
            position = 0;
            if (t instanceof ProtocolCodecException) {
                throw (ProtocolCodecException) t;
            } else {
                throw new ProtocolCodecException(t);
            }
        }
    }

    private int remaining(ByteBuffer in) {
        return in.limit() - position;
    }

    private String getBufferDebugInfo(ByteBuffer in) {
        return "pos=" + in.position() + ",lim=" + in.limit() + ",rem=" + in.remaining()
                + ",offset=" + position + ",state=" + state;
    }

    private byte get(ByteBuffer in) {
        return in.get(position++);
    }

    private boolean hasRemaining(ByteBuffer in) {
        return position < in.limit();
    }

    private String getMessageString(ByteBuffer buffer) throws UnsupportedEncodingException {
        byte[] data = new byte[position - buffer.position()];
        buffer.get(data);
        return new String(data, charsetEncoding);
    }

    private void handleError(ByteBuffer buffer, int recoveryPosition, String text,
                             boolean disconnect) throws ProtocolCodecException {
        buffer.position(recoveryPosition);
        position = recoveryPosition;
        state = SEEKING_HEADER;

        text = appendDebugInformation(buffer, text);

        if (disconnect) {
            throw new CriticalProtocolCodecException(text);
        } else {
            log.error(text);
        }
    }

    private String appendDebugInformation(ByteBuffer buffer, String text) {
        int mark = buffer.position();
        try {
            StringBuilder sb = new StringBuilder(text);
            sb.append("\nBuffer debug info: ").append(getBufferDebugInfo(buffer));
            buffer.position(0);
            sb.append("\nBuffer contents: ");
            try {
                final byte[] array = new byte[buffer.limit()];
                for(int i = 0; i < array.length; ++i)
                    array[i] = buffer.get();
                sb.append(new String(array, "ISO-8859-1"));
            } catch (Exception e) {
                sb.append(buffer.getHexDump());
            }
            text = sb.toString();
        } finally {
            buffer.position(mark);
        }
        return text;
    }

    private boolean isLogon(ByteBuffer buffer) {
        return indexOf(buffer, buffer.position(), LOGON_PATTERN) != -1;
    }

    private static int indexOf(ByteBuffer buffer, int position, byte[] data) {
        for (int offset = position, limit = buffer.limit() - data.length + 1; offset < limit; offset++) {
            if (buffer.get(offset) == data[0] && startsWith(buffer, offset, data)) {
                return offset;
            }
        }
        return -1;
    }

    private static boolean startsWith(ByteBuffer buffer, int bufferOffset, byte[] data) {
        try {
            if (bufferOffset + data.length > buffer.limit()) {
                return false;
            }

            //StringBuffer sb = new StringBuffer();
            for (int dataOffset = 0; dataOffset < data.length && bufferOffset < buffer.limit(); dataOffset++, bufferOffset++) {

                byte b = buffer.get(bufferOffset);
                if(b == '\r' && dataOffset > 0 && buffer.get(bufferOffset - 1) == '\u0001') {
                    return true;  // EOF io
                }

                byte bPatern = data[dataOffset];
                if (b != bPatern && bPatern != '?') {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1) throws Exception {
        // empty
    }

    /**
     * Used to process streamed messages from a file
     */
    public interface MessageListener {
        void onMessage(String message);
    }

    /**
     * Utility method to extract messages from files. This method loads all
     * extracted messages into memory so if the expected number of extracted
     * messages is large, do not use this method or your application may run
     * out of memory. Use the streaming version of the method instead.
     *
     * @param file
     * @return a list of extracted messages
     * @throws java.io.IOException
     * @throws ProtocolCodecException
     */
    public List<String> extractMessages(File file) throws IOException, ProtocolCodecException {
        final List<String> messages = new ArrayList<String>();
        extractMessages(file, new MessageListener() {

            public void onMessage(String message) {
                messages.add(message);
            }

        });
        return messages;
    }

    /**
     * Utility to extract messages from a file. This method will return each
     * message found to a provided listener. The message file will also be
     * memory mapped rather than fully loaded into physical memory. Therefore,
     * a large message file can be processed without using excessive memory.
     *
     * @param file
     * @param listener
     * @throws IOException
     * @throws ProtocolCodecException
     */
    public void extractMessages(File file, final MessageListener listener) throws IOException,
            ProtocolCodecException {
        // Set up a read-only memory-mapped file
        FileChannel readOnlyChannel = new RandomAccessFile(file, "r").getChannel();
        MappedByteBuffer memoryMappedBuffer = readOnlyChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                (int) readOnlyChannel.size());

        decode(null, ByteBuffer.wrap(memoryMappedBuffer), new ProtocolDecoderOutput() {

            public void write(Object message) {
                listener.onMessage((String) message);
            }

            public void flush() {
                // ignored
            }

        });
    }

    private static byte[] getBytes(String s) {
        try {
            return s.getBytes(CharsetSupport.getDefaultCharset());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
