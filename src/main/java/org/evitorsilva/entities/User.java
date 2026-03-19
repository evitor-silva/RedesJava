package org.evitorsilva.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserPreference> preferences;

    public User() {}
    
    public User(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<UserPreference> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(List<UserPreference> preferences) {
        this.preferences = preferences;
    }
}
