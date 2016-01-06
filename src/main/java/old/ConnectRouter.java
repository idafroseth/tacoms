package old;

//Hvis vi ønsker å kunne kjøre programmet uten konsoll og ønsker et brukergrensenitt maa de nedenforstående pakkene importeres
//import java.awt.Dimension;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JPasswordField;
//import javax.swing.JTextField;
//import javax.swing.SpringLayout;

import java.awt.Dimension;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.exception.OnepRemoteProcedureException;
import com.cisco.onep.core.util.OnepConstants;
// import com.cisco.onep.core.util.TLSUnverifiedElementHandler.Decision;
import com.cisco.onep.element.NetworkApplication;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.element.SessionConfig;
import com.cisco.onep.element.SessionConfig.SessionTransportMode;
import com.cisco.onep.element.SessionHandle;
import com.cisco.onep.interfaces.InterfaceFilter;
import com.cisco.onep.interfaces.InterfaceStatus.InterfaceState;

import model.TLSPinningHandler;

import com.cisco.onep.interfaces.NetworkInterface;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class ConnectRouter {

    // START SNIPPET: java_variables
    private String routerIP;
    private String username;
    private String password;
    private NetworkElement networkElement;
    private SessionHandle sessionHandle;
    private SessionConfig config;
    private String pinningFile;
    // END SNIPPET: java_variables

    private Logger logger;
    private Properties properties;
    
    public ConnectRouter(String[] args) {
    	//Create logger
    	logger = LoggerFactory.getLogger(this.getClass());
  
    	if(!importFil()){
    		parseOptions(args);
    		showAuthenticationDialog();
    	}
    }

    /**
     * Trying to connect to the network element 
     * @param applicationName
     * @return True if the connection success without an exception
     * @throws OnepException
     */
	public boolean connect(String applicationName) throws OnepException {

        NetworkApplication networkApplication = NetworkApplication.getInstance();
        //setting the applications name which is used for authenticaion later this is not needed
        networkApplication.setName(applicationName);
        
        try {
        	networkElement = networkApplication.getNetworkElement(routerIP);
        } catch (UnknownHostException e) {
        	getLogger().error(e.getLocalizedMessage(), e);
        	return false;
        } catch (OnepException e) {
            getLogger().error(e.getLocalizedMessage(), e);
            return false;
        }
        if (networkElement == null) {
            getLogger().error("Failed to get network element");
            return false;
        }
        getLogger().info("We have a NetworkElement - " + networkElement);

        getLogger().info("Connecting using transport type TLS");
        //TLS is the default connection supported
        config = new SessionConfig(SessionTransportMode.TLS);
        config.setPort(OnepConstants.ONEP_TLS_PORT);
        //Enable tls pinning
        config.setTLSPinning(pinningFile, new TLSPinningHandler(pinningFile));

        try {
            // START SNIPPET: connect
        	//her kan vi benytte connect(username, password) hvis vi skal koble til nettverkselementet uten å benytte oss av TLS
            sessionHandle = networkElement.connect(username, password, config);
            // END SNIPPET: connect
        } catch (OnepConnectionException e) {
            getLogger().error(e.getMessage());
            return false;
        } catch (OnepException e) {
            getLogger().error(e.getLocalizedMessage(), e);
            return false;
        }
        if (sessionHandle == null) {
            getLogger().error("Failed to connect to NetworkElement - " + networkElement);
            return false;
        }
        getLogger().info("Successful connection to NetworkElement - " + networkElement);
        return true;
    }
	/**
	 * Connecting without TLS
	 * @return true if the connection was successful
	 * @throws OnepException if we are not able to connect to the network element
	 */
	public boolean connect() throws OnepException {
		 NetworkApplication networkApplication = NetworkApplication.getInstance();
		 
		 try {
	        	networkElement = networkApplication.getNetworkElement(routerIP);
	        } catch (UnknownHostException e) {
	        	getLogger().error(e.getLocalizedMessage(), e);
	        	return false;
	        } catch (OnepException e) {
	            getLogger().error(e.getLocalizedMessage(), e);
	            return false;
	        }
		 
	        if (networkElement == null) {
	            getLogger().error("Failed to get network element");
	            return false;
	        }
	        getLogger().info("We have a NetworkElement - " + networkElement);

	        getLogger().info("Connecting to the NetworkElement whitout any security");
	
	        try {
	            sessionHandle = networkElement.connect(username, password);
	        } catch (OnepConnectionException e) {
	            getLogger().error(e.getMessage());
	            return false;
	        } catch (OnepException e) {
	            getLogger().error(e.getLocalizedMessage(), e);
	            return false;
	        }
	        if (sessionHandle == null) {
	            getLogger().error("Failed to connect to NetworkElement - " + networkElement);
	            return false;
	        }
	        getLogger().info("Successful connection to NetworkElement - " + networkElement);
	        return true;
	}
	  /**
     * Disconnects the application from the Network Element.
     *
     * @return True if the disconnect succeeded without an exception, else false if the application failed to disconnect
     *         from the Network Element.
     */
	public boolean disconnect() {
        try {
            networkElement.disconnect();
        } catch (Exception e) {
            getLogger().error("Failed to disconnect from Network Element");
            return false;
        }
        return true;
    }

   
    public String getElementHostname() {
        return routerIP;
    }

    /**
     * Gets the Network Element's InetAddress.
     *
     * @return The elementHostname as an InetAddress.
     * @throws UnknownHostException
     *             If the IP address of the NetworkElement cannot be resolved to a host.
     */
    public InetAddress getElementInetAddress() throws UnknownHostException {
        return InetAddress.getByName(getElementHostname());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    /**
     *
     * @return The tls pinning file that is specified via the command line or in the tutorial.properties file.
     */
    public String getPinningFile() {
        return pinningFile;
    }
    
    public NetworkElement getNetworkElement() {
        return networkElement;
    }

    /**
     * Sets the NetworkElement to be used.
     *
     * @param networkElement
     *            The networkElement to set.
     */
    public void setNetworkElement(NetworkElement networkElement) {
        this.networkElement = networkElement;
    }

    public Logger getLogger() {
        return logger;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public List<NetworkInterface> getAllInterfaces() {
        List<NetworkInterface> interfaceList = null;
        try {
            NetworkElement networkElement = getNetworkElement();
            interfaceList = networkElement.getInterfaceList(new InterfaceFilter());
        } catch (Exception e) {
            getLogger().error(e.getLocalizedMessage(), e);
        }
        return interfaceList;
    }


    // START SNIPPET: simulateShutdown
//    public void simulateShutdown(boolean state) throws OnepException, UnknownHostException {
//        List<NetworkInterface> networkInterfaces = getAllInterfaces();
//
//        // Loop over all interfaces
//        for (NetworkInterface networkInterface : networkInterfaces) {
//            if(networkInterface.getType() == NetworkInterface.Type.ONEP_IF_TYPE_ETHERNET) {
//
//                       // If this is the interface that the NetworkApplication is connected to, then
//                       // move onto the next interface.
//                       if (!networkInterface.getAddressList().contains(getElementInetAddress())) {
//                          if(state) {
//                             getLogger().info("Calling shutdown on " + networkInterface.getName() + "\n\n");
//                          } else {
//                             getLogger().info("Calling no shutdown on " + networkInterface.getName() + "\n\n");
//                          }
//                           // Call shutdown with the boolean value passed in.
//                           networkInterface.shutdown(state);
//                       }
//           }
//        }
//    }
    // END SNIPPET: simulateShutdown

    /**
     * Returns the first ETHERNET interface
     * from the interface list.
     *
     * @param networkElement
     * @return
     * @throws OnepException
     * @throws OnepRemoteProcedureException
     * @throws OnepConnectionException
     */
    public NetworkInterface getInterface()
            throws OnepException, OnepRemoteProcedureException,
            OnepConnectionException, UnknownHostException {
        List<NetworkInterface> ifList = getAllInterfaces();
        if (ifList != null) {
         for (NetworkInterface networkInterface : ifList) {
                 if(networkInterface.getType() == NetworkInterface.Type.ONEP_IF_TYPE_ETHERNET) {
                     if (!networkInterface.getAddressList().isEmpty() &&
                         !networkInterface.getAddressList().contains(getElementInetAddress())) {
                        return networkInterface;
                     }
                 }
            }
        }
        return null;
    }

   /**
    * Returns the first interface from the interface list
    * that is in up state.
    *
    * @return networkInterface
    * @throws OnepException
    * @throws OnepRemoteProcedureException
    * @throws OnepConnectionException
    */
   public NetworkInterface getUpInterface()
                  throws OnepException, OnepRemoteProcedureException,
                   OnepConnectionException, UnknownHostException {
      List<NetworkInterface> ifList = getAllInterfaces();
      if (ifList != null) {
         for (NetworkInterface networkInterface : ifList) {
            if(networkInterface.getType() == NetworkInterface.Type.ONEP_IF_TYPE_ETHERNET) {
               if (networkInterface.getStatus().getLinkState() == InterfaceState.ONEP_IF_STATE_OPER_UP
                  && !networkInterface.getAddressList().isEmpty() 
                  && !networkInterface.getAddressList().contains(getElementInetAddress())) {
                     return networkInterface;
               }
            }
         }
      }
      return null;
    }

   /**
    * Get an Ethernet Interface
    * @return the interface which is of type Ethernet.
    *
    * @throws OnepException
    * @throws OnepRemoteProcedureException
    * @throws OnepConnectionException
    * @throws UnknownHostException
    */
   public NetworkInterface getEthernetInterface() throws OnepException,
         OnepRemoteProcedureException, OnepConnectionException,
         UnknownHostException {

      List<NetworkInterface> ifList = networkElement.getInterfaceList(new InterfaceFilter(null,
                  NetworkInterface.Type.ONEP_IF_TYPE_ETHERNET));
      if (ifList != null) {
         for (NetworkInterface ni : ifList) {
            if (!ni.getAddressList().contains(getElementInetAddress())) {
               return ni;
            }
         }
      }
      return null;
   }

    /**
     * Parse options from the command line arguments, or if none are supplied,
     * from the properties file "tutorial.properties".
     *
     * If any required options are missing, the application will exit.
     *
     * @param args  The command line arguments passed to the main(...) method.
     */
    public void parseOptions(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i + 1 < args.length; i += 2) {
                if ((args[i].equals("-a")) || (args[i].equals("--hostname"))) {
                    routerIP = args[i + 1];
                } else if ((args[i].equals("-K")) || (args[i].equals("--keyStore"))) {
                    System.setProperty("javax.net.ssl.keyStore", args[i + 1]);
                } else if ((args[i].equals("-T")) || (args[i].equals("--trustStore"))) {
                    System.setProperty("javax.net.ssl.trustStore", args[i + 1]);
                } else if((args[i].equals("-P")) || (args[i].equals("--pinFile"))){
                	pinningFile = args[i + 1];
                }
            }
        } else {
            if (properties == null) {
                InputStream inputStream = null;
                try {
                    inputStream = this.getClass().getClassLoader().getResourceAsStream("tutorial.properties");
                    if (inputStream != null) {
                        properties = new Properties();
                        properties.load(inputStream);
                    }
                } catch (Exception e) {
                    getLogger().error(e.getLocalizedMessage(), e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ioe) {
                            getLogger().error("Error in closing the connect.properties file: " + ioe.getMessage());
                        }
                    }
                }
            }

            if (properties != null) {
                routerIP = properties.getProperty("hostname");
                if (properties.getProperty("keyStore") != null) {
                    System.setProperty("javax.net.ssl.keyStore", properties.getProperty("keyStore"));
                }
                if (properties.getProperty("trustStore") != null) {
                    System.setProperty("javax.net.ssl.trustStore", properties.getProperty("trustStore"));
                }
                if (properties.getProperty("trustStore") != null) {
                   pinningFile = properties.getProperty("pinningFile");
                }
            }
        }

        if (routerIP == null) {
            getLogger().info(getUsageString());
            System.exit(1);
        }
    }

    /**
     * Gets a string that shows the command to run the application.
     *
     * @return The usage string.
     */
    public String getUsageString() {
        return "Usage: java " + getClass().getCanonicalName() +
            " " + getUsageRequiredOptions() + " " + getUsageOptionalOptions();
    }

    /**
     * Gets a string that shows the options that are required to run the
     * application.
     *
     * @return The required options.
     */
    public String getUsageRequiredOptions() {
        return "-a <element addr>";
    }

    /**
     * Gets a string that shows the options that are optional to run the
     * application.
     *
     * @return The optional options.
     */
    public String getUsageOptionalOptions() {
        return "[-K <JSSE key store>] [-T <JSSE trust store>] [-P <tls pinning file>]";
    }

    /**
     * Prompts the user for their username and password to be used to
     * authenticate to the network element and passwords to decrypt keystores.
     *
     * Sets the username and password fields and the keyStorePassword and
     * trustStorePassword properties.
     */
    public void showAuthenticationDialog() {
        Console console = System.console();
        //Sjekker først om passord er satt fra Console
        if (console != null) {
            System.out.println("Authenticate to the network element\n");
            username = console.readLine("Username: ");
            char[] pwd = console.readPassword("Password: ");
            if (pwd != null) {
                password = new String(pwd);
                java.util.Arrays.fill(pwd, '\0');
            } else {
                password = "";
            }
            if (System.getProperty("javax.net.ssl.keyStore") != null) {
                char[] keyStorePassword = console.readPassword("JSSE key store password [none]: ");
                if (keyStorePassword != null && keyStorePassword.length > 0) {
                    System.setProperty("javax.net.ssl.keyStorePassword", new String(keyStorePassword));
                    java.util.Arrays.fill(keyStorePassword, '\0');
                }
            }
            if (System.getProperty("javax.net.ssl.trustStore") != null) {
                char[] trustStorePassword = console.readPassword("JSSE trust store password [none]: ");
                if (trustStorePassword != null && trustStorePassword.length > 0) {
                    System.setProperty("javax.net.ssl.trustStorePassword", new String(trustStorePassword));
                    java.util.Arrays.fill(trustStorePassword, '\0');
                }
            }
            /**
             * The following code can be used in those cases the program is run outside a terminal. Then the user will be 
             * presented a jpanel box.
             */
        } else {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(360, 200));
            JLabel usernameLbl = new JLabel("Username:");
            panel.add(usernameLbl);
            JTextField usernameFld = new JTextField(20);
            panel.add(usernameFld);
            JLabel passwordLbl = new JLabel("Password:");
            panel.add(passwordLbl);
            JPasswordField passwordFld = new JPasswordField(20);
            panel.add(passwordFld);
            JLabel kspLbl = new JLabel("JSSE key store password (leave blank for none):");
            JPasswordField kspFld = new JPasswordField(20);
            if (System.getProperty("javax.net.ssl.keyStore") != null) {
               panel.add(kspLbl);
               panel.add(kspFld);
            }
            JLabel tspLbl = new JLabel("JSSE trust store password (leave blank for none):");
            JPasswordField tspFld = new JPasswordField(20);
            if (System.getProperty("javax.net.ssl.trustStore") != null) {
               panel.add(tspLbl);
               panel.add(tspFld);
            }
            SpringLayout layout = new SpringLayout();
            layout.putConstraint(SpringLayout.EAST, usernameLbl, 80, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, usernameLbl, 0, SpringLayout.NORTH, panel);
            layout.putConstraint(SpringLayout.WEST, usernameFld, 10, SpringLayout.EAST, usernameLbl);
            layout.putConstraint(SpringLayout.NORTH, usernameFld, 0, SpringLayout.NORTH, usernameLbl);
            layout.putConstraint(SpringLayout.EAST, passwordLbl, 80, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, passwordLbl, 15, SpringLayout.SOUTH, usernameLbl);
            layout.putConstraint(SpringLayout.WEST, passwordFld, 10, SpringLayout.EAST, passwordLbl);
            layout.putConstraint(SpringLayout.NORTH, passwordFld, 0, SpringLayout.NORTH, passwordLbl);
            layout.putConstraint(SpringLayout.WEST, kspLbl, 15, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, kspLbl, 15, SpringLayout.SOUTH, passwordLbl);
            layout.putConstraint(SpringLayout.WEST, kspFld, 30, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, kspFld, 10, SpringLayout.SOUTH, kspLbl);
            layout.putConstraint(SpringLayout.WEST, tspLbl, 15, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, tspLbl, 15, SpringLayout.SOUTH, kspFld);
            layout.putConstraint(SpringLayout.WEST, tspFld, 30, SpringLayout.WEST, panel);
            layout.putConstraint(SpringLayout.NORTH, tspFld, 10,SpringLayout.SOUTH, tspLbl);
            panel.setLayout(layout);

            int option = JOptionPane.showOptionDialog(null, panel,
                    "Authenticate to the network element",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, null, null);
            if (option != 0) {
                System.exit(0);
            }

            username = usernameFld.getText();
            char[] passwordBuffer = passwordFld.getPassword();
            password = new String(passwordBuffer);
            java.util.Arrays.fill(passwordBuffer, '\0');
            char[] kspBuffer = kspFld.getPassword();
            if (kspBuffer.length > 0) {
                System.setProperty("javax.net.ssl.keyStorePassword", new String(kspBuffer));
                java.util.Arrays.fill(kspBuffer, '\0');
            }
            char[] tspBuffer = tspFld.getPassword();
            if (tspBuffer.length > 0) {
                System.setProperty("javax.net.ssl.trustStorePassword", new String(tspBuffer));
                java.util.Arrays.fill(tspBuffer, '\0');
            }
        }
    }
    protected boolean importFil(){
    	String filnavn = new java.io.File("").getAbsolutePath()+"/connect/input.txt";
    	System.out.println(filnavn);
    	try{
    	 	Scanner scFil;
    	 	scFil = new Scanner(new File(filnavn));
        	getLogger().info("Loading configure file " + filnavn);
        	while(scFil.hasNextLine() && !scFil.hasNext("SLUTT")){
        		String line = scFil.nextLine();
		//	System.out.println(line);
			if(line.equals("A")){
        			this.routerIP = scFil.nextLine();
			//	System.out.println(routerIP);
        		}else if(line.equals("UserName")){
        			this.username = scFil.nextLine();
        			System.out.println("brukernavn: " +username);
        		}else if(line.equals("Password")){
        			this.password = scFil.nextLine();
        		}
        	}
        
 	   	scFil.close();
    	}catch(IOException e){
    		getLogger().info("Could not find file");
    	}
   
    	if(this.username == null || this.password == null || routerIP == null ){
    		System.out.println(routerIP + "" + username +"" + password);
    		getLogger().info("The importfile has wrong format, please try again.");
    		return false;
    	}else{
    		return true;
    	}

    }

}

