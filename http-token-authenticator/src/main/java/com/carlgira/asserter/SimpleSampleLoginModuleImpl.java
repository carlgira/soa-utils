package com.carlgira.asserter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;
import weblogic.management.utils.NotFoundException;
import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.principal.WLSUserImpl;

/**
 * The simple sample authenticator's login module implementation.
 *
 * It is used in one of two modes:
 * - authentication where it validates the user's password
 *   then populates the subject with the user and the user's groups.
 * - identity assertion where it checks that the user exists,
 *   then populates the subject with the user and the user's groups.
 *
 * The SimpleSampleAuthenticationProviderImpl creates an options hash map
 * that is passed to this login module.  It contains one entry,
 * named "database", that is an object that manages the
 * user and group definitions.  It optionally contains another entry,
 * named "IdentityAssertion", that puts the login module in
 * "identity assertion" mode (vs. the default which is "authenticadtion"
 * mode).
 *
 * It uses the built in WLSUserImpl and WLSGroupImpl classes to
 * populate the subject with users and groups.
 *
 * @author Copyright (c) 2002 by BEA Systems. All Rights Reserved.
 */
final public class SimpleSampleLoginModuleImpl implements LoginModule
{
    private Subject                           subject;             // the subject for this login
    private CallbackHandler                   callbackHandler;     // where to get user names, passwords, ... for this login

    private boolean                           isIdentityAssertion; // are we in authentication or identity assertion mode?

    // Authentication status
    private boolean    loginSucceeded;                             // have we successfully logged in?
    private boolean    principalsInSubject;                        // did we add principals to the subject?
    private Vector     principalsForSubject = new Vector();        // if so, what principals did we add to the subject


    public void
    initialize(
            Subject         subject,
            CallbackHandler callbackHandler,
            Map             sharedState,
            Map             options
    )
    {
        // only called (once!) after the constructor and before login

        System.out.println("SimpleSampleLoginModuleImpl.initialize");
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        // Determine if we're in identity assertion or authentication mode
        isIdentityAssertion =
                "true".equalsIgnoreCase((String)options.get("IdentityAssertion"));

    }

    /**
     * Attempt to login.
     *
     * If we're in authentication mode, extract the user name and password
     * from the callback handler.  If the user exists and the password matches,
     * then populate the subject with the user and the user's group.  Otherwise,
     * the login fails.
     *
     * If we're in identity assertion mode, extract the user name (only)
     * from the callback handler.  If the user exists, then populate the
     * subject with the user and the user's groups.  Otherwise, the
     * login fails.
     *
     * @return A boolean indicating whether or not the login for
     * this login module succeeded.
     */
    public boolean login() throws LoginException
    {
        // only called (once!) after initialize

        System.out.println("SimpleSampleLoginModuleImpl.login");

        // loginSucceeded      should be false
        // principalsInSubject should be false

        // Call a method to get the callbacks.
        // For authentication mode, it will have one for the
        // username and one for the password.
        // For identity assertion mode, it will have one for
        // the user name.
        Callback[] callbacks = getCallbacks();

        // Get the user name.
        String userName = getUserName(callbacks);

        if (userName.length() > 0) {
            // We have a user name
            System.out.println("\tuserName=" + userName);
        } else {
            // anonymous login.
            System.out.println("\tempty userName");
        }

        loginSucceeded = true;

        // since the login succeeded, add the user and its groups to the
        // list of principals we want to add to the subject.
        principalsForSubject.add(new WLSUserImpl(userName));
        addGroupsForSubject(userName);

        return loginSucceeded;
    }

