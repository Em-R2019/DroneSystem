/***********************************************************************
 *            The Real Object Application Framework (ROAF)             *
 *        Copyright (C) 2010 Kristof Beiglböck / kbeigl@roaf.de        *
 *                                                                     *
 *  This file is part of the Real Object Application Framework, ROAF.  *
 *                                                                     *
 *  The ROAF is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published  * 
 *  by the Free Software Foundation, either version 3 of the License,  *
 *  or (at your option) any later version.                             *
 *                                                                     *
 *  The ROAF is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.               *
 *  See the GNU General Public License for more details.               *
 *                                                                     *
 *  You should have received a copy of the GNU General Public License  *
 *  along with the ROAF.  If not, see <http://www.gnu.org/licenses/>.  *
 ***********************************************************************/
package roaf.util;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

/**
 * Helper class to concentrate java.rmi.. in one class.
 * Methods are designed to catch all Exceptions, 
 * provide hints and return true on success, false on failure.
 * lookupRMIURL returns (Remote) object or null on failure.
 * server and client implementations should be coded to handle return 
 * values, continue to run and need only import java.rmi.Remote and
 * java.rmi.RemoteException
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public final class RMI 
{
	private RMI() {} // prevent instantiation

//	public static boolean verboseMode; ? showHints

	/**
	 * The security manager should only be set once per JVM and runtime. 
	 * Therefore the method has no unset counter part.
	 */
	public static boolean setRMISecurityManager()
	{
		try {
			if (System.getSecurityManager() == null)
				System.setSecurityManager( new RMISecurityManager() );
			return true;
		} catch (SecurityException e) {
			System.err.println("SecurityException: " + e.getMessage());
			System.err.println("hint: probably no permission to re/setSecurityManager.");
		}
		return false;
	}

