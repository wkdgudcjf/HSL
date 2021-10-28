package com.samsung.hsl.fitnesstrainer.sqlite;

public class User {
	public long id;
	public String name;
	public String email;
	public String password;
	public String gender;
	public String birthday;
	public float height;
	public float weight;
	public int stableHeartrate;
	public String checktime;
	public byte[] picture;
	public String datetime;
	
	 @Override
    public int hashCode() {
        return email.hashCode();
            
    }
	 
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (!(o instanceof User))
            return false;
        if (o == this)
            return true;

        User user = (User) o;
        return user.email.equals(this.email);
	}
	
	public void setUser(User user){
		this.id = user.id;
		this.name = user.name;
		this.email = user.email;
		this.password = user.password;
		this.gender = user.gender;
		this.birthday = user.birthday;
		this.height = user.height;
		this.weight = user.weight;
		this.stableHeartrate = user.stableHeartrate;
		this.checktime = user.checktime;
		this.picture = user.picture;
		this.datetime = user.datetime;
		
	}
}
