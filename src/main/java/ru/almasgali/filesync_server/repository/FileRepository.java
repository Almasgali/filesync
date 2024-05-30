package ru.almasgali.filesync_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.almasgali.filesync_server.data.model.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
