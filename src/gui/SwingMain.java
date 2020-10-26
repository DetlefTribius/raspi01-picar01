package gui;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

public class SwingMain              
{

    /** PROPERTIES_FILE - Name der Property-Datei, beinhaltet alle Systemeinstellungen */
    public final static String PROPERTIES_FILE = "system.properties";

    /** 
     * LOOK_AND_FEEL_KEY - Key-Eintrag unter dem in der properties-Datei
     * das LookAndFeel abgelegt ist.
     */
    public final static String LOOK_AND_FEEL_KEY = "lookAndFeel"; 
    
    /** lookAndFeel = Einstellung des LookAndFeel der Anwendung, gelesen aus der 
     *  Datei PROPERTIES_FILE 
     */
    private final String lookAndFeel;

    /**
     * ICON_IMAGE_KEY - Key unter dem das Icon-File abgelegt ist...
     */
    public final static String ICON_IMAGE_KEY = "iconImageFile";

    /**
     * iconImageFile - Name des ImageFile, wird aus der Property-Datei 
     * unter dem Key ICON_IMAGE_KEY = "iconImageFile" ausgelesen...
     */
    private final String iconImageFile;
    
    /** 
     * defaultLookAndFeel - boolsche Kennung, es ist kein LookAndFeel ueber die
     * Konfiguration eingestellt worden, verwende dann die 
     * LookAndFeelDecorated-Darstellung des Systems... 
     */ 
    private final boolean defaultLookAndFeel;
    
    /** main()-Funktionalitaet... */
    public static void main(String[] args)
    {
        // Start von allem...
        new SwingMain();
    }
    
    /**
     * Konstruktor der SwingMain-Klasse, hier wird alles wesentliche erledigt...
     */
    public SwingMain()
    {
        // *** Laden der Systemeinstellungen... ***        
        final java.io.InputStream inputStream = getClass().getResourceAsStream(SwingMain.PROPERTIES_FILE);
        final java.util.Properties properties = new java.util.Properties();
        try
        {
            properties.load(inputStream);
        }
        catch (Exception exception)
        {
            // Wenn kein Zugriff auf die Property-Datei, dann Applikation beenden!
            System.err.println( "Can't read the properties file '" + SwingMain.PROPERTIES_FILE + "'! " );
            System.exit(0);
        }
        // Key-Eintrag fuer das LookAndFeel in der property-Datei lautet 'lookAndFeel' 
        this.lookAndFeel = properties.getProperty(SwingMain.LOOK_AND_FEEL_KEY, "");
        // Wenn kein lookAndFeel in der Properties-Datei gesetzt wurde, 
        // dann den LookAndFeelDecorated-Darstellung verwenden...
        this.defaultLookAndFeel = (lookAndFeel.length() == 0)? true : false;
        // LookAndFeel einstellen...
        try
        {
            if (defaultLookAndFeel)
            {
                javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
            }
            else
            {
                javax.swing.UIManager.setLookAndFeel(this.lookAndFeel);
            }
        }
        catch(Exception exception)
        {
            System.err.println(exception.toString());
        }
        // *** Testausgabe... ***
        // System.out.println( javax.swing.UIManager.getSystemLookAndFeelClassName() );
        final Model model = new Model();
        SwingWindow swingWindow = new SwingWindow(model);
        //
        this.iconImageFile = properties.getProperty(SwingMain.ICON_IMAGE_KEY, "");
        try
        {
            URL resource = swingWindow.getClass().getResource(this.iconImageFile);
            BufferedImage image = ImageIO.read(resource);
            swingWindow.setIconImage(image);
        }
        catch (Throwable exception)
        {
            System.err.println("Can't read the image file '" + this.iconImageFile + "'!");
        }
        // swingWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        // Wir verweden hier aber: WindowConstants.DO_NOTHING_ON_CLOSE und fangen das
        // Event selbst ab, um die Nachbereitung zu ermoeglichen...
        swingWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        swingWindow.pack();
        swingWindow.setResizable(false);
        new Controller(swingWindow, model);
        EventQueue.invokeLater(new Runnable() 
        {
            @Override
            public void run()
            {
                swingWindow.setVisible(true);
            }
        });
    }
}
    
