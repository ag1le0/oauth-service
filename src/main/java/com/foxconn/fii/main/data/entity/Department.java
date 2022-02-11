package com.foxconn.fii.main.data.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "hr_pm_info_user_tmp", schema = "dbo", catalog = "uthing")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dept_code")
    private String ouCode;

    @Column(name = "emp_no")
    private String employeeId;

    @Column(name = "USER_LEVEL")
    private String level;

    @Column(name = "status")
    private int status;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