//	client-server 'broker' - java.rmi.registry ------------

	/**
	 * Allocates a registry the specified host and port.
	 * If Registry is not found, false is returned.
	 * Method is called from createRuntimeRegistry.
	 * Attention! One (distributed) application should consequently 
	 * call this method only once or consequently with identical
	 * host and port. A previous allocation will be lost!
	 */
	public static boolean allocateRegistry( String host, int port )
	{
		System.out.println("allocateRegistry( " + host + ", " + port + " ) ..." );
		SecurityManager sm = System.getSecurityManager(); 
		if ( sm == null )
			System.out.println("attention! SecurityManager is not set (null)");
//		if ( rmiRegistry != null ) 
//			 do something or old reference is lost!
//		rename localhost to a network accessible hostname
		if ( host.equals( "localhost" ) 
		  || host.equals( "127.0.0.1" )
		  || host.equals( getLocalHostAddress() )) host = getLocalHostName();
		boolean foundReg = false;
//		check if port is negative, 0 (anonymous?) or out of range 1024-65535 ?
		try { 
			rmiRegistry = LocateRegistry.getRegistry( host, port ); 
		} 
		catch (RemoteException e) { 
			System.err.println("RemoteException: " + e.getMessage() );
		}
//		javadoc LocateRegistry: Note that a getRegistry call 
//		does not actually make a connection to the remote host. 
//		It simply creates a local reference to the remote registry and 
//		will succeed even if no registry is running on the remote host. 
//		Therefore, a subsequent method invocation to a remote registry 
//		returned as a result of this method may fail. 
//		=> test (any) subsequent method (without args) invocation:
		try { 
			rmiRegistry.list();
			foundReg = true;
		} catch (AccessException e) {
			System.err.println("AccessException: " + e.getMessage() );
		} catch (ConnectException e) {
			System.err.println( // "ConnectException: " + e.getMessage() + "\n" +
					"hint: start rmiregistry <port> from commandline on host.\n" +
					"hint: usually placed at " 
					+ System.getProperty("java.home") + "\\bin\\rmiregistry.exe");
		} catch (RemoteException e) {
			System.err.println("RemoteException: " + e.getMessage() );
		}
		catch (java.security.AccessControlException e) {
			System.err.println("java.security.AccessControlException: " + e.getMessage() );
//			if ( sm == null )
//			System.err.println("hint: insert RMI.setRMISecurityManager() before lookup.");
			System.err.println("hint: add -Djava.security.policy='path to policy file' to commandline");
		}
		if ( foundReg )
		{
			RMI.port = port; RMI.host = host; 
			rmiURL = "//" + host + ":" + port + "/";
			System.out.println("allocated registry on //" + host + ":" + port + "/" );
		}
		else 
		{ 
			RMI.port = -1; RMI.host = null; rmiURL = null; 
			System.err.println("no registry running on //" + host + ":" + port + "/" );
		}
		return foundReg;
	}
	
	/**
	 * Check if registry is already running on localhost and given port.
	 * If it is use it, 
	 * else create a registry for the runtime of the calling VM.
	 * Also see note in allocateRegistry (implicitly called).
	 */
	public static boolean createRuntimeRegistry( int port )
	{
//		check if port is negative, 0 (anonymous?) or out of range 1024-65535 ?
		if ( allocateRegistry( "localhost", port ))
			return true; // registry already running

		boolean createdReg = false;
		try { 
			rmiRegistry = LocateRegistry.createRegistry( port );
			createdReg = true;
		} 
		catch (ExportException e) {
			// should never occur after getRegistry above
			System.err.println("ExportException: " + e.getMessage() );
			System.err.println("hint: someone already started registry on port " + port );
		}
		catch (RemoteException e) { 
			System.err.println("RemoteException: " + e.getMessage() );
		}
		if ( createdReg )
		{
    		System.out.println("Created registry on //localhost:" + port + "/");
			System.out.println("note! registry will be killed after this runtime.");
//			observation: sometimes registry gets killed immediately?
			return allocateRegistry( "localhost", port ); // again, and set labels
		}
		return createdReg; // = false
	}

	/** 
	 * For the time being one registry should do for roaf development.
	 * Later it might make sense to allow more on different ports and 
	 * add methods. The registry is hidden to the roaf to ensure that
	 * all parties use the same host and port. Allthough different 
	 * applications might consequently use a different host and/or port.
	 */
	private static Registry rmiRegistry = null;

	/** if registry is set return port number, else return -1 */
	public static int getPort() { return port; }
	private static int port = -1;

	/** if registry is set return "hostname", else return null */
	public  static String getHost()   { return new String( host ); }   // ?
	private static String host = null;

	/** if registry is set return "//hostname:portnr/", else return null 
	 *  Can be implicitly used to check if a registry is allocated. */
//	TODO: map all urls via RMI.listRegisteredObjects() and match to remote objects
//	XXXX: current design only supports the last rmiURL 
	public  static String getRmiURL() { return new String( rmiURL ); } // ?
	private static String rmiURL = null;

	public static String getLocalHostName() 
	{
		try {
			return
			InetAddress.getLocalHost().getHostName(); // "kbeigl-acer"
//			InetAddress.getLocalHost().toString() )   // "kbeigl-acer/192.168.2.100"
		} catch (UnknownHostException e1) 
		{ System.err.println("Couldn't find name for localhost?"); }
		return "localhost"; // unreachable from other machines !
	}

	public static String getLocalHostAddress() 
	{
		try 
		{ return InetAddress.getLocalHost().getHostAddress(); } // "192.168.2.100" 
		catch (UnknownHostException e1) 
		{ System.err.println("Couldn't find localhosts IP address?"); }
		return "127.0.0.1"; // unreachable from other machines !
	}

//	server side - java.rmi.server -------------------------

//	TODO: how about this? 
//	Supply casting interface extending Remote with Generics and return boolean!
//	public static <T> T exportAsRemoteObject( Remote<T> t)
	/**
	 * Accepts object implementation or Remote object interface
	 * and returns true if successful, else false.
	 * The reference to the implementation or Remote interface
	 * should be held in the calling method, if needed.
	 * note: An object can be exported, before registry is running.
	 */
