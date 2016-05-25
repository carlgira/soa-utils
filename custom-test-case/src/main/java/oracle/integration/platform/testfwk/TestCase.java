package oracle.integration.platform.testfwk;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathFactory;

import com.carlgira.testcase.GroovyProcessor;
import oracle.fabric.common.BusinessEvent;
import oracle.fabric.common.FabricException;
import oracle.fabric.common.NormalizedMessage;
import oracle.fabric.common.NormalizedMessageImpl;
import oracle.integration.platform.PlatformMessageBundle;
import oracle.integration.platform.blocks.event.BusinessEventImpl;
import oracle.integration.platform.testfwk.repos.definition.TestCaseDAO;
import oracle.integration.platform.testfwk.repos.definition.TestCaseDAOFactory;
import oracle.integration.platform.testfwk.repos.definition.TestCaseDAOFilter;
import oracle.integration.platform.testfwk.xbean.AssertModel;
import oracle.integration.platform.testfwk.xbean.ComponentTestModel;
import oracle.integration.platform.testfwk.xbean.CompositeTestDocumentModel;
import oracle.integration.platform.testfwk.xbean.CompositeTestModel;
import oracle.integration.platform.testfwk.xbean.ElementModel;
import oracle.integration.platform.testfwk.xbean.LocationModel;
import oracle.integration.platform.testfwk.xbean.MessageModel;
import oracle.integration.platform.testfwk.xbean.WireActionsModel;
import oracle.soa.common.util.CXStringUtils;
import oracle.xml.jaxp.JXDocumentBuilderFactory;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestCase
{
    private static final String sClassName = TestCase.class.getSimpleName();
    private CompositeTestDocumentModel mTestCaseModel;
    private TestCase mParent;
    private TestCaseDAO mDAO;
    private int mActionCount = -1;
    private GroovyProcessor groovyProcessor;

    public TestCase(String compositeName, String suiteName, String testName, boolean isInclude, TestCaseDAOFactory factory)
    {
        try
        {
            this.mActionCount = -1;
            TestCaseDAO.Type type = isInclude ? TestCaseDAO.Type.Include : TestCaseDAO.Type.Test;
            TestCaseDAOFilter filter = TestCaseDAOFilter.uniqueFilter(compositeName, suiteName, testName, type);
            List<TestCaseDAO> defnDAOs = factory.load(filter);

            this.mDAO = ((TestCaseDAO)defnDAOs.get(0));
            this.mTestCaseModel = CompositeTestDocumentModel.Factory.parse(new ByteArrayInputStream(this.mDAO.getXmlDefn()));

            String include = this.mTestCaseModel.getCompositeTest().getInclude();
            if (include != null) {
                this.mParent = new TestCase(compositeName, suiteName, include, true, factory);
            }
            this.groovyProcessor = new GroovyProcessor(testName);
        }
        catch (Exception xe)
        {
            String msg = PlatformMessageBundle.getString("SOA-20067", new Object[] { testName, compositeName, xe.getMessage() == null ? "" : xe.getMessage() });

            throw new FabricException(msg, xe);
        }
    }

    public WireActionsModel getDefinedWireActions(String sourceURI)
    {
        for (WireActionsModel w : this.mTestCaseModel.getCompositeTest().getWireActionsArray()) {
            if (w.getWireSource().equals(sourceURI)) {
                return w;
            }
        }
        return null;
    }

    public WireActionsModel getDefinedWireActions(String sourceURI, String oper)
    {
        WireActionsModel firstMacthed = null;
        for (WireActionsModel w : this.mTestCaseModel.getCompositeTest().getWireActionsArray()) {
            if (CXStringUtils.compareStrs(w.getWireSource(), sourceURI))
            {
                if (CXStringUtils.compareStrs(w.getOperation(), oper)) {
                    return w;
                }
                for (AssertModel assertModel : w.getAssertArray())
                {
                    AssertModel.Expected expected = assertModel.getExpected();
                    if (expected != null)
                    {
                        LocationModel location = expected.getLocation();
                        if (location != null) {
                            if (location.isSetCallbackOperation()) {
                                if (location.getCallbackOperation().equals(oper)) {
                                    return w;
                                }
                            }
                        }
                    }
                }
                if (firstMacthed == null) {
                    firstMacthed = w;
                }
            }
        }
        return firstMacthed;
    }

    public WireActionsModel getWireActions(String sourceURI, String oper)
    {
        WireActionsModel w = getDefinedWireActions(sourceURI, oper);
        return this.mParent != null ? this.mParent.getDefinedWireActions(sourceURI) : w != null ? w : null;
    }

    public WireActionsModel getWireActions(String sourceURI)
    {
        WireActionsModel w = getDefinedWireActions(sourceURI);
        return this.mParent != null ? this.mParent.getDefinedWireActions(sourceURI) : w != null ? w : null;
    }

    public List<WireActionsModel> getIncludedWireActions(String sourceURI)
    {
        List<WireActionsModel> actions = new ArrayList();
        TestCase current = this;
        while (current != null)
        {
            WireActionsModel w = current.getDefinedWireActions(sourceURI);
            if (w != null) {
                actions.add(w);
            }
            current = current.mParent;
        }
        return actions;
    }

    public CompositeTestDocumentModel getModel()
    {
        return this.mTestCaseModel;
    }

    public NormalizedMessage createNormalizedMessage(MessageModel msg, TestCaseDAOFactory daoFactory)
    {
        try
        {
            Map<String, Object> payload = new HashMap();
            populatePayload(msg, daoFactory, payload);
            NormalizedMessage nMsg = new NormalizedMessageImpl();
            nMsg.setPayload(payload);

            return nMsg;
        }
        catch (Throwable t)
        {
            String errmsg = PlatformMessageBundle.getString("SOA-20076", new Object[] { t.getMessage() == null ? "" : t.getMessage() });

            throw new FabricException(errmsg, t);
        }
    }

    public Set<String> getAllComponentTestCaseNames(Map<String, ComponentPlugin> plugins)
    {
        TestCase testCase = this;
        HashSet<String> retVal = new HashSet();
        while (testCase != null)
        {
            for (CompositeTestModel.ComponentTest componentTest : testCase.getModel().getCompositeTest().getComponentTestArray()) {
                retVal.add(componentTest.getComponentName());
            }
            testCase = testCase.mParent;
        }
        return retVal;
    }

    public ComponentTestCase createComponentTestCase(String componentName, ComponentPlugin plugin, TestCaseDAOFactory daoFactory)
    {
        TestCase testCase = this;
        ArrayList<ComponentTestModel> al = null;
        while (testCase != null)
        {
            CompositeTestModel.ComponentTest[] componentTests = testCase.getModel().getCompositeTest().getComponentTestArray();
            for (CompositeTestModel.ComponentTest componentTest : componentTests) {
                if (componentTest.getComponentName().equals(componentName))
                {
                    TestFwkUtil.logFinep(sClassName, "createComponentTestCase", "found component test case for {0}", componentName);
                    if (al == null) {
                        al = new ArrayList();
                    }
                    List<TestCaseDAO> componentDAOList = daoFactory.load(TestCaseDAOFilter.uniqueFilter(this.mDAO.getCompositeDN(), this.mDAO.getTestSuite(), componentTest.getFilePath(), TestCaseDAO.Type.ComponentTest));
                    if (componentDAOList.size() < 1)
                    {
                        String errmsg = PlatformMessageBundle.getString("SOA-20103", new Object[] { componentTest.getFilePath(), this.mDAO.getName() });

                        throw new FabricException(errmsg);
                    }
                    try
                    {
                        ComponentTestModel componentModel = plugin.parseTestModel(new ByteArrayInputStream(((TestCaseDAO)componentDAOList.get(0)).getXmlDefn()));

                        al.add(0, componentModel);
                    }
                    catch (Throwable t) {}
                }
            }
            testCase = testCase.mParent;
        }
        return new ComponentTestCase(this.mDAO.getCompositeDN(), this.mDAO.getTestSuite(), this.mDAO.getName(), al, plugin, daoFactory);
    }

    public String getCompositeDN()
    {
        return this.mDAO.getCompositeDN();
    }

    public String getSuiteName()
    {
        return this.mDAO.getTestSuite();
    }

    public String getTestName()
    {
        return this.mDAO.getName();
    }

    public int getActionCount()
    {
        if (this.mActionCount < 0)
        {
            TestCase testCase = this;
            HashSet<String> wireSources = new HashSet();
            this.mActionCount = 0;
            while (testCase != null)
            {
                WireActionsModel[] wireActions = testCase.getModel().getCompositeTest().getWireActionsArray();
                if (wireActions != null) {
                    for (WireActionsModel wireAction : wireActions)
                    {
                        int inputCount = 0;int outputCount = 0;int faultCount = 0;
                        if ((wireAction.getAssertArray() != null) && (wireAction.getAssertArray().length > 0))
                        {
                            for (AssertModel assertModel : wireAction.getAssertArray())
                            {
                                AssertModel.Expected expected = assertModel.getExpected();
                                if (expected != null)
                                {
                                    LocationModel loc = expected.getLocation();
                                    if (loc != null) {
                                        if (loc.getKey() != null) {
                                            if (loc.getKey().equalsIgnoreCase("output")) {
                                                outputCount = 1;
                                            } else if (loc.getKey().equalsIgnoreCase("input")) {
                                                inputCount = 1;
                                            } else if (loc.getKey().equalsIgnoreCase("fault")) {
                                                faultCount = 1;
                                            }
                                        }
                                    }
                                }
                            }
                            this.mActionCount = (this.mActionCount + inputCount + outputCount + faultCount);
                        }
                    }
                }
                testCase = testCase.mParent;
            }
        }
        return this.mActionCount;
    }

    public TestCase getParent()
    {
        return this.mParent;
    }

    public BusinessEvent createBusinessEvent(CompositeTestModel.Event event, TestCaseDAOFactory testCaseFactory)
    {
        BusinessEvent businessEvent = null;
        try
        {
            QName action = event.getName();
            ElementModel elementModel = event.getElement();
            Element element = getElement(elementModel);
            Map properties = new HashMap();
            businessEvent = new BusinessEventImpl(action, properties, element);
        }
        catch (Throwable t)
        {
            String errmsg = PlatformMessageBundle.getString("SOA-20076", new Object[] { t.getMessage() == null ? "" : t.getMessage() });

            throw new FabricException(errmsg, t);
        }
        return businessEvent;
    }

    private void populatePayload(MessageModel message, TestCaseDAOFactory daoFactory, Map<String, Object> payload)
            throws Exception
    {
        JXDocumentBuilderFactory f = new JXDocumentBuilderFactory();
        f.setNamespaceAware(true);
        DocumentBuilder builder = f.newDocumentBuilder();
        Node firstElement = null;
        for (MessageModel.Part part : message.getPartArray())
        {
            if (part.isSetContent())
            {
                Node cNode = part.getContent().getDomNode();
                if ((cNode == null) || (cNode.getFirstChild() == null))
                {
                    TestFwkUtil.getLogger().info("TestCase: Null content node found for part " + part.getPartName());

                    continue;
                }
            }
            TestFwkUtil.buildElement(this.mDAO.getCompositeDN(), this.mDAO.getTestSuite(), daoFactory, XPathFactory.newInstance().newXPath(), part);
            if(this.groovyProcessor.hasGroovy(part)){
                String result = this.groovyProcessor.processXML(part);
                part.set(XmlObject.Factory.parse(result));
            }

            Node n = part.getContent().getDomNode();

            Document d = builder.newDocument();

            firstElement = n.getFirstChild();
            while ((firstElement != null) && (firstElement.getNodeType() != 1)) {
                firstElement = firstElement.getNextSibling();
            }
            if (firstElement == null) {
                throw new NullPointerException("no child node found for part");
            }
            if ((firstElement instanceof Element))
            {
                firstElement = d.importNode(firstElement, true);
                d.appendChild(firstElement);
                payload.put(part.getPartName(), d.getDocumentElement());
            }
        }
    }

    private Element getElement(ElementModel elementModel)
            throws Exception
    {
        JXDocumentBuilderFactory f = new JXDocumentBuilderFactory();
        f.setNamespaceAware(true);
        DocumentBuilder builder = f.newDocumentBuilder();
        Document d = builder.newDocument();

        Node n = elementModel.getContent().getDomNode();
        Node firstElement = n.getFirstChild();
        while ((firstElement != null) && (firstElement.getNodeType() != 1)) {
            firstElement = firstElement.getNextSibling();
        }
        if (firstElement == null) {
            throw new NullPointerException("no child node found for part");
        }
        firstElement = d.importNode(firstElement, true);
        d.appendChild(firstElement);

        return d.getDocumentElement();
    }
}

