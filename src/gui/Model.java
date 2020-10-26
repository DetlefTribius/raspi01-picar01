package gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import raspi.hardware.TB6612MDriver;
import raspi.hardware.i2c.PCA9685;

// Vgl. https://www.baeldung.com/java-observer-pattern
// auch https://wiki.swechsler.de/doku.php?id=java:allgemein:mvc-beispiel
// http://www.nullpointer.at/2011/02/06/howto-gui-mit-swing-teil-4-interaktion-mit-der-gui/
// http://www.javaquizplayer.com/blogposts/java-propertychangelistener-as-observer-19.html
/**
 * 
 * Das Model haelt die Zustandsgroessen..
 *
 * 
 * 
 */
public class Model 
{
    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(Model.class);
    
    /**
     * Kennung isRaspi kennzeichnet, der Lauf erfolgt auf dem RasberryPi.
     * Die Kennung wird zur Laufzeit aus den Systemvariablen fuer das
     * Betriebssystem und die Architektur ermittelt. Mit dieser Kennung kann
     * die Beauftragung von Raspi-internen Programmen gesteuert werden.
     * Beispielsweise kann die GUI in einem Nicht-Raspi-Umgebung getestet
     * werden.
     */
    private final boolean isRaspi;
    /**
     * OS_NAME_RASPI = "linux" - Kennung fuer Linux.
     * <p>
     * ...wird verwendet, um einen Raspi zu erkennen...
     * </p>
     */
    public final static String OS_NAME_RASPI = "linux";
    /**
     * OS_ARCH_RASPI = "arm" - Kennung fuer die ARM-Architektur.
     * <p>
     * ...wird verwendet, um einen Raspi zu erkennen...
     * </p>
     */
    public final static String OS_ARCH_RASPI = "arm";
    
    /**
     * Referenz auf den GPIO-controller...
     * <p>
     * Der GPIO-Controller bedient die GPIO-Schnittstelle des Raspi.
     * </p>
     */
    private final GpioController gpioController;
    
    /**
     * i2cBus - Referenz auf den IC2Bus...
     */
    private static I2CBus I2CBUS = null;
    
    /**
     * Referenz auf den PWM-Driver
     */
    private PCA9685 pca9685 = null;
    
    /**
     * Referenz auf den Servo-Antrieb als Teil des PWM-Drivers
     * <p>
     * Der PWM-Driver umfasst beispielsweise 16 Channel.
     * Jeder Channel kann als Servo oder als Motor angesprochen werden.
     * </p>
     */
    private PCA9685.Servo servo = null;
    
    /**
     * PCA9685.Motor motorA
     * <p>
     * Der PWM-Driver kann jeweils einen Channel fuer einen Motor
     * einrichten. Hier wird die Referenz auf den Motor A hinterlegt. 
     * </p>
     */
    private PCA9685.Motor motorA = null;
    
    /**
     * PCA9685.Motor motorB
     * <p>
     * Der PWM-Driver kann jeweils einen Channel fuer einen Motor
     * einrichten. Hier wird die Referenz auf den Motor B hinterlegt. 
     * </p>
     */
    private PCA9685.Motor motorB = null;
    
    /**
     * servoMinSteering - Grenzwert (links) fuer den Servo-Antrieb
     */
    private final int servoMinSteering;
    
    /**
     * servoMaxSteering - Grenzwert (rechts) fuer den Servo-Antrieb
     */
    private final int servoMaxSteering;
    
    /**
     * 
     */
    private final float servoDiffSteering;
    
    /**
     * Referenz auf den TB6612MDriver...
     * <p>
     * Der Motortreiber haelt Referenzen auf Motor A und B und auf die Steuer-Pins
     * (GPIO-Eingabepins zur Richtungsvorgabe).
     * </p>
     */
    private TB6612MDriver motorDriver;
    
    /**
     * ADDRESS - Bus-Adresse des PCA9685-Bausteins (PWM-Driver), 
     * festgelegt durch 'Verdrahtung' auf dem Baustein... 
     */
    public final static int ADDRESS = 0x40; 
    
    /**
     * PWM_FREQUENCY = 50
     * <p>
     * Frequenzvorgabe fuer den PWM-Driver (Hz)
     * </p>
     */
    public final static int PWM_FREQUENCY = 50;
    
    /**
     * DELAY = 100 Pausenzeit (100 ms) fuer einzelne Aktionen...
     */
    public final static int DELAY = 100;
    
