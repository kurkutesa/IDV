/**
 * $Id: TrackDataSource.java,v 1.90 2007/08/06 17:02:27 jeffmc Exp $
 *
 * Copyright 1997-2005 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.repository.harvester;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.output.OutputHandler;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class Harvester extends RepositoryManager {

    /** _more_ */

    public static final String TAG_HARVESTER = "harvester";

    /** _more_ */
    public static final String TAG_HARVESTERS = "harvesters";

    /** _more_ */
    public static final String ATTR_SLEEPUNIT = "sleepunit";

    /** _more_ */
    public static final String UNIT_ABSOLUTE = "absolute";

    /** _more_ */
    public static final String UNIT_MINUTE = "minute";

    /** _more_ */
    public static final String UNIT_HOUR = "hour";

    /** _more_ */
    public static final String UNIT_DAY = "day";


    /** _more_ */
    public static final String ATTR_CLASS = "class";

    /** _more_ */
    public static final String ATTR_ROOTDIR = "rootdir";

    /** _more_ */
    public static final String ATTR_MONITOR = "monitor";

    /** _more_ */
    public static final String ATTR_ADDMETADATA = "addmetadata";

    /** _more_ */
    public static final String ATTR_ADDSHORTMETADATA = "addshortmetadata";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_ACTIVEONSTART = "activeonstart";

    /** _more_ */
    public static final String ATTR_TESTCOUNT = "testcount";

    /** _more_ */
    public static final String ATTR_TESTMODE = "testmode";

    /** _more_ */
    public static final String ATTR_SLEEP = "sleep";


    /** _more_ */
    public static final String ATTR_NAMETEMPLATE = "nametemplate";

    /** _more_ */
    public static final String ATTR_GROUPTEMPLATE = "grouptemplate";

    /** _more_ */
    public static final String ATTR_TAGTEMPLATE = "tagtemplate";

    /** _more_ */
    public static final String ATTR_DESCTEMPLATE = "desctemplate";

    /** _more_ */
    public static final String ATTR_BASEGROUP = "basegroup";



    /** _more_ */
    protected String baseGroupId = "";

    /** _more_ */
    protected String groupTemplate = "";

    /** _more_ */
    protected String nameTemplate = "${filename}";

    /** _more_ */
    protected String descTemplate = "";

    /** _more_ */
    protected String tagTemplate = "";



    /** _more_ */
    protected Harvester parent;

    /** _more_ */
    protected List<Harvester> children;

    /** _more_ */
    protected File rootDir;

    /** _more_ */
    private String name = "";


    /** _more_ */
    private Element element;

    /** _more_ */
    private boolean monitor = false;

    /** _more_ */
    private boolean active = false;

    /** _more_ */
    private boolean activeOnStart = false;




    /** _more_ */
    private double sleepMinutes = 5;

    /** _more_ */
    private String sleepUnit = UNIT_ABSOLUTE;

    /** _more_ */
    private int timestamp = 0;

    /** _more_ */
    private boolean addMetadata = false;

    /** _more_ */
    private boolean addShortMetadata = false;

    /** _more_ */
    private String id;

    /** _more_ */
    private boolean isEditable = false;

    /** _more_ */
    protected TypeHandler typeHandler;

    /** _more_ */
    private String error;

    /** _more_ */
    protected StringBuffer status = new StringBuffer();

    /** _more_ */
    User user;

    /** _more_ */
    private boolean testMode = false;

    /** _more_ */
    private int testCount = 100;


    /**
     * _more_
     *
     * @param repository _more_
     */
    public Harvester(Repository repository) {
        super(repository);
        this.id = repository.getGUID();
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public Harvester(Repository repository, String id) throws Exception {
        super(repository);
        this.id          = id;
        this.isEditable  = true;
        this.typeHandler = repository.getTypeHandler(TypeHandler.TYPE_FILE);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public Harvester(Repository repository, Element element)
            throws Exception {
        this(repository);
        this.children = createHarvesters(repository, element);
        for (Harvester child : children) {
            child.parent = this;
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     * @param createDate _more_
     * @param fromDate _more_
     * @param toDate _more_
     * @param filename _more_
     *
     * @return _more_
     */
    public String applyMacros(String s, Date createDate, Date fromDate,
                              Date toDate, String filename) {
        if (fromDate == null) {
            fromDate = createDate;
        }
        if (toDate == null) {
            toDate = fromDate;
        }
        s = getEntryManager().replaceMacros(s, createDate, fromDate, toDate);
        String[] macros = { "filename", filename, "fileextension",
                            IOUtil.getFileExtension(filename), };

        for (int i = 0; i < macros.length; i += 2) {
            String macro = "${" + macros[i] + "}";
            String value = macros[i + 1];
            s = s.replace(macro, value);
        }
        return s;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected User getUser() throws Exception {
        if (user == null) {
            user = repository.getUserManager().getDefaultUser();
        }
        return user;
    }


    /** _more_ */
    private Request request;

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Request getRequest() throws Exception {
        if (request == null) {
            request = new Request(getRepository(), getUser());
        }
        return request;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Group getBaseGroup() throws Exception {
        if ((baseGroupId == null) || (baseGroupId.length() == 0)) {
            return null;
        }
        Request request = new Request(getRepository(), getUser());
        Group   g = getEntryManager().findGroup(getRequest(), baseGroupId);
        if (g != null) {
            return g;
        }
        return getEntryManager().findGroupFromName(baseGroupId, getUser(),
                false);
    }



    /**
     * _more_
     *
     * @param selectId _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    protected void addBaseGroupSelect(String selectId, StringBuffer sb)
            throws Exception {
        Group baseGroup = getBaseGroup();
        String baseSelect = OutputHandler.getGroupSelect(getRequest(),
                                selectId);
        sb.append(HtmlUtil.hidden(selectId + "_hidden", ((baseGroup != null)
                ? baseGroup.getId()
                : ""), HtmlUtil.id(selectId + "_hidden")));
        sb.append(HtmlUtil.formEntry(msgLabel("Base Group"),
                                     HtmlUtil.disabledInput(selectId,
                                         ((baseGroup != null)
                                          ? baseGroup.getFullName()
                                          : ""), HtmlUtil.id(selectId)
                                          + HtmlUtil.SIZE_60) + baseSelect));
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element element) throws Exception {
        rootDir = new File(XmlUtil.getAttribute(element, ATTR_ROOTDIR, ""));

        this.typeHandler =
            repository.getTypeHandler(XmlUtil.getAttribute(element,
                ATTR_TYPE, TypeHandler.TYPE_ANY));

        groupTemplate = XmlUtil.getAttribute(element, ATTR_GROUPTEMPLATE,
                                             groupTemplate);
        this.baseGroupId = XmlUtil.getAttribute(element, ATTR_BASEGROUP, "");

        Group baseGroup = getBaseGroup();
        if (baseGroup != null) {
            baseGroupId = baseGroup.getId();
        }


        nameTemplate = XmlUtil.getAttribute(element, ATTR_NAMETEMPLATE,
                                            nameTemplate);
        descTemplate = XmlUtil.getAttribute(element, ATTR_DESCTEMPLATE, "");
        tagTemplate = XmlUtil.getAttribute(element, ATTR_TAGTEMPLATE,
                                           tagTemplate);



        this.name = XmlUtil.getAttribute(element, ATTR_NAME, "");
        this.monitor = XmlUtil.getAttribute(element, ATTR_MONITOR, monitor);
        this.addMetadata = XmlUtil.getAttribute(element, ATTR_ADDMETADATA,
                addMetadata);
        this.addShortMetadata = XmlUtil.getAttribute(element,
                ATTR_ADDSHORTMETADATA, addShortMetadata);
        this.activeOnStart = XmlUtil.getAttribute(element,
                ATTR_ACTIVEONSTART, activeOnStart);
        this.testCount = XmlUtil.getAttribute(element, ATTR_TESTCOUNT,
                testCount);
        this.testMode = XmlUtil.getAttribute(element, ATTR_TESTMODE,
                                             testMode);
        this.sleepUnit = XmlUtil.getAttribute(element, ATTR_SLEEPUNIT,
                sleepUnit);

        this.sleepMinutes = XmlUtil.getAttribute(element, ATTR_SLEEP,
                sleepMinutes);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param redirectToEdit _more_
     *
     * @return _more_
     */
    public String getRunLink(Request request, boolean redirectToEdit) {
        if (getActive()) {
            return HtmlUtil
                .href(request
                    .url(getRepository().getHarvesterManager()
                        .URL_HARVESTERS_LIST, ARG_ACTION, ACTION_STOP,
                            ARG_HARVESTER_ID, getId(),
                            ARG_HARVESTER_REDIRECTTOEDIT,
                            "" + redirectToEdit), msg("Stop"));
        } else {
            return HtmlUtil
                .href(request
                    .url(getRepository().getHarvesterManager()
                        .URL_HARVESTERS_LIST, ARG_ACTION, ACTION_START,
                            ARG_HARVESTER_ID, getId(),
                            ARG_HARVESTER_REDIRECTTOEDIT,
                            "" + redirectToEdit), msg("Start"));
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyEditForm(Request request) throws Exception {
        getEntryManager().clearSeenResources();
        rootDir = new File(request.getUnsafeString(ATTR_ROOTDIR,
                (rootDir != null)
                ? rootDir.toString()
                : ""));

        name = request.getString(ARG_NAME, name);

        typeHandler = repository.getTypeHandler(request.getString(ATTR_TYPE,
                ""));
        activeOnStart    = request.get(ATTR_ACTIVEONSTART, false);
        testCount        = request.get(ATTR_TESTCOUNT, testCount);
        testMode         = request.get(ATTR_TESTMODE, false);
        monitor          = request.get(ATTR_MONITOR, false);
        addMetadata      = request.get(ATTR_ADDMETADATA, false);
        addShortMetadata = request.get(ATTR_ADDSHORTMETADATA, false);
        sleepMinutes     = request.get(ATTR_SLEEP, sleepMinutes);
        sleepUnit        = request.getString(ATTR_SLEEPUNIT, sleepUnit);
        if (sleepUnit.equals(UNIT_HOUR)) {
            sleepMinutes = sleepMinutes * 60;
        } else if (sleepUnit.equals(UNIT_DAY)) {
            sleepMinutes = sleepMinutes * 60 * 60;
        }
        nameTemplate = request.getString(ATTR_NAMETEMPLATE, nameTemplate);
        groupTemplate = request.getUnsafeString(ATTR_GROUPTEMPLATE,
                groupTemplate);

        baseGroupId = request.getUnsafeString(ATTR_BASEGROUP + "_hidden", "");

        descTemplate = request.getUnsafeString(ATTR_DESCTEMPLATE,
                descTemplate);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtil.formEntry(msgLabel("Harvester name"),
                                     HtmlUtil.input(ARG_NAME, name,
                                         HtmlUtil.SIZE_40)));
        sb.append(HtmlUtil.formEntry(msgLabel("Create entries of type"),
                                     repository.makeTypeSelect(request,
                                         false, typeHandler.getType(), false,
                                         null)));

        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("Absolute (minutes)", UNIT_ABSOLUTE));
        tfos.add(new TwoFacedObject("Minutes", UNIT_MINUTE));
        tfos.add(new TwoFacedObject("Hourly", UNIT_HOUR));
        //        tfos.add(new TwoFacedObject("Daily",UNIT_DAY));

        String minutes = "" + sleepMinutes;
        if (sleepUnit.equals(UNIT_HOUR)) {
            minutes = "" + (sleepMinutes / 60);
        } else if (sleepUnit.equals(UNIT_DAY)) {
            minutes = "" + (sleepMinutes / (60 * 60));
        }
        String sleepType = HtmlUtil.select(ATTR_SLEEPUNIT, tfos, sleepUnit);
        String sleepLbl =
            "<br>" + HtmlUtil.space(3)
            + "e.g., 30 minutes = on the hour and the half hour<br>"
            + HtmlUtil.space(3);

        if (sleepUnit.equals(UNIT_ABSOLUTE)) {
            sleepLbl += "Would run in " + sleepMinutes + " minutes";
        } else {
            long sleepTime = Misc.getPauseEveryTime((int) sleepMinutes);
            Date now       = new Date();
            Date then      = new Date(now.getTime() + sleepTime);
            sleepLbl += "Would run at " + then;
        }



        //J-
        sb.append(
            HtmlUtil.formEntry(
                msgLabel("Run"), 
                HtmlUtil.checkbox(ATTR_TESTMODE, "true", testMode) + HtmlUtil.space(1) + msg("Test mode") +
                HtmlUtil.space(3) +  msgLabel("Count") + HtmlUtil.input(ATTR_TESTCOUNT, "" + testCount, HtmlUtil.SIZE_5) +
                HtmlUtil.br()    +
                HtmlUtil.checkbox(ATTR_ACTIVEONSTART, "true", activeOnStart) + HtmlUtil.space(1) + msg("Active on startup") +
                HtmlUtil.br() + 
                HtmlUtil.checkbox(ATTR_MONITOR, "true", monitor) + HtmlUtil.space(1) + msg("Run continually") +
                HtmlUtil.br() + HtmlUtil.space(3) +
                msgLabel("Every") + HtmlUtil.space(1) + HtmlUtil.input(ATTR_SLEEP, ""+ minutes, HtmlUtil.SIZE_5) + HtmlUtil.space(1) + sleepType + sleepLbl));
        //J+
    }


    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !getClass().equals(o.getClass())) {
            return false;
        }
        return this.id.equals(((Harvester) o).id);
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public void applyState(Element element) throws Exception {
        element.setAttribute(ATTR_CLASS, getClass().getName());
        element.setAttribute(ATTR_NAME, name);
        element.setAttribute(ATTR_ACTIVEONSTART, activeOnStart + "");
        element.setAttribute(ATTR_TESTMODE, testMode + "");
        element.setAttribute(ATTR_TESTCOUNT, testCount + "");
        element.setAttribute(ATTR_MONITOR, monitor + "");
        element.setAttribute(ATTR_ADDMETADATA, addMetadata + "");
        element.setAttribute(ATTR_ADDSHORTMETADATA, addShortMetadata + "");
        element.setAttribute(ATTR_TYPE, typeHandler.getType());

        element.setAttribute(ATTR_SLEEP, sleepMinutes + "");
        element.setAttribute(ATTR_SLEEPUNIT, sleepUnit);

        element.setAttribute(ATTR_TAGTEMPLATE, tagTemplate);
        element.setAttribute(ATTR_NAMETEMPLATE, nameTemplate);
        element.setAttribute(ATTR_GROUPTEMPLATE, groupTemplate);
        element.setAttribute(ATTR_BASEGROUP, baseGroupId);
        element.setAttribute(ATTR_DESCTEMPLATE, descTemplate);

        if (rootDir != null) {
            element.setAttribute(ATTR_ROOTDIR, rootDir.toString());
        }
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getContent() throws Exception {
        Document doc  = XmlUtil.makeDocument();
        Element  root = doc.createElement(TAG_HARVESTER);
        applyState(root);
        return XmlUtil.toString(root);
    }

    /**
     * _more_
     *
     * @param content _more_
     *
     * @throws Exception _more_
     */
    public void initFromContent(String content) throws Exception {
        if ((content == null) || (content.trim().length() == 0)) {
            return;
        }
        content = content.replace("${fromdate}", "${from_date}");
        content = content.replace("${year}", "${from_year}");
        content = content.replace("${month}", "${from_month}");
        content = content.replace("${monthname}", "${from_monthname}");
        content = content.replace("${day}", "${from_day}");
        Element root =
            XmlUtil.getRoot(new ByteArrayInputStream(content.getBytes()));
        init(root);
    }


    /**
     * _more_
     *
     * @param type _more_
     * @param filepath _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry processFile(TypeHandler type, String filepath)
            throws Exception {
        return null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param root _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static List<Harvester> createHarvesters(Repository repository,
            Element root)
            throws Exception {
        List<Harvester> harvesters = new ArrayList<Harvester>();
        List            children   = XmlUtil.findChildren(root,
                                         TAG_HARVESTER);
        for (int i = 0; i < children.size(); i++) {
            Element node = (Element) children.get(i);
            Class c = Misc.findClass(XmlUtil.getAttribute(node, ATTR_CLASS));
            Constructor ctor = Misc.findConstructor(c,
                                   new Class[] { Repository.class,
                    Element.class });
            Harvester harvester = (Harvester) ctor.newInstance(new Object[] {
                                      repository,
                                      node });
            harvesters.add(harvester);
            harvester.init(node);
        }
        return harvesters;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public File getRootDir() {
        return rootDir;
    }


    /**
     * _more_
     *
     * @param timestamp _more_
     *
     * @return _more_
     */
    public boolean canContinueRunning(int timestamp) {
        return getActive() && (timestamp == getCurrentTimestamp());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getCurrentTimestamp() {
        return timestamp;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public final void run() throws Exception {
        try {
            error = null;
            setActive(true);
            runInner(++timestamp);
        } catch (Exception exc) {
            getRepository().getLogManager().logError("In harvester", exc);
            error = "Error: " + exc + "<br>" + LogUtil.getStackTrace(exc);
        }
        setActive(false);
    }




    /**
     * _more_
     */
    public void clearCache() {}


    /**
     * _more_
     *
     * @return _more_
     */
    public String getError() {
        return error;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getExtraInfo() throws Exception {
        if (error != null) {
            return "<pre>" + error + "</pre>";
        }
        return "";
    }

    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {}

    /**
     * _more_
     */
    protected void doPause() {
        Misc.pauseEvery((int) getSleepMinutes());
    }

    /**
     * Set the Active property.
     *
     * @param value The new value for Active
     */
    public void setActive(boolean value) {
        active = value;
    }

    /**
     * Get the Active property.
     *
     * @return The Active
     */
    public boolean getActive() {
        return active;
    }



    /**
     * Set the Monitor property.
     *
     * @param value The new value for Monitor
     */
    public void setMonitor(boolean value) {
        monitor = value;
    }

    /**
     * Get the Monitor property.
     *
     * @return The Monitor
     */
    public boolean getMonitor() {
        return monitor;
    }

    /**
     *  Set the AddMetadata property.
     *
     *  @param value The new value for AddMetadata
     */
    public void setAddMetadata(boolean value) {
        addMetadata = value;
    }

    /**
     *  Get the AddMetadata property.
     *
     *  @return The AddMetadata
     */
    public boolean getAddMetadata() {
        return addMetadata;
    }


    /**
     *  Set the AddMetadata property.
     *
     *  @param value The new value for AddMetadata
     */
    public void setAddShortMetadata(boolean value) {
        addShortMetadata = value;
    }

    /**
     *  Get the AddMetadata property.
     *
     *  @return The AddMetadata
     */
    public boolean getAddShortMetadata() {
        return addShortMetadata;
    }


    /**
     * Set the SleepMinutes property.
     *
     * @param value The new value for SleepMinutes
     */
    public void setSleepMinutes(double value) {
        sleepMinutes = value;
    }

    /**
     * Get the SleepMinutes property.
     *
     * @return The SleepMinutes
     */
    public double getSleepMinutes() {
        return sleepMinutes;
    }

    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the IsEditable property.
     *
     * @param value The new value for IsEditable
     */
    public void setIsEditable(boolean value) {
        isEditable = value;
    }

    /**
     * Get the IsEditable property.
     *
     * @return The IsEditable
     */
    public boolean getIsEditable() {
        return isEditable;
    }


    /**
     * _more_
     *
     * @param element _more_
     * @param attr _more_
     *
     * @return _more_
     */
    public List<String> split(Element element, String attr) {
        if ( !XmlUtil.hasAttribute(element, attr)) {
            return new ArrayList<String>();
        }
        return StringUtil.split(XmlUtil.getAttribute(element, attr), ",",
                                true, true);
    }


    /**
     * Class HarvesterEntry _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public static class HarvesterEntry {

        /** _more_ */
        String url;

        /** _more_ */
        String name;

        /** _more_ */
        String description;

        /** _more_ */
        String baseGroupId;

        /** _more_ */
        String group;

        /**
         * _more_
         *
         * @param url _more_
         * @param name _more_
         * @param description _more_
         * @param group _more_
         * @param baseGroupId _more_
         */
        public HarvesterEntry(String url, String name, String description,
                              String group, String baseGroupId) {
            this.url         = url;
            this.name        = name;
            this.description = description;
            this.group       = group;
            this.baseGroupId = baseGroupId;

        }

        /**
         * _more_
         *
         * @param node _more_
         */
        public HarvesterEntry(Element node) {
            this.url  = XmlUtil.getAttribute(node, ATTR_URL, "");
            this.name = XmlUtil.getAttribute(node, ATTR_NAME, "");
            this.description = XmlUtil.getAttribute(node, ATTR_DESCRIPTION,
                    "");
            this.group       = XmlUtil.getAttribute(node, ATTR_GROUP, "");
            this.baseGroupId = XmlUtil.getAttribute(node, ATTR_BASEGROUP, "");
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String toString() {
            return url;
        }


    }

    /**
     * Set the ActiveOnStart property.
     *
     * @param value The new value for ActiveOnStart
     */
    public void setActiveOnStart(boolean value) {
        activeOnStart = value;
    }

    /**
     * Get the ActiveOnStart property.
     *
     * @return The ActiveOnStart
     */
    public boolean getActiveOnStart() {
        return activeOnStart;
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debug(String msg) {
        if (getTestMode()) {
            getRepository().getLogManager().logInfo(msg);
            msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            msg = msg.replace("\n", "<br>");
            status.append(msg);
            status.append(HtmlUtil.br());
        }
    }


    /**
     * Set the TestMode property.
     *
     * @param value The new value for TestMode
     */
    public void setTestMode(boolean value) {
        testMode = value;
    }

    /**
     * Get the TestMode property.
     *
     * @return The TestMode
     */
    public boolean getTestMode() {
        return testMode;
    }


    /**
     * Set the TestCount property.
     *
     * @param value The new value for TestCount
     */
    public void setTestCount(int value) {
        testCount = value;
    }

    /**
     * Get the TestCount property.
     *
     * @return The TestCount
     */
    public int getTestCount() {
        return testCount;
    }



}

