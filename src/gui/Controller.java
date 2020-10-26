package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller implements ActionListener
{
    
    /**
     * logger - Logger, hier slf4j...
     */
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);      
    
    /**
     * view - Referenz auf die angemeldete View...
     */
    View view;
    
    /**
     * model - Referenz auf das Model, das Model haelt alle 
     * Daten/Zustandsgroessen der Anwendung...
     */
    Model model;
    
    /**
     * <p>
     * Der Controller verbindet View und Model.
     * </p>
     * <p>
     * Die View nimmt die Darstellung vor, das Model 
     * haelt die Daten und beauftragtt die View bei Datenaenderung.
     * </p>
     * @param view die View
     * @param model das Model
     */
    public Controller(View view, Model model)
    {
        this.view = view;
        this.view.addActionListener(this);
        this.model = model;
        this.model.addPropertyChangeListener(this.view);
    }
    
    /**
     * actionPerformed(ActionEvent event) wird durch das SwingWindow
     * beauftragt und muss die Aktion an das Model weiterreichen...
     * Das Model nimmt die Datenaenderung auf und reagiert entsprechend.
     * Dann erfolgt das Nachziehen der View durch das Model...
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        final JComponent source = (JComponent)event.getSource();
        final String name = source.getName();
        if (source instanceof JButton)
        {
            logger.debug("actionPerformed(): " + event.getActionCommand() + " " + name);
            if (Model.NAME_START_BUTTON.equals(name))
            {
                // Start-Button...
                this.model.start();
                
            }
            if (Model.NAME_STOP_BUTTON.equals(name))
            {
                // Stop-Button...
                this.model.stop();
                
            }
            
            if (Model.NAME_END_BUTTON.equals(name))
            {
                // Ende-Button...
                this.model.shutdown();
                System.exit(0);
            }
            return;
        }
        if (source instanceof JSlider)
        {
            final int value = ((JSlider)source).getValue();
            logger.info("actionPerformed(): " + name + " " + value);
            
            this.model.setProperty(name, Integer.valueOf(value));
            
            return;
        }
        if (source instanceof JComboBox<?>)
        {
            final Object selectedItem = ((JComboBox<?>)source).getSelectedItem();
            if (selectedItem instanceof Transmission)
            {
                final Transmission value = (Transmission) selectedItem;
                logger.info("actionPerformed(): " + event.getActionCommand() + " " + name + " " + value );
                this.model.setProperty(name, value);
                return;                
            }
        }
    }
}
