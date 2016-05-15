//Alex Foster

import java.rmi.*;

public interface AlertInterface extends Remote{
	void printAlert(String s) throws RemoteException;
}
