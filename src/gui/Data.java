/**
 * 
 */
package gui;

/**
 * @author Detlef Tribius
 *
 * Die Data-Klasse fasst alle Daten zusammen, die zwischen der
 * Oberflaeche (GUI) und dem Model ausgetauscht werden.
 *
 */
public class Data implements Comparable<Data>
{

    /**
     * COUNTER_KEY = "counterKey" - Key zum Zugriff auf die Nummer/den Zaehler...
     */
    public final static String COUNTER_KEY = "counterKey";

    /**
     * counter - Zaehler
     */
    private final Long counter;
    
    /**
     * 
     * @param counter Long - Zaehlerstand
     */
    public Data(Long counter)
    {
        this.counter = (counter != null)? counter : Long.valueOf(0L);
    }
    
    /**
     * compareTo(Data another) - Vergleich auf Basis der lfd. Nummer (counter)
     */
    @Override
    public int compareTo(Data another)
    {
        return this.counter.compareTo(another.counter);
    }

    /**
     * hashCode() - auf Basis von counter...
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((counter == null) ? 0 : counter.hashCode());
        return result;
    }

    /**
     * equals(Object obj) - auf Basis von counter...
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Data other = (Data) obj;
        if (counter == null)
        {
            if (other.counter != null)
                return false;
        } else if (!counter.equals(other.counter))
            return false;
        return true;
    }
    
    /**
     * @return the counter
     */
    public final Long getCounter()
    {
        return this.counter;
    }

    /**
     * 
     * @return String[]
     */
    public String[] getKeys()
    {
        return new String[] {COUNTER_KEY};
    }

    /**
     * getValue(String key) - Bereitstellung der Anzeige...
     * @param key
     * @return string-Anzeige
     */
    public final String getValue(String key)
    {
        if (Data.COUNTER_KEY.equals(key))
        {
            return (this.counter != null)? this.counter.toString() : null;  
        }
        return null;
    }
    
    /**
     * toString() - zu Protokollzwecken...
     */
    @Override
    public String toString()
    {
        return new StringBuilder().append("[")
                                  .append(this.counter)
                                  .append("]")
                                  .toString();
    }
}
