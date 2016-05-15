//Alex Foster

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class Alert extends UnicastRemoteObject implements AlertInterface{

public Alert() throws RemoteException{
	super();
}

public void printAlert(String s) throws RemoteException{
	System.out.println(s);
}

}

