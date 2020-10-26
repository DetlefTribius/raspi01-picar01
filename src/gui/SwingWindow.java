package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Das SwingWindow ist ein JFrame (GUI-Swing-Klasse).
 * Es gestaltet die GUI mit einer Liste und den 
 * entsprechenden Buttons.
 * </p>
 * <p>
 * Die Datenhaltung erfolgt im Model.
 * </p> 
 * <p>
 * Vgl.: https://dbs.cs.uni-duesseldorf.de/lehre/docs/java/javabuch/html/k100242.html<br>
 * Auch: http://www.willemer.de/informatik/java/guimodel.htm<br>
 * </p>
 * <p>
 * Radio-Button: http://www.fredosaurus.com/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
 * </p>
 * @author Detlef Tribius
 *
 */
public class SwingWindow extends JFrame implements View   
{
    /**
     * serialVersionUID = 1L - durch Eclipse generiert...
     */
    private static final long serialVersionUID = 1L;

    /**
     * logger - Instanz zur Protokollierung...
     */
    private final static Logger logger = LoggerFactory.getLogger(SwingWindow.class);      

    /**
     * 
     */
    private ActionListener actionListener = null; 
    
    /**
     * textComponentMap - nimmt die Controls der JTextFielder auf...
     */
    private final java.util.Map<String, JTextComponent> textComponentMap = new java.util.TreeMap<>();
    
    /**
     * sliderMap - Map mit den JSlider-Controls...
     */
    private final java.util.Map<String, JSlider> sliderMap = new java.util.TreeMap<>();
    
    /**
     * comboBoxMap - nimmt die Controls der JComboBoxen auf...
     */
    private final java.util.Map<String, JComboBox<Transmission>> comboBoxMap = new java.util.TreeMap<>();
    
    /**
     * checkBoxMap - nimmt die Controls fuer die CheckBoxen auf...
     */
    private final java.util.Map<String, JCheckBox> checkBoxMap = new java.util.TreeMap<>();
  
    /**
     * TEXT_FIELD kennzeichnet ein TextField in der Beschreibung controData...
     */
    private static final String TEXT_FIELD = JTextField.class.getCanonicalName(); 
    /**
     * SLIDER kennzeichnet ein Slider in der Beschreibung controlData...
     */
    private static final String SLIDER = JSlider.class.getCanonicalName();
    
    private static final String COMBO_BOX = JComboBox.class.getCanonicalName();

    private static final String CHECK_BOX = JCheckBox.class.getCanonicalName();
    
    
    /**
     * controlData - Beschreibungsdaten der Oberflaechenelemente...
     */
    private final static String[][] controlData = new String[][]
    {
      //{ Data-Key            |GUI-Type  | Bezeichnung | Min.                   | Max.                       | Startwert } 
        {Model.DATA_SERVO_KEY, SLIDER,     "Servo", "-" + Model.SERVO_MAX_VALUE, "+" + Model.SERVO_MAX_VALUE, Model.SERVO_NULL_VALUE, "5", "10" },
        {Model.DATA_MOTOR_KEY, SLIDER,     "Motor", Model.MOTOR_NULL_VALUE,      Model.MOTOR_MAX_VALUE,       Model.MOTOR_NULL_VALUE, "5", "20" },
        {Model.DATA_GEAR_KEY, COMBO_BOX, "Gang" },
        {Data.COUNTER_KEY,     TEXT_FIELD, "Lfd. Nr." }
    };

    /**
     * CONTROL_IDS - Array mit den IDs der Contral-Controls...
     * <p>
     * Mit diesen ID's wird die GUI aufgebaut!
     * </p>
     * <p>
     * Die CONTROL_IDS muessen in den controlData's vorhanden sein!
     * </p>
     */
    private final static String[] CONTROL_IDS = new String[]
    {
        Model.DATA_SERVO_KEY,
        Model.DATA_MOTOR_KEY,
        Model.DATA_GEAR_KEY,
        Data.COUNTER_KEY
    };
    
    /**
     * controlPanelMap enthaelt die JPanels, die jedes Control enthalten.
     * Der Key ergibt sich aus controlData...
     */
    private final java.util.Map<String, JPanel> controlPanelMap = getControlPanelMap();
    
    
    /**
     * Start-Button...
     */
    private final JButton startButton = new JButton("Start");
    
    /**
     * Stop-Button... 
     */
    private final JButton stopButton = new JButton("Stop");

