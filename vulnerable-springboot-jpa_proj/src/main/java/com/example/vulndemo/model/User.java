
package com.example.vulndemo.model;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column
    public String username;

    @Column
    public String password;

    @Column
    public String email;

    public User() {}
}