    /**
     * SERVO_CHANNEL = 0
     * <p>
     * Channel-Nummer fuer den Servo
     * </p>
     */
    public final static int SERVO_CHANNEL = 0;
    
    /**
     * MOTOR_A_CHANNEL = 4
     * <p>
     * PWM-Channel-Nummer Motor A
     * </p>
     */
    public final static int MOTOR_A_CHANNEL = 4;
    
    /**
     * MOTOR_B_CHANNEL = 5
     * <p>
     * PWM-Channel-Nummer Motor B
     * </p>
     */
    public final static int MOTOR_B_CHANNEL = 5;
    
    /**
     * Referenzvariable fuer den Status
     */
    private static Status status = Status.Reset;
    
    /**
     * counter - Taktzaehler (keine weitere funktionale Bedeutung)
     */
    private static long counter = 0L;

    /**
     * Taktung in ms
     */
    public final static int CYCLE_TIME = 1000;
    
    /**
     * Die Steuerung instanziieren...
     * <p>
     * Der ControlThread ermoeglicht die  regelmaessige Beauftragung von
     * Algorithmen (Regelungen)...
     * </p>
     * <p>
     * Die Servo- und Motor-Aktivitaeten werden zusatzlich an die User-Interaktion
     * beauftragt.
     * </p>
     */
    private ControlThread controlThread = new ControlThread(CYCLE_TIME);
    
    /**
     * gpioPinOutputMap nimmt die GpioPinDigitalOutput-Objekte auf, 
     * Key ist dabei jeweils der Pin_Name, z.B. "GPIO 21"...
     * <p>
     * Verwendung: Unter dem Key 'Name des GPIO' wird die Referenz auf den Pin abgelegt. 
     * </p>
     */
    private final java.util.TreeMap<String, GpioPinDigitalOutput> gpioPinOutputMap = new java.util.TreeMap<>();

    /**
     * PIN_MA - zur Ansteuerung des Motor A (Drehrichtung...)
     */
    private final static Pin PIN_MA = RaspiPin.GPIO_00;
    
    /**
     * PIN_MB - zur Ansteuerung des Motor B (Drehrichtung...)
     */
    private final static Pin PIN_MB = RaspiPin.GPIO_02;
    
    /**
     * ...die folgenden Pins werden angesprochen...
     */
    private final static Pin[] GPIO_PINS = 
    {
        PIN_MA,
        PIN_MB
    };
    
    /**
     * PIN_NAMES - String-Array mit den Namen der RaspiPin's.
     * Das Array wird aus dem Array GPIO_PINS[] befuellt.
     */
    public final static String[] PIN_NAMES = new String[GPIO_PINS.length];
    
    static 
    {
        // Befuellen des Arrays PIN_NAMES[] aus GPIO_PINS[]...
        for(int index = 0; index < GPIO_PINS.length; index++)
        {
            PIN_NAMES[index] = GPIO_PINS[index].getName();
        }
    }
    
    /**
     * NAME_START_BUTTON = "StartButton"
     */
    public final static String NAME_START_BUTTON = "StartButton";
    
    /**
     * NAME_STOP_BUTTON = "StopButton"
     */
    public final static String NAME_STOP_BUTTON = "StopButton";
    
    /**
     * NAME_END_BUTTON = "EndButton"
     */
    public final static String NAME_END_BUTTON = "EndButton";
    
    /**
     * dataMap - nimmt die Eingaben auf...
     * <p>
     * Ablage key => Eingabe-Object
     * </p>
     * Anm.: Hier Verwendung einer Synchronisierten Map.
     */
    private final java.util.Map<String, Object>  dataMap = Collections.synchronizedMap(new java.util.TreeMap<>());

    /**
     * Unter dem DATA_KEY werden Anzeigewerte fuer die Oberflaeche zusammengefasst.
     * Mit jedem Takt werden diese Anzeigewerte fuer die GUI bereitgestellt.
     */
    public final static String DATA_KEY = "dataKey"; 
    
    /**
     * DATA_SERVO_KEY = "dataServoKey"
     */
    public final static String DATA_SERVO_KEY = "dataServoKey";
    
    /**
     * SERVO_NULL_VALUE = "0" - Anfangswert des Servo-Ausschlages...
     * <p>
     * Dieser Anfangswert wird sowohl im Model als auch in der View eingestellt.
     * </p>
     */
    public final static String SERVO_NULL_VALUE = "0";
    
