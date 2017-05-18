package kr.ac.jbnu.ssel.instantfeedback.domain;

import java.util.Date;

public class User {

	private String username;
	private int age;
	private int experience;
	private int javaExperience;
	private String area;
	private Date createdDate;
	
	public User()
	{		
	}
	
	public User(String username)
	{
		setUsername(username);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getExpierence() {
		return experience;
	}

	public void setExpierence(int expierence) {
		this.experience = expierence;
	}

	public int getJavaExpierence() {
		return javaExperience;
	}

	public void setJavaExpierence(int javaExpierence) {
		this.javaExperience = javaExpierence;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