//	this method is independent and only unifies the roaf.RMI teminology!
	public static boolean exportAsRemoteObject( Remote remoteImplementation )
	{
		try { 
//			object can only be exported on local host
//			UnicastRemoteObject.exportObject( remoteImplementation );   // unclear!?
			UnicastRemoteObject.exportObject( remoteImplementation, 0); // unclear!?
			return true;
		} 
		catch (ExportException e1) { 
//			java.rmi.server.ExportException: object already exported
//			www.coderanch.com/t/210349/Distributed-Java/java/object-already-exported-exception-with
//			Does your server implementation extends UnicastRemoteObject? 
//			If yes, then you do not need to explicitly export it using UnicastRemoteObject.exportObject() methods. 
//			As soon as you initialize a class that extends UnicastRemoteObject, the object is exported by RMI. 
//			If you try to export a class instance that extends UnicastRemoteObject, 
//			you will always get the exception that you have mentioned.
//			e1.printStackTrace(); 
			return true; // !!
		}
		catch (RemoteException e1) { e1.printStackTrace(); }
		return false;
	}

	/** 
	 * from UnicastRemoteObject javadocs:
	 * Removes the Remote implementing object from the RMI runtime. 
	 * The object can no longer accept incoming RMI calls. 
	 * The object is forcibly unexported even if there are pending calls 
	 * to the remote object or the remote object still has calls in progress. 
	 */
//	Add force parameter later, 
//	if needed:      unexportRemoteObject( Remote remoteImpl, boolean force )
//	TODO: test and analyze method
	public static boolean unexportRemoteObject( Remote remoteImplementation )
	{
//		why should export return false, instead of throwing NSOEx?
		boolean check = false;
		try 
		{
			check = UnicastRemoteObject.unexportObject( remoteImplementation, true);
			if ( !check ) 
				System.err.println("unexportObject returned false, instead of Excep! why?");
		} 
		catch (NoSuchObjectException e) { e.printStackTrace(); }
		return check;
	}

	public static void listRegisteredObjects()
	{
		String[] names = null;
		try { names = rmiRegistry.list(); } 
		catch (AccessException e) { e.printStackTrace(); } 
		catch (RemoteException e) { e.printStackTrace(); }
		for (int boundName = 0; boundName < names.length; boundName++)
			System.out.println( names[boundName] );
	}

	/**
	 * 'publish' remoteObject on allocated registry.
	 * remoteObject can be the implementing object or the Remote interface to it.
	 */
	public static boolean registerRemoteObject( String name, Remote remoteObject )
	{
//		if rmiRegistry is allocated .. 
		try {
//			TODO VERIFY THIS:
//			the registration of the Object with bind() implicitly starts 
//			a non daemon thread to process incoming method invocations.
//			The thread only terminates, when the Object is freed with unbind()

//			TODO: implement in RMI class
//			throw Exception, if name is already registered! 
//			otherwise the old instance is kicked offline, 
//			while still working for connected clients!!

//			maybe use bind, then rebind in RMI ! 
//			rmiRegistry.bind( name, remoteObject );
//			hint: <String> already was bound, new object will be bound: rebind
			rmiRegistry.rebind( name, remoteObject );
// !OR use!      Naming.rebind( rmiURL, remoteObject );
			System.out.println("remoteObject bound to " + name + " at " + new Date());
//			TODO: ((RealObject) remoteObject).setRMIURL( name ); !
			return true;
		} catch (AccessException e) {
			System.err.println("AccessException: " + e.getMessage() );
			e.printStackTrace();
		} catch (ConnectException e) {
			System.err.println("ConnectException: " + e.getMessage() );
			System.err.println(
					"hint: start rmiregistry <port> from commandline on host.\n" +
					"hint: usually placed at " 
					+ System.getProperty("java.home") + "\\bin\\rmiregistry.exe");
//	Naming.rebind:
//		} catch (java.net.MalformedURLException e) {
//			System.err.println(""Can't bind the registrar."");
//		} catch (java.net.UnknownHostException e) {
//			System.err.println("Can't get current host.");
		} catch (RemoteException e) {
			System.err.println("RemoteException: " + e.getMessage() );
			e.printStackTrace();

//			geht bisher auch ohne codebase auf cmdline? 
//			s.u. wenn ComputeEngine (Compute) gebinded wird!
//			-> in RMI entsprechend implementieren bzw. abfangen mit hint

//			hint: ohne codebase in commandline!
//			java.rmi.ServerException: RemoteException occurred in server thread; 
//			nested exception is: java.rmi.UnmarshalException: error unmarshalling arguments; 
//			nested exception is: java.lang.ClassNotFoundException: compute.Compute
		}
//		catch (java.io.IOException e) {
//			ErrorMessages.fatalError("Can't open multicast socket.", e); 
//	    }
		catch (java.security.AccessControlException e) {
			System.err.println("java.security.AccessControlException: " + e.getMessage() );
//			if ( sm == null )
//			System.err.println("hint: insert RMI.setRMISecurityManager() before lookup.");
			System.err.println("hint: add -Djava.security.policy='path to policy file' to commandline");
		} catch (Exception e) {
//			ErrorMessages.fatalError("Unknown exception on server startup", e);
		}
		return false; // test
	}