    /**
     * SERVO_MAX_VALUE = "30" - Endwert des Servo-Ausschlages
     * in der Eingabemasseinheit (GUI)...
     */
    public final static String SERVO_MAX_VALUE = "30";
    
    /**
     * DATA_MOTOR_KEY = "dataMotorKey"
     */
    public final static String DATA_MOTOR_KEY = "dataMotorKey";
    
    /**
     * MOTOR_NULL_VALUE = "0" - Anfangswert des Motor-Sollwertes...
     * <p>
     * Dieser Anfangswert wird sowohl im Model als auch in der View eingestellt.
     * </p>
     */
    public final static String MOTOR_NULL_VALUE = "0";
    
    /**
     * MOTOR_MAX_VALUE = "100" - Maximalwert des Motor-Sollwertes
     */
    public final static String MOTOR_MAX_VALUE = "100";
    
    /**
     * DATA_GEAR_KEY = "dataGearKey"
     * <p>
     * Key zum Zugriff auf die Getriebeschaltung ("D" oder "R")
     * </p>
     */
    public final static String DATA_GEAR_KEY = "dataGearKey";
    
    /**
     * DATA_GEAR_ENABLED_KEY = "dataGearEnabledKey"
     */
    public final static String DATA_GEAR_ENABLED_KEY = "dataGearEnabledKey";
    
    /**
     * LIMIT_FOR_GEAR_ENABLED = 0.1f - ab diesem Betrag (speed-Vorgabe) 
     * ist das Getriebe gesperrt.
     */
    public final static float LIMIT_FOR_GEAR_ENABLED = 0.1f;
    
    /**
     * Key "dataIsRunnableKey" => isRunnable
     */
    public final static String DATA_IS_RUNNABLE_KEY = "dataIsRunnableKey";
    
    /**
     * DATA_KEYS[] - Array mit ergaenzenden Keys zur zusaetzlichen Ablage in der
     * dataMap...
     */
    private final static String[] DATA_KEYS = 
    {
        Model.DATA_KEY,                         // => Anzeigewerte fuer die GUI, Zusammenfassung in Data    
        Model.DATA_SERVO_KEY,                   // => Sollwert Servo (Integer)
        Model.DATA_MOTOR_KEY,                   // => Sollwert Motor (Integer)
        Model.DATA_GEAR_KEY,                    // => Vorwaerts/Rueckwaerts (Getriebe, Gear, Transmission)
        Model.DATA_GEAR_ENABLED_KEY,            // => Boolscher Wert, Enabled ComboBox  
        Model.DATA_IS_RUNNABLE_KEY              // => isRunnable-Flag
    };
    
