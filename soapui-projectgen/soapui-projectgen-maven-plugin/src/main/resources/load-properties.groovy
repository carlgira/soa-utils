groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
def propFileName = context.expand( '${#Project#properties.file}' )
file = new File(propFileName)
props = new java.util.Properties ()
props.load (new FileInputStream (file ))
Enumeration e = props.propertyNames();
while (e.hasMoreElements()) {
String key = (String) e.nextElement();
com.eviware.soapui.SoapUI.globalProperties.setPropertyValue( key, props.getProperty(key) )
}
return