import java.util.HashMap;

public class UsersDataBase {
	
	private HashMap<String, User> users;
	
	public UsersDataBase(){
		users = new HashMap<String,User>();
	}
	
	public void update(String key, String receiverName, String receiverSurname, String line){
		User user = users.get(key);
		user.addToHistory(receiverName + receiverSurname, line);
	}
	
	public String getHistory(String key, String receiverName, String receiverSurname){
		User user = users.get(key);
		String history = user.getHistory(receiverName + receiverSurname);
		return history;
	}
	
	public boolean addUserToDB(String name, String password){
		if (!userExists(name)){
			//System.out.println("adding " + name);
			User user = new User(name, password);
			users.put(name, user);
			return true;
		}
		return false;
	}

	public boolean passwordCorrect(String name, String password){
		if(userExists(name)){
			User user = users.get(name);
			if(user.getPassword().equals(password)) return true;
		}
		return false;
	}

	public User getUser(String name){
		return users.get(name);
	}
	
	public boolean userExists(String name){
		//System.out.println("users: ");
		//for (String key : users.keySet())
		//	System.out.println(key);
		if (users.containsKey(name))
			return true;
		else return false;
	}
}
