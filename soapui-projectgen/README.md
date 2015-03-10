I've been working with SoapUI for years, it's an excellent tool for testing.
Creating one project manually is nothing, but when you are into a big project and lots of apps need their soapUI project it's a tedious to create them manually or ask the developers to do it (they don't do it most of the times).
So, for the app template o archetype i wanted to add a utility to automatic create that SoapUI Project. I just wanted to create the skeleton of the soapUI project so later someone else could put all the tests.
The idea is to build a soapui project based on a list of wsdls to create the TestSuites and another list of Wsdls for the mock services. The plugin is able to receive local wsdls or in an Url. 
It has the next parameters:
In the github are the sources of the maven plugin, a project to kwon how to use it, and and example Osb (oracle service bus) on how to execute the generated soapui project.
