package com.carlgira.asserter;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import weblogic.management.security.ProviderMBean;
import weblogic.security.service.ContextHandler;
import weblogic.security.spi.AuthenticationProviderV2;
import weblogic.security.spi.IdentityAsserterV2;
import weblogic.security.spi.IdentityAssertionException;
import weblogic.security.spi.PrincipalValidator;
import weblogic.security.spi.SecurityServices;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.HashMap;

public final class SimpleSampleIdentityAsserterProviderImpl implements AuthenticationProviderV2, IdentityAsserterV2
{
    final static private String TOKEN_TYPE   = "PerimeterAtnToken";
    final static private String TOKEN_PREFIX = "username=";

    private AppConfigurationEntry.LoginModuleControlFlag controlFlag;

    private String description;

    @Override
    public void initialize(ProviderMBean mbean, SecurityServices services)
    {
        System.out.println("SimpleSampleIdentityAsserterProviderImpl.initialize");
        SimpleSampleIdentityAsserterMBean myMBean = (SimpleSampleIdentityAsserterMBean)mbean;
        description = myMBean.getDescription() + "\n" + myMBean.getVersion();


        String flag = myMBean.getControlFlag();
        try {
            controlFlag =(AppConfigurationEntry.LoginModuleControlFlag)
                    AppConfigurationEntry.LoginModuleControlFlag.class.getField(flag).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid flag value" + flag,e);
        }


    }
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void shutdown()
    {
        System.out.println("SimpleSampleIdentityAsserterProviderImpl.shutdown");
    }

    @Override
    public AppConfigurationEntry getLoginModuleConfiguration() {
        return new AppConfigurationEntry(
                "com.carlgira.asserter.SimpleSampleLoginModuleImpl",
                controlFlag,
                new HashMap<String, Object>() {{
                }}
        );
    }

    @Override
    public AppConfigurationEntry getAssertionModuleConfiguration() {
        return getLoginModuleConfiguration();
    }

    @Override
    public PrincipalValidator getPrincipalValidator() {
        return new
                weblogic.security.provider.PrincipalValidatorImpl();
    }

    public IdentityAsserterV2 getIdentityAsserter()
    {
        return this;
    }

    public CallbackHandler assertIdentity(String type, Object token, ContextHandler context) throws IdentityAssertionException
    {
        System.out.println("SimpleSampleIdentityAsserterProviderImpl.assertIdentity");
        System.out.println("\tType\t\t= "  + type);
        System.out.println("\tToken\t\t= " + token);

        Object requestValue = context.getValue("com.bea.contextelement.servlet.HttpServletRequest");
        if ((requestValue == null) || (!(requestValue instanceof HttpServletRequest)))
        {
            System.out.println("do nothing");
        }
        else{
            HttpServletRequest request = (HttpServletRequest) requestValue;
            java.util.Enumeration names = request.getHeaderNames();
            while(names.hasMoreElements()){
                String name = (String) names.nextElement();
                System.out.println(name + ":" + request.getHeader(name));
            }
        }

        // check the token type
        if (!(TOKEN_TYPE.equals(type))) {
            String error =
                    "SimpleSampleIdentityAsserter received unknown token type \"" + type + "\"." +
                            " Expected " + TOKEN_TYPE;
            System.out.println("\tError: " + error);
            throw new IdentityAssertionException(error);
        }

        // make sure the token is an array of bytes
        if (!(token instanceof byte[])) {
            String error =
                    "SimpleSampleIdentityAsserter received unknown token class \"" + token.getClass() + "\"." +
                            " Expected a byte[].";
            System.out.println("\tError: " + error);
            throw new IdentityAssertionException(error);
        }

        // convert the array of bytes to a string
        byte[] tokenBytes = (byte[])token;
        if (tokenBytes == null || tokenBytes.length < 1) {
        String error =
                "SimpleSampleIdentityAsserter received empty token byte array";
        System.out.println("\tError: " + error);
        throw new IdentityAssertionException(error);
    }

        String tokenStr = new String(tokenBytes);

        // make sure the string contains "username=someusername
        if (!(tokenStr.startsWith(TOKEN_PREFIX))) {
            String error =
                    "SimpleSampleIdentityAsserter received unknown token string \"" + type + "\"." +
                            " Expected " + TOKEN_PREFIX + "username";
            System.out.println("\tError: " + error);
            throw new IdentityAssertionException(error);
        }

        // extract the username from the token
        String userName = tokenStr.substring(TOKEN_PREFIX.length());
        System.out.println("\tuserName\t= " + userName);

        // store it in a callback handler that authenticators can use
        // to retrieve the username.
        return new SimpleSampleCallbackHandlerImpl(userName);
    }


}
