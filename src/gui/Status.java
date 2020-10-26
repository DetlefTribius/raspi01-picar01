/**
 * 
 */
package gui;

/**
 * enum Status beschreibt den Status des PiCar-S Systems.
 * 
 * @author Detlef Tribius
 *
 */
public enum Status
{
    /**
     * Reset("Reset")
     */
    Reset("Reset"),
    /**
     * Started("Started")
     */
    Started("Started"),
    /**
     * Stopped("Stopped")
     */
    Stopped("Stopped"),
    /**
     * Finish("Finish")
     */
    Finish("Finish");
    
    /**
     * String status - Kennung fuer den Status...   
     */
    private final String status;
    
    /**
     * private Status(String status) - Privater Konstruktor...
     * @param status
     */
    private Status(String status)
    {
        this.status = status;
    }
}
