package com.carlgira.asserter;

/**
 * Created by emateo on 19/05/2016.
 */
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/*package*/ class SimpleSampleCallbackHandlerImpl implements CallbackHandler
{
    private String userName; // the name of the user from the identity assertion token

    /*package*/ SimpleSampleCallbackHandlerImpl(String user)
    {
        userName = user;
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException
    {
        // loop over the callbacks
        for (int i = 0; i < callbacks.length; i++) {

            Callback callback = callbacks[i];

            // we only handle NameCallbacks
            if (!(callback instanceof NameCallback)) {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }

            // send the user name to the name callback:
            NameCallback nameCallback = (NameCallback)callback;
            nameCallback.setName(userName);
        }
    }
}