//	client side - java.rmi --------------------------------

	/**
	 * Use Naming.lookup to lookup remote object and return Remote interface.
	 * Instead of two steps
	 *  Registry registry = LocateRegistry.getRegistry( host, port, remObjName )
	 *  stub = (Remote) registry.lookup( remObjName )
	 * If method is not successful (returns null) hints are provided.
	 * If method is successful the returned reference can be casted.
	 * The advantage of Naming vs. Registry is that Naming accepts
	 * an URL String with host, port and remote object name.
	 * If host is missing: localhost is used and 
	 * if port is missing: 1099 is used as default value.
	 * "rmi:" at beginning is optional (and helpful)
	 *  rmi://hostName:portNr/symbolicName
	 *  rmi://hostName/symbolicName
	 *  rmi://:portNr/symbolicName
	 *  rmi:/symbolicName (only one slash)
	 * Naming parses the URL implicitly lookes up the Registry: 
	 * Registry registry = LocateRegistry.getRegistry( .. );
	 * then the name:
	 * Remote remote = (Remote) registry.lookup(name);
	 */
//	TODO: how about this? to return casted reference!
//	public static  <T> T lookupRMIURL( String RMIURL )
//	returns Remote stub 
	public static Remote lookupRMIURL( String RMIURL )
	{
		Remote remoteObjectStub = null;
		SecurityManager sm = System.getSecurityManager(); 
		if ( sm == null )
			System.out.println("attention! SecurityManager is not set (null)");
		try 
		{
			System.out.println("Naming.lookup: " + RMIURL );
//			Naming for formatted rmi URL String
			remoteObjectStub = (Remote) Naming.lookup( RMIURL ); 
		} 
		catch ( ConnectException e ) { 
			System.err.println( e.toString() + ": " + e.getMessage() );
			System.err.println("hint: check if host is running and if port is correct.");
//			or RMI was started after 'this' app
//			if ( sm != null )
//			sm.checkConnect(host, port);
//		} catch (java.rmi.UnknownHostException exc) {
//		} catch (java.rmi.ConnectIOException exc) {
		} catch (MalformedURLException e) {
// BINGO	ErrorMessages.fatalError("Can't bind the registrar.", e);
			System.err.println("MalformedURLException: " + e.getMessage() );
			System.err.println("hint: check syntax of lookup String.");
		} catch (UnmarshalException e) {
			System.err.println("UnmarshalException: " + e.getMessage() );
			if ( sm == null )
				System.err.println("hint: insert RMI.setRMISecurityManager() before lookup.");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("NotBoundException: " + e.getMessage() );
			System.err.println("hint: RMI host is running, but remote object is not bound. ");
//			rmiRegistry.list() .. 
		} catch (java.security.AccessControlException e) {
			System.err.println("java.security.AccessControlException: " + e.getMessage() );
//			if ( sm == null )
//			System.err.println("hint: insert RMI.setRMISecurityManager() before lookup.");
			System.err.println("hint: add -Djava.security.policy='path to policy file' to commandline");
		} catch (Exception e) {
//			unexpected Exception trying to lookup host
			e.printStackTrace(); // anything else
		}
		if ( remoteObjectStub == null ) 
			System.err.println("hint: lookup was NOT successful -> return null");
		else
			System.out.println("Naming.lookup  successful :)");

		return remoteObjectStub;
	}
}