package de.dixti.jarscan;
/**
 * Thrown if any Exception occurs while initializing or scanning. 
 */
public class JarScanException extends RuntimeException {
    
    public JarScanException(String msg) {
        super(msg);
    }
    public JarScanException(Throwable cause) {
        super(cause);
    }
    
}