    /**
     * Ende-Button... 
     */
    private final JButton endButton = new JButton("Ende");
    
    /**
     * buttons[] - Zusammenfassung der JButton fuer das buttenPanel...
     */
    private final JButton[] buttons = new JButton[]
    {
        startButton,
        stopButton,
        endButton
    };
    
    /**
     * jContentPane - Referenz auf das Haupt-JPanel 
     */
    private JPanel jContentPane = null;
    
    /**
     * This is the default constructor
     */
    public SwingWindow(Model model)
    {
        super();
        initialize();
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                logger.debug("windowClosing(WindowEvent)...");
                model.shutdown();
                System.exit(0);
            }
        });
    }

    /**
     * getControlPanelMap()
     * @return controlPanelMap
     */
    private final Map<String, JPanel> getControlPanelMap()
    {
        java.util.Map<String, JPanel> controlPanelMap = new java.util.TreeMap<>();
        
        for(String[] controlParam: SwingWindow.controlData)
        {
            final String controlId = controlParam[0];
            final String controlType = controlParam[1];
            final String labelText = controlParam[2];
            {
                JPanel controlPanel = new JPanel();
                controlPanel.setLayout(new BoxLayout(controlPanel, javax.swing.BoxLayout.X_AXIS));
                controlPanel.add(Box.createHorizontalGlue());
                controlPanel.add(new JLabel(labelText));
                controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                if (TEXT_FIELD.equals(controlType))
                {
                    JTextField controlTextField = new JTextField(10);
                    controlTextField.setMaximumSize(new Dimension(100, controlTextField.getMinimumSize().height));
                    this.textComponentMap.put(controlId, controlTextField);
                    controlTextField.setEditable(false);
                    controlPanel.add(controlTextField);
                    controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                    controlPanelMap.put(controlId, controlPanel);
                    continue;
                }
                if (SLIDER.equals(controlType))
                {
                    JSlider controlSlider = getSlider(controlParam);
                    controlSlider.setName(controlId);
                    controlSlider.addChangeListener(new ChangeListener() 
                    {
                        @Override
                        public void stateChanged(ChangeEvent event)
                        {
                            JSlider source = (JSlider) event.getSource();
                            
                            int value = source.getValue();
                            
                            logger.info(source.getName() + ": " + value); 
                            
                            stateChangedDelegate(event); 
                        }
                    });
                    
                    this.sliderMap.put(controlId, controlSlider);
                    controlPanel.add(controlSlider);
                    controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                    
                    //
                    // Besonderheit JSlider: Aufnahme des controlPanel in einen weiteren,
                    // aeusseren Rahmen (=> borderLineControlPanel) mit Umrahmung... 
                    //
                    
                    // 1.) Abstand setzen...
                    controlPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                    
                    // 2.) Panel mit Rahmen erzeugen und controlPanel aufnehmen...
                    JPanel borderLineControlPanel = new JPanel();
                    borderLineControlPanel.setLayout(new BoxLayout(borderLineControlPanel, javax.swing.BoxLayout.X_AXIS));
                    borderLineControlPanel.add(controlPanel);
                    
                    javax.swing.border.Border border = BorderFactory.createLineBorder(Color.BLUE); 
                    borderLineControlPanel.setBorder(border);
                    
                    controlPanelMap.put(controlId, borderLineControlPanel);
                    continue;                    
                }
                if (COMBO_BOX.equals(controlType) && Model.DATA_GEAR_KEY.equals(controlId))
                {
                    // 1. Es handelt sich um eine ComboBox
                    // 2. Es ist die ComboBox mit der Gang-Angabe
                    JComboBox<Transmission> transmissionComboBox = new JComboBox<>(Transmission.values());
                    transmissionComboBox.setName(controlId);
                    
                    transmissionComboBox.setEnabled(true);
                    
                    // ActionListener einrichten...
                    transmissionComboBox.addActionListener(new ActionListener() 
                    {

                        @Override
                        @SuppressWarnings("unchecked")
                        public void actionPerformed(ActionEvent event)
                        {
                            JComboBox<BigDecimal> source = (JComboBox<BigDecimal>)event.getSource();   
                            logger.info(source.getName() + ": " + event.getActionCommand());   
                            
                            actionCommandDelegate(event);
                        }
                    });
                    
                    this.comboBoxMap.put(controlId, transmissionComboBox);
                    
                    transmissionComboBox.setMaximumSize(new Dimension(100, transmissionComboBox.getMinimumSize().height));
                    
                    controlPanel.add(transmissionComboBox);
                    controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                    
                    // 1.) Abstand setzen...
                    controlPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                    
                    // 2.) Panel mit Rahmen erzeugen und controlPanel aufnehmen...
                    JPanel borderLineControlPanel = new JPanel();
                    borderLineControlPanel.setLayout(new BoxLayout(borderLineControlPanel, javax.swing.BoxLayout.X_AXIS));
                    borderLineControlPanel.add(controlPanel);
                    
                    javax.swing.border.Border border = BorderFactory.createLineBorder(Color.BLUE); 
                    borderLineControlPanel.setBorder(border);
                    
                    controlPanelMap.put(controlId, borderLineControlPanel);
                    
                    continue;
                }
            }
        }    
        return controlPanelMap;
    }

    /**
     * getSlider(String[] controlParam)
     * @param controlParam - String-Array mit den Control-Parametern
     * @return JSlider
     */
    private final JSlider getSlider(String[] controlParam)
    {
        final String controlId = controlParam[0];
        final String controlType = controlParam[1];
        final String labelText = controlParam[2];
        
        final int min = (controlParam.length > 4)? Integer.parseInt(controlParam[3]) : 0;
        final int max = (controlParam.length > 4)? Integer.parseInt(controlParam[4]) : 100;
        final int value = (controlParam.length > 5)? Integer.parseInt(controlParam[5]) : min;

        final int defaultTickSpacing = 10;
        
        // Sind weitere Parameter vorhanden? 
        final int minorTickSpacing = (controlParam.length > 7)? Integer.parseInt(controlParam[6]) : defaultTickSpacing;
        final int majorTickSpacing = (controlParam.length > 7)? Integer.parseInt(controlParam[7]) : defaultTickSpacing;
        
        JSlider controlSlider = new JSlider(min, max, value);
        
        controlSlider.setMinorTickSpacing(minorTickSpacing);  
        controlSlider.setMajorTickSpacing(majorTickSpacing);

        controlSlider.setPaintTicks(true);
        controlSlider.setPaintLabels(true);
        controlSlider.setName(labelText);
        
        return controlSlider;
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(450, 250);
        this.setContentPane(getJContentPane());
        this.setTitle( "PiCar-S" );
        this.startButton.setName(Model.NAME_START_BUTTON);
        this.stopButton.setName(Model.NAME_STOP_BUTTON);
        this.endButton.setName(Model.NAME_END_BUTTON);
    }

    /**
     * This method initializes jContentPane
     * 
     * getJContentPane() - Methode baut das SwingWindow-Fenster auf.
     * Es werden alle sichtbaren Komponenten instanziiert.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            // BorderLayout hat die Bereiche
            // BorderLayou.NORTH
            // BorderLayout.CENTER
            // BorderLayout.SOUTH
            jContentPane.setLayout(new BorderLayout(10, 10));
            
            {   // NORTH
                JPanel northPanel = new JPanel();
                northPanel.setLayout(new BoxLayout(northPanel, javax.swing.BoxLayout.Y_AXIS));
                
                // northPanel wird in den Bereich NORTH eingefuegt.
                jContentPane.add(northPanel, BorderLayout.NORTH);
            }
            
            { // WEST
                // leeres Panel (Platzhalter)...
                jContentPane.add(new JPanel(), BorderLayout.WEST);
            }
            
            { // EAST
                // leeres Panel (Platzhalter)...
                jContentPane.add(new JPanel(), BorderLayout.EAST);
            }
            
            {   // *** CENTER-Panel ***
                // Struktur: centerPanel als BoxLayout, Ausrichtung von oben nach unten.
                // Jede Zelle nimmt ein Control auf. Sie wird als mit dem BoxLayout  
                // von links nach rechts eingerichten...
                JPanel centerPanel = new JPanel();
                centerPanel.setLayout(new BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));
                
                // Iteration ueber die darzustellenden Controls...
                for (final String controlId: SwingWindow.CONTROL_IDS)
                {
                    final JPanel controlPanel = this.controlPanelMap.get(controlId);
                    if (controlPanel != null)
                    {
                        centerPanel.add(controlPanel);
                        {
                            // Leerzeile...
                            JPanel emptyPanel = new JPanel();
                            emptyPanel.setLayout(new BoxLayout(emptyPanel, javax.swing.BoxLayout.Y_AXIS));
                            emptyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                            centerPanel.add(emptyPanel);
                        }    
                    }
                }
                
                jContentPane.add(centerPanel, BorderLayout.CENTER);
            }
            
            {   // *** SOUTH-Panel ***
                // buttonPanel beinhaltet die Button...
                JPanel buttonPanel = new JPanel();
                FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
                flowLayout.setAlignment(FlowLayout.RIGHT);
            
                for(JButton button: buttons)
                {
                    button.setHorizontalAlignment(SwingConstants.RIGHT);
                    button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent event)
                        {
                            final JButton source = (JButton)event.getSource();
                            logger.debug(source.getName());
                            //
                            actionCommandDelegate(event);
                        }
                    });
                    //
                    buttonPanel.add(button);
                }
                
                jContentPane.add(buttonPanel, BorderLayout.SOUTH);
            }
        }
        return jContentPane;
    }

    @Override
    public void addActionListener(ActionListener listener)
    {
        logger.debug("Controller hinzugefuegt (ActionListener)...");
        this.actionListener = listener;
    }

    /**
     * propertyChange(PropertyChangeEvent event) - wird vom Model her beaufragt
     * Anm.: => support.firePropertyChange(key, oldValue, newValue);
     * und muss die View evtl. nachziehen...  
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        final String propertyName = event.getPropertyName();
        final Object newValue = event.getNewValue();
        
        if (Model.DATA_KEY.equals(propertyName))
        {
            // propertyChange vom Model her mit DATA_KEY...
            if (newValue instanceof Data)
            {
                final Data newData = (Data) newValue;
                for(String key: newData.getKeys())
                {
                    if (this.textComponentMap.containsKey(key))
                    {
                        final JTextComponent textComponent = this.textComponentMap.get(key);
                        textComponent.setText(newData.getValue(key));
                        continue;
                    }
                }
            }
        }
        
        if(Model.DATA_SERVO_KEY.equals(propertyName) 
        || Model.DATA_MOTOR_KEY.equals(propertyName))
        {
            // Model signalisiert eine Aenderung der Servo-Vorgabe...
            if (this.sliderMap.containsKey(propertyName) && (newValue instanceof Integer))
            {
                final int value = ((Integer)newValue).intValue(); 
                JSlider slider = this.sliderMap.get(propertyName);
                slider.setValue(value);
                logger.debug(propertyName + ": " + value + " eingestellt...");
            }
        }
        
        if (Model.DATA_GEAR_KEY.equals(propertyName))
        {
            // Model signalisiert eine Aenderung am Datenteil zur ComboBox transmissionComboBox...
            // (Fahrtrichtung "D" oder "R")
            if (this.comboBoxMap.containsKey(propertyName) && (newValue instanceof Transmission))
            {
                JComboBox<Transmission> transmissionComboBox = this.comboBoxMap.get(propertyName);
                transmissionComboBox.setSelectedItem(newValue);
                logger.debug(propertyName + ": " + newValue + " eingestellt...");
            }
        }
        
        if (Model.DATA_GEAR_ENABLED_KEY.equals(propertyName))
        {
            final boolean isTransmissionEnabled = Boolean.TRUE.equals(newValue);
            JComboBox<Transmission> transmissionComboBox = this.comboBoxMap.get(Model.DATA_GEAR_KEY);
            transmissionComboBox.setEnabled(isTransmissionEnabled);
            logger.debug(propertyName + ": " + newValue + " eingestellt...");
        }
        
        if (Model.DATA_IS_RUNNABLE_KEY.equals(propertyName))
        {
            if (newValue instanceof java.lang.Boolean)
            {
                final boolean isRunnable = ((java.lang.Boolean) newValue).booleanValue();
                this.startButton.setEnabled(isRunnable);
            }
        }
        
        // Kontrollausgabe im Debuglevel...
        logger.debug(event.toString());
    }

    /**
     * 
     * @param event
     */
    private void actionCommandDelegate(java.awt.event.ActionEvent event) 
    {                                       
        if (this.actionListener != null) 
        {
            this.actionListener.actionPerformed(event);
        }
    }
    
    /**
     * 
     * @param event
     */
    private void stateChangedDelegate(ChangeEvent event)
    {
        if (this.actionListener != null)
        {
            JSlider source = (JSlider) event.getSource();

            final String name = source.getName();
            
            this.actionListener.actionPerformed(new ActionEvent(source,
                                                                ActionEvent.ACTION_PERFORMED,
                                                                name));
        }    
    }
}
