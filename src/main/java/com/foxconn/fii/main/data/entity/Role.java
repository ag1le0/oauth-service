package com.foxconn.fii.main.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "oauth_role")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role", unique = true)
    private String role;

    @Column(name = "description")
    private String description;

    @Column(name = "system")
    private String system;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