    /**
     * Completes the login by adding the user and the user's groups
     * to the subject.
     *
     * @return A boolean indicating whether or not the commit succeeded.
     */
    public boolean commit() throws LoginException
    {
        // only called (once!) after login

        // loginSucceeded      should be true or false
        // principalsInSubject should be false
        // user  should be null if !loginSucceeded, null or not-null otherwise
        // group should be null if user == null,    null or not-null otherwise

        System.out.println("SimpleSampleLoginModule.commit");
        if (loginSucceeded) {
            // put the user and the user's groups (computed during the
            // login method and stored in the principalsForSubject object)
            // into the subject.
            subject.getPrincipals().addAll(principalsForSubject);
            principalsInSubject = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Aborts the login attempt.  Remove any principals we put
     * into the subject during the commit method from the subject.
     *
     * @return A boolean indicating whether or not the abort succeeded.
     */
    public boolean abort() throws LoginException
    {
        // only called (once!) after login or commit
        // or may be? called (n times) after abort

        // loginSucceeded should be true or false
        // principalsInSubject should be false if user is null, otherwise true or false

        System.out.println("SimpleSampleLoginModule.abort");
        if (principalsInSubject) {
            subject.getPrincipals().removeAll(principalsForSubject);
            principalsInSubject = false;
        }
        return true;
    }

    /**
     * Logout.  This should never be called.
     *
     * @return A boolean indicating whether or not the logout succeeded.
     */
    public boolean logout() throws LoginException
    {
        // should never be called

        System.out.println("SimpleSampleLoginModule.logout");
        return true;
    }

    /**
     * Throw an invalid login exception.
     *
     * @param msg A String containing the text of the LoginException.
     *
     * @throws LoginException
     */
    private void throwLoginException(String msg) throws LoginException
    {
        System.out.println("Throwing LoginException(" + msg + ")");
        throw new LoginException(msg);
    }

    /**
     * Throws a failed login excception.
     *
     * @param msg A String containing the text of the FailedLoginException.
     *
     * @throws LoginException
     */
    private void throwFailedLoginException(String msg) throws FailedLoginException
    {
        System.out.println("Throwing FailedLoginException(" + msg + ")");
        throw new FailedLoginException(msg);
    }

    /**
     * Get the list of callbacks needed by the login module.
     *
     * @return The array of Callback objects by the login module.
     * Returns one for the user name and password if in authentication mode.
     * Returns one for the user name if in identity assertion mode.
     */
    private Callback[] getCallbacks() throws LoginException
    {
        if (callbackHandler == null) {
            throwLoginException("No CallbackHandler Specified");
        }

        Callback[] callbacks;
        if (isIdentityAssertion) {
            callbacks = new Callback[1]; // need one for the user name
        } else {
            callbacks = new Callback[2]; // need one for the user name and one for the password

            // add in the password callback
            callbacks[1] = new PasswordCallback("password: ",false);
        }

        // add in the user name callback
        callbacks[0] = new NameCallback("username: ");

        // Call the callback handler, who in turn, calls back to the
        // callback objects, handing them the user name and password.
        // These callback objects hold onto the user name and password.
        // The login module retrieves the user name and password from them later.
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw new LoginException(e.toString());
        } catch (UnsupportedCallbackException e) {
            throwLoginException(e.toString() + " " + e.getCallback().toString());
        }

        return callbacks;
    }

    /**
     * Get the user name from the callbacks (that the callback handler
     * has already handed the user name to).
     *
     * @param callbacks The array of Callback objects used by this login module.
     * The first in the list must be the user name callback object.
     *
     * @return A String containing the user name (from the user name callback object)
     */
    private String getUserName(Callback[] callbacks) throws LoginException
    {
        String userName = ((NameCallback)callbacks[0]).getName();
        if (userName == null) {
            throwLoginException("Username not supplied.");
        }
        System.out.println("\tuserName\t= " + userName);
        return userName;
    }

    /**
     * Add the user's groups to the list of principals to be added to the subject.
     *
     * @param A String containing the user name the user's name.
     */
    private void addGroupsForSubject(String userName)
    {
        // Get the user's list of groups (recursively - so, if user1 is a member
        // of group1 and group1 is a member of group2, then it returns group1 and
        // group2).  Iterate over the groups, adding each to the list of principals
        // to add to the subject.
        String groupName = "PerimeterAtnUsers";
        System.out.println("\tgroupName\t= " + groupName);
        principalsForSubject.add(new WLSGroupImpl(groupName));
    }

    /**
     * Get the password from the callbacks (that the callback handler
     * has already handed the password to) - that is, the password from
     * the login attempt.  Must only be used for authentication mode, not
     * for identity assertion mode.
     *
     * @param useName A String containing the name of the user
     * (already retrieved from the callbacks).  Only passed in
     * so that we can print a better error message if the password
     * is bogus.
     *
     * @param callbacks The array of Callback objects used by this login module.
     * The second in the list must be the password callback object.
     *
     * @return A String containing the password from the login attempt
     *
     * @throws LoginException if no password was supplied in the login attempt.
     */
    private String getPasswordHave(String userName, Callback[] callbacks) throws LoginException
    {
        PasswordCallback passwordCallback = (PasswordCallback)callbacks[1];
        char[] password = passwordCallback.getPassword();
        passwordCallback.clearPassword();
        if (password == null || password.length < 1) {
            throwLoginException("Authentication Failed: User " + userName + ".  Password not supplied");
        }
        String passwd = new String(password);
        System.out.println("\tpasswordHave\t= " + passwd);
        return passwd;
    }
}
