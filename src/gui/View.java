package gui;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/**
 * Interface View - Basisverhalten der View in Bezug auf
 * MVC-Zusammenspiel...
 * @author Detlef Tribius
 *
 */
public interface View extends PropertyChangeListener
{
    // View benoetig u.a auch eine addActionListener()-Methode...
    public void addActionListener(ActionListener listener);
}
