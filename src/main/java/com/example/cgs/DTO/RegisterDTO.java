package com.example.cgs.DTO;

public class RegisterDTO {

    private String name;
    private String email;
    private String password;
    private String dob;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	@Override
	public String toString() {
		return "RegisterDTO [name=" + name + ", email=" + email + ", password=" + password + ", dob=" + dob + "]";
	}    
}
