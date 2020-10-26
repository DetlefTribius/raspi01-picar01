/**
 * 
 */
package gui;

/**
 * @author Detlef Tribius
 *
 * <p>
 * enum Transmission steht fuer den Vorwaerts- bzw. Rueckwaertsgang
 * des Antriebes.
 * </p>
 * <ul>
 *  <li>"D" - Vorwaertsgang</li>
 *  <li>"R" - Rueckwaertsgang</li>
 * </ul>
 */
public enum Transmission
{
    D("D") 
    {
        @Override
        public String getName()
        {
            return "Vorwärts";
        }

        @Override
        public float getFactor()
        {
            return 1.0f;
        }
    },
    
    R("R") 
    {
        @Override
        public String getName()
        {
            return "Rückwärts";
        }

        @Override
        public float getFactor()
        {
            return -1.0f;
        }
    };
    
    /**
     * Transmission(String value)
     * @param value
     */
    private Transmission(String value)
    {
        this.value = value;
    }
    /**
     * String value
     */
    private final String value;
    
    /**
     * 
     * @return
     */
    public String getValue()
    {
        return this.value;
    }
    /**
     * getFactor() - liefert Faktor zum Gang...
     * @return
     */
    public abstract float getFactor();
    /**
     * getName() - Bezeichnung des Ganges...
     */
    public abstract String getName();
}