    /**
     * support - Referenz auf den PropertyChangeSupport...
     */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    /**
     * Default-Konstruktor 
     */
    public Model()
    {
        // Zuerst: Wo erfolgt der Lauf, auf einem Raspi?
        final String os_name = System.getProperty("os.name").toLowerCase();
        final String os_arch = System.getProperty("os.arch").toLowerCase();
        logger.debug("Betriebssytem: " + os_name + " " + os_arch);
        // Kennung isRaspi setzen...
        this.isRaspi = OS_NAME_RASPI.equals(os_name) && OS_ARCH_RASPI.equals(os_arch);
        
        // ...den gpioController anlegen...
        this.gpioController = isRaspi? GpioFactory.getInstance() : null;
       
        for (Pin pin: Model.GPIO_PINS)
        {
            final String key = pin.getName();
            if (isRaspi)
            {
                // Zugriff auf die Pin nur wenn Lauf auf dem Raspi...
                GpioPinDigitalOutput gpioPin = this.gpioController.provisionDigitalOutputPin(pin, key, PinState.LOW);
                gpioPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
                this.gpioPinOutputMap.put(key, gpioPin);
            } 
            else
            {
                // Der Lauf erfolgt nicht auf dem Raspi...
                this.gpioPinOutputMap.put(key, null);
            }
        }
        
        try
        {
            Model.I2CBUS = isRaspi? I2CFactory.getInstance(I2CBus.BUS_1) : null;
            // pca9685 - PWM-Modul (16 Channels, davon 1 Servo- und 2 Motor-Channel genutzt)
            this.pca9685 = (Model.I2CBUS != null)? PCA9685.getInstance(Model.I2CBUS.getDevice(ADDRESS)) : null;
            
            if (this.pca9685 != null)
            {
                this.pca9685.initialize();
                logger.info("initialize() erfolgreich.");
                Thread.sleep(DELAY);
                this.pca9685.setPWMFrequency(PWM_FREQUENCY);
                logger.info("setPWMFrequency() erfolgreich.");
                this.servo = this.pca9685.getServo(Model.SERVO_CHANNEL);
                // motorA - Channel einrichten...
                this.motorA = this.pca9685.getMotor(Model.MOTOR_A_CHANNEL);
                // motorB - Channel einrichten...
                this.motorB = this.pca9685.getMotor(Model.MOTOR_B_CHANNEL);
                
                final GpioPinDigitalOutput outputPinMA = this.gpioPinOutputMap.get(PIN_MA.getName());
                final GpioPinDigitalOutput outputPinMB = this.gpioPinOutputMap.get(PIN_MB.getName());
                this.motorDriver = new TB6612MDriver(outputPinMA, outputPinMB, this.motorA, this.motorB);
                this.motorDriver.reset();
                Thread.sleep(DELAY);
            }
            else
            {
                logger.info("Referenz pca9685 ist null!");  
            }
        } 
        catch (UnsupportedBusNumberException | IOException | InterruptedException exception)
        {
            logger.error(exception.toString(), exception);
            System.err.println(exception.toString());
            System.exit(0);
        }
        // Die Parameter ... aus dem Servo auslesen...
        servoMinSteering = (this.servo != null)? this.servo.getServoMinSteering() : 0;
        servoMaxSteering = (this.servo != null)? this.servo.getServoMaxSteering() : 0;
        servoDiffSteering = ((float)(servoMaxSteering - servoMinSteering))/2.0f;
        
        // *** Befuellen der dataMap... ***
        // Die dataMap muss mit allen Key-Eintraegen befuellt werden, sonst 
        // ist setProperty(String key, Object newValue) unwirksam!
        for (String key: Model.DATA_KEYS)
        {
            this.dataMap.put(key, null);
        }
        this.dataMap.put(Model.DATA_SERVO_KEY, Integer.valueOf(Model.SERVO_NULL_VALUE));
        this.dataMap.put(Model.DATA_MOTOR_KEY, Integer.valueOf(Model.MOTOR_NULL_VALUE));
        this.dataMap.put(Model.DATA_GEAR_KEY, Transmission.D);
        this.dataMap.put(Model.DATA_GEAR_ENABLED_KEY, Boolean.TRUE);
        this.dataMap.put(Model.DATA_IS_RUNNABLE_KEY, Boolean.TRUE);
    }
     
    /**
     * 
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * 
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * setProperty(String key, Object newValue) - Die View wird informiert...
     * 
     * @param key
     * @param newValue
     */
    public void setProperty(String key, Object newValue)
    {
        if (this.dataMap.containsKey(key))
        {
            Object oldValue = this.dataMap.get(key);
            // dataMap bekommt immer den newValue!
            this.dataMap.put(key, newValue);

            if (oldValue == null || newValue == null || !oldValue.equals(newValue))
            {
                logger.debug(key + ": " + oldValue + " => " + newValue);
                
                if (Model.DATA_SERVO_KEY.equals(key))
                {
                    try
                    {
                        doServo();
                    } 
                    catch (IOException exception)
                    {
                        logger.error("IOException in doServo()!", exception);                        
                    }
                }
                
                if (Model.DATA_MOTOR_KEY.equals(key))
                {
                    try
                    {
                        doMotor();
                    } 
                    catch (IOException exception)
                    {
                        logger.error("IOException in doServo()!", exception);                        
                    }
                    
                }
            }
            // firePropertyChange() - reagiert nur bei Property-Aenderung!
            support.firePropertyChange(key, oldValue, newValue);
        }
    }

