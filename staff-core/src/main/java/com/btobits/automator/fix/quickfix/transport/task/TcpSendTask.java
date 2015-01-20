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

package com.btobits.automator.fix.quickfix.transport.task;

import com.btobits.automator.fix.utils.FileMessageFactory;
import com.btobits.automator.fix.quickfix.transport.ITcpProccesor;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class TcpSendTask extends Task {
    private String refid;
    private String file;
    private String delimeter = "\n";
    private int repeatDelay = 0;
    private Integer timeout = 10000;

    public void setTimeout(String inTimeout) {
        timeout = Integer.parseInt(inTimeout) * 1000;
    }

    public TcpSendTask() {
    }

    public void setRefid(String refid) {
        this.refid = refid;
    }

    public void setFile(String inFile) {
        this.file = inFile;
    }

    public void setDelimeter(String inDelimeter) {
        this.delimeter = inDelimeter;
    }

    public void setRepeatDelay(String inRepeatDelay) {
        this.repeatDelay = (int) (Float.parseFloat(inRepeatDelay) * 1000);
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (refid == null) {
                throw new Exception("Reference to Tcp client is not specified");
            }

            if (file == null) {
                throw new Exception("Parameter file must exists. ");
            }

            final Object obj = getProject().getReference(refid);
            if (obj == null) {
                throw new Exception("Failed to get transport client by reference");
            }

            if (obj instanceof ITcpProccesor) {
                sendData((ITcpProccesor) obj);
            } else {
                throw new Exception("Tcp client type is unknown");
            }
        } catch (Exception e) {
            throw new BuildException("Error sending data :" + e);
        }
    }

    /*
      * Send data
      * */
    private void sendData(ITcpProccesor client) throws Exception {
        final FileMessageFactory messageFactory = new FileMessageFactory(file, null);
        messageFactory.setDelimeter(delimeter);
        messageFactory.loadInternal(client, repeatDelay, refid, timeout);
    }
}
