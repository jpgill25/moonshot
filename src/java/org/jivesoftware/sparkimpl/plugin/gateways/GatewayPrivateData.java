/**
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2005 Jive Software. All rights reserved.
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package org.jivesoftware.sparkimpl.plugin.gateways;

import org.jivesoftware.smackx.packet.PrivateData;
import org.jivesoftware.smackx.provider.PrivateDataProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handle Gateway preferences through private data to persist through seperate locations of the client.
 *
 * @author Derek DeMoro
 */
public class GatewayPrivateData implements PrivateData {

    private final Map<String, String> loginSettingsMap = new HashMap<String, String>();

    public static final String ELEMENT = "gateway-settings";
    public static final String NAMESPACE = "http://www.jivesoftware.org/spark";

    public void addService(String serviceName, boolean autoLogin) {
        loginSettingsMap.put(serviceName, Boolean.toString(autoLogin));
    }

    public boolean autoLogin(String serviceName) {
        String str = loginSettingsMap.get(serviceName);
        if(str == null){
            return true;
        }

        return Boolean.parseBoolean(str);
    }

    public String getElementName() {
        return ELEMENT;
    }

    public String getNamespace() {
        return NAMESPACE;
    }


    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf.append("<gateways>");
        Iterator iter = loginSettingsMap.keySet().iterator();
        while (iter.hasNext()) {
            buf.append("<gateway>");
            String serviceName = (String)iter.next();
            String autoLogin = loginSettingsMap.get(serviceName);
            buf.append("<serviceName>" + serviceName + "</serviceName>");
            buf.append("<autoLogin>" + autoLogin + "</autoLogin>");
            buf.append("</gateway>");
        }
        buf.append("</gateways>");


        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    public static class ConferencePrivateDataProvider implements PrivateDataProvider {

        public ConferencePrivateDataProvider() {
        }

        public PrivateData parsePrivateData(XmlPullParser parser) throws Exception {
            GatewayPrivateData data = new GatewayPrivateData();

            boolean done = false;

            boolean isInstalled = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("gateways")) {
                    isInstalled = true;
                }

                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("gateway")) {
                    boolean gatewayDone = false;
                    String serviceName = null;
                    String autoLogin = null;
                    while (!gatewayDone) {
                        int eType = parser.next();
                        if (eType == XmlPullParser.START_TAG && parser.getName().equals("serviceName")) {
                            serviceName = parser.nextText();
                        }
                        else if (eType == XmlPullParser.START_TAG && parser.getName().equals("autoLogin")) {
                            autoLogin = parser.nextText();
                        }
                        else if (eType == XmlPullParser.END_TAG && parser.getName().equals("gateway")) {
                            data.addService(serviceName, Boolean.parseBoolean(autoLogin));
                            gatewayDone = true;
                        }
                    }
                }

                else if (eventType == XmlPullParser.END_TAG && parser.getName().equals("gateways")) {
                    done = true;
                }
                else if (!isInstalled) {
                    done = true;
                }
            }
            return data;
        }
    }


}