    /**
     * doServo() - Uebertragung der GUI-Eingaben oder Zustandsaenderungen
     * auf den Servo...
     * <p>
     * Es gibt mehrere Beauftragungen.
     * <ul>
     *  <li>1. Eingabe an der GUI.</li>
     *  <li>2. "Regler"-Takt</li>
     * </ul>
     * </p>
     * @throws IOException 
     */
    private void doServo() throws IOException
    {
        final Object value = Model.this.dataMap.get(Model.DATA_SERVO_KEY);
        final boolean isInteger = (value instanceof java.lang.Integer);
        // servoData: Input durch den User, von -SERVO_MAX_VALUE ... +SERVO_MAX_VALUE
        final int servoData = (isInteger)? ((Integer)value).intValue() : 0;

        // Der folgende Teil nur auf dem Raspi...
        if (Model.this.isRaspi)
        {
            // Folgende Abbildung:
            // SERVO_NULL_VALUE (0)   => (Model.this.servoMinSteering + Model.this.servoMaxSteering)/2
            // SERVO_MAX_VALUE (30)   => Model.this.servoMaxSteering
            // -SERVO_MAX_VALUE (-30) => Model.this.servoMinSteering

            final float servoMaxValue = Float.valueOf(Model.SERVO_MAX_VALUE).floatValue();
            // relValue - Stellgroesse fuer setPWM()...
            final int relValue = Math.round(((float)servoData*Model.this.servoDiffSteering)/servoMaxValue);
            
            logger.debug("doServo(): servoData=" + servoData + " relValue=" + relValue);
            
            Model.this.servo.setPWM(relValue);
        } 
    }
    
    /**
     * @throws InterruptedException 
     * 
     */
    public void doMotor() throws IOException
    {
        // 1.) Speed bestimmen...
        final Object value = Model.this.dataMap.get(Model.DATA_MOTOR_KEY);
        final boolean isInteger = (value instanceof java.lang.Integer);
        // motorValue: Input durch den User, 
        // Eingabe moeglich von MOTOR_NULL_VALUE (0) ... + MOTOR_MAX_VALUE (100)
        final float motorValue = (isInteger)? ((Integer)value).floatValue() : 0.0f;        
        // motorMaxValue - max. moegliche Eingabe (zur Normierung...)
        final float motorMaxValue = Integer.valueOf(Model.MOTOR_MAX_VALUE).floatValue();
        // Normierung, speed jetzt zwischen 0.0f und 1.0f...
        final float speed = motorValue/motorMaxValue;
        
        // 2.) Schaltung (Gear) abfragen... 
        final Object gearData = Model.this.dataMap.get(Model.DATA_GEAR_KEY);
        final Transmission transmission = (gearData instanceof Transmission)? (Transmission) gearData : Transmission.D;
        final float factor = transmission.getFactor();
        
        // 3.) Getriebe sperren? => Wenn speed-Vorgabe groesser als Model.LIMIT_FOR_GEAR_ENABLED...
        setProperty(Model.DATA_GEAR_ENABLED_KEY, (speed > Model.LIMIT_FOR_GEAR_ENABLED)? Boolean.FALSE : Boolean.TRUE);
        
        // 4.) Motor steuern...
        if (Model.this.isRaspi)
        {
            // ...nur wenn Lauf auf dem Raspi...
            Model.this.motorDriver.setPWM(factor * speed);
        }
    }
    
    /**
     * setPWM(Integer value) - Sollwert-Setzen fuer den Servo...
     * @param value
     * @throws IOException 
     */
    public void setPWM(Integer value) throws IOException
    {
        final boolean isInteger = (value instanceof java.lang.Integer);
        // servoData: Input durch den User, von -SERVO_MAX_VALUE ... +SERVO_MAX_VALUE
        final int servoData = (isInteger)? ((Integer)value).intValue() : 0;
        logger.debug("doIt(): servoData = " + servoData); 

        // Anfangswert fuer den Stellwert relValue ist 0!
        int relValue = 0;
        
        final float servoMaxValue = Float.valueOf(Model.SERVO_MAX_VALUE).floatValue();
        
        // Der folgende Teil nur auf dem Raspi...
        if (Model.this.isRaspi)
        {
            // Folgende Abbildung:
            // SERVO_NULL_VALUE (0)   => (Model.this.servoMinSteering + Model.this.servoMaxSteering)/2
            // SERVO_MAX_VALUE (30)   => Model.this.servoMaxSteering
            // -SERVO_MAX_VALUE (-30) => Model.this.servoMinSteering
            
            relValue = Math.round(((float)servoData*Model.this.servoDiffSteering)/((float)servoMaxValue));
            
            logger.debug("servoData: " + servoData + " relValue: " + relValue);
            
            Model.this.servo.setPWM(relValue);
        } 
    }
    
    /**
     * notifyGUI()
     */
    public synchronized void notifyGUI()
    {
        if (this.dataMap.containsKey(Model.DATA_KEY))
        {
            final Data data = new Data(Long.valueOf(Model.counter));
            setProperty(Model.DATA_KEY, data);
        }
    }
    
