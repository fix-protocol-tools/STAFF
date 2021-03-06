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

import com.btobits.automator.fix.quickfix.transport.ITcpProccesor;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class TcpStartTask extends Task {
    private String refid;
    private int connectAttempt;

    public TcpStartTask() {
    }

    public void setRefid(String refid) {
        this.refid = refid;
    }

    public void setConnectAttempt(String inConnectAttempt) {
        connectAttempt = Integer.parseInt(inConnectAttempt);
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (refid == null) {
                throw new Exception("Reference to transport processor is not specified");
            }

            final Object obj = getProject().getReference(refid);
            if (obj == null) {
                throw new Exception("Failed to get transport processor by reference");
            }

            if (obj instanceof ITcpProccesor) {
                final ITcpProccesor processor = (ITcpProccesor) obj;
                processor.open();
            } else {
                throw new Exception("Tcp client type is unknown");
            }
        } catch (Exception e) {
            throw new BuildException("Error starting Tcp processor :" + e);
        }
    }
}
