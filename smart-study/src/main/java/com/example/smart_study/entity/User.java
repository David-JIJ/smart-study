package com.example.smart_study.entity;

public class User {
    private Integer id;
    private String username;
    private String password;
    private String role;

    // 下面是 Java 的固定写法，你可以右键选择 Generate -> Getter and Setter 全选生成
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