    /**
     * start() - Methode wird durch den Start-Button beauftragt
     */
    public synchronized void start()
    {
        if (Model.status != Status.Started)
        {
            this.controlThread.start();
            setStatus(Status.Started);
            setCounter(0);
            logger.debug("Started()...");
            setProperty(DATA_IS_RUNNABLE_KEY, Boolean.FALSE);
        }
    }
    
    /**
     * stop() - Methode wird durch den Stop-Button beauftragt
     * 
     * Vgl.: https://dbs.cs.uni-duesseldorf.de/lehre/docs/java/javabuch/html/k100142.html
     * 
     * Vgl.: https://www.codeflow.site/de/article/java-thread-stop
     */
    public void stop()
    {
        final Integer motorNullValue = Integer.valueOf(Model.MOTOR_NULL_VALUE);
        setProperty(Model.DATA_MOTOR_KEY, motorNullValue);
        
        this.controlThread.stop();
        setStatus(Status.Stopped);
        // Offensichtlich kann ein neuer Thread eher gestartet werden,
        // als der alte beendet wurde. Daher verzoegern wir die 
        // Moeglichkeit des Neustartes ein wenig...
        // => Evtl. spaetere Ueberarbeitung notwendig! 
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException exception){}
        logger.debug("Stopped()...");
        setProperty(DATA_IS_RUNNABLE_KEY, Boolean.TRUE);
    }
     
    /**
     * shutdown()...
     * <p>
     * Der gpioController wird auf dem Raspi heruntergefahren...
     * </p>
     */
    public void shutdown()
    {
       logger.debug("shutdown()..."); 
       if (isRaspi)
       {
           final java.util.List<GpioPin> pinList = new java.util.ArrayList<>(this.gpioPinOutputMap.values());
           final GpioPin[] pins = pinList.toArray(new GpioPin[(pinList != null)? pinList.size() : 0]);
           this.gpioController.unprovisionPin(pins);
           this.gpioController.shutdown();  
       }
    }
    
    @Override
    public String toString()
    {
        return "gui.Model";
    }
    
    /**
     * setStatus(Status status)
     * 
     * @param status
     */
    public synchronized void setStatus(Status status)
    {
        Model.status = status;
    }
    
    /**
     * setCounter(int counter)
     * @param counter
     */
    public synchronized void setCounter(int counter)
    {
        Model.counter = counter;
    }
    
    /**
     * incrementCounter()
     */
    public synchronized void incrementCounter()
    {
        Model.counter++;
    }
    
    /**
     * ControlThread - Klasse zur Taktung der Aktionen... 
     *
     */
    class ControlThread implements Runnable
    {
        /**
         * 
         */
        private Thread worker;
        /**
         * isRunning - Flag...
         */
        private final AtomicBoolean isRunning = new AtomicBoolean(false); 
        
        /**
         * cycleTime - Zykluszeit in ms.
         */
        private final int cycleTime;
        
        /**
         * ControlThread(int cycleTime) - Konstruktor mit Zykluszeit in ms.
         * @param cycleTime - Zykluszeit (ms)
         */
        public ControlThread(int cycleTime)
        {
            this.cycleTime = cycleTime;
        }
        
        /**
         * 
         */
        public void start()
        {
            this.worker = new Thread(this);
            this.worker.start();
        }
        
        
        public void stop()
        {
            this.isRunning.set(false);
        }
        
        @Override
        public void run()
        {
            logger.debug("run()...");
            
            this.isRunning.set(true);
            
            while(this.isRunning.get())
            {
                doIt();
                try
                {
                    Thread.sleep(cycleTime);
                }
                catch(InterruptedException exception)
                {
                    Thread.currentThread().interrupt();
                    logger.error("Thread was interrupted, Failed to complete operation", exception);
                }
            }
        }
        
        /**
         * doIt()
         */
        private void doIt()
        {
            // incrementCounter() erhoeht den counter um 1...
            incrementCounter();
            
            try
            {
                doServo();
                
                doMotor();
            }
            catch(IOException exception)
            {
                logger.error("IOException in doIt()", exception);
                Thread.currentThread().interrupt();
            }
            
            // ...die relevanten Daten werden in die GUI uebertragen...
            notifyGUI();
        }
    }
}
