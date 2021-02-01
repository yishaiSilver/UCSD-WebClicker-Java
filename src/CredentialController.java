import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class CredentialController{
	
//	private boolean hasFile;
	
	private String username = "";
	private String password = "";
	
	private JTextField usernameBox;
	private JPasswordField passwordBox;
	
	public CredentialController(JTextField userBox, JPasswordField passBox) {
		
		usernameBox = userBox;
		passwordBox = passBox;
		
		File file = new File("creds.txt");
		
		if(file.exists() && !file.isDirectory()) {
			try {
				FileReader fileReader = new FileReader("creds.txt");
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				
				username = bufferedReader.readLine();
				password = bufferedReader.readLine();
				
				bufferedReader.close();
				
//				username = "instructor@ucsd.edu";
//				password = "instructor";
				
				usernameBox.setText(username);
				passwordBox.setText(password);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {	
		}
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void saveCreds(String username, String encryptedPassword) {
		try {
			File oldCreds = new File("creds.txt");
			oldCreds.createNewFile();
			
			FileWriter file = new FileWriter("creds.txt");
			file.write(username + "\n");
			file.write(encryptedPassword);
			file.close();
			
			System.out.println("saving creds");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}