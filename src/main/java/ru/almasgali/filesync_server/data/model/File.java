package ru.almasgali.filesync_server.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "files")
@AllArgsConstructor
@Data
@Builder
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String username;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private long createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private long updatedAt;
}
