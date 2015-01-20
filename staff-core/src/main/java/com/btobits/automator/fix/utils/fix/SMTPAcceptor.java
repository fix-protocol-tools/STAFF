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

package com.btobits.automator.fix.utils.fix;

import com.btobits.automator.mail.smtp.SMTPProcessor;
import com.ericdaugherty.mail.server.configuration.ConfigurationManager;
import com.ericdaugherty.mail.server.services.general.ServiceListener;
import com.ericdaugherty.mail.server.services.smtp.SMTPMessage;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Volodymyr_Biloshkurs
 */
public class SMTPAcceptor extends DataType implements IControl {
    //private int port = 25;
    private ServiceListener serviceListener;
    private Thread serviceThread;
    public static final BlockingQueue<SMTPMessage> messages = new ArrayBlockingQueue<SMTPMessage>(20);
    private String configurationDirectory = "settings";

    public String getConfigurationDirectory() {
        return configurationDirectory;
    }

    public void setConfigurationDirectory(String configurationDirectoryin) {
        configurationDirectory = configurationDirectoryin;
        ConfigurationManager.initialize(configurationDirectory);
    }

    public SMTPAcceptor() {
        super();
    }

    public SMTPAcceptor(Project project) {
        super();
        setProject(project);
    }

    public void start() throws Exception {
        int port = ConfigurationManager.getInstance().getSmtpPort();
        serviceListener = new ServiceListener(port, SMTPProcessor.class, 1);
        serviceThread = new Thread(serviceListener, "SMTP");
        serviceThread.start();
    }

    public void stop() throws Exception {
        try {
            serviceThread.interrupt();
        } catch (Exception ex) {
        }

        try {
            serviceListener.shutdown();
        } catch (Exception ex) {
        }

        serviceThread = null;
        serviceListener = null;
    }

    @Override
    public void stop(boolean needSendLogout) throws Exception {
        throw new UnsupportedOperationException("Stop with 'needSendLogout' parameter don't supported by " + this.getClass().getSimpleName());
    }
}
