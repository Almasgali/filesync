package ru.almasgali.filesync_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.almasgali.filesync_server.data.model.File;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByNameAndUsername(String name, String username);

    List<File> findByUsername(String username);
}